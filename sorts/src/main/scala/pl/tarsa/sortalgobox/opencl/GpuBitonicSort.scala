/**
 * Copyright (C) 2015 Piotr Tarsa ( http://github.com/tarsa )
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the author be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 *
 */
package pl.tarsa.sortalgobox.opencl

import java.nio.{ByteBuffer, ByteOrder}

import org.jocl.CL._
import org.jocl._
import pl.tarsa.sortalgobox.opencl.common._
import pl.tarsa.sortalgobox.sorts.common.MeasuredSortAlgorithm

import scala.Array._
import scala.io.Source

object GpuBitonicSort extends MeasuredSortAlgorithm[Int] {
  val sourceCodePath = "/pl/tarsa/sortalgobox/opencl/GpuBitonicSort.cl"

  override def sort(array: Array[Int]): Long = {
    CLCache.withGpuContext(sort(array, FakeTimeLine, _))
  }

  def sort(array: Array[Int], timeLine: TimeLine,
    deviceContext: CLDeviceContext): Long = {

    if (array.isEmpty || array.length == 1) {
      return 0
    }

    val bytesInInt = 4
    val buffer = ByteBuffer.allocateDirect(array.length * bytesInInt)
      .order(ByteOrder.nativeOrder()).asIntBuffer()
    buffer.put(array)
    val pBuffer = Pointer.to(buffer)

    setExceptionsEnabled(true)

    val commandQueue = clCreateCommandQueue(deviceContext.context,
      deviceContext.deviceId, CL_QUEUE_PROFILING_ENABLE, null)

    timeLine.append("Command queue set up")

    val memObjects = ofDim[cl_mem](1)

    memObjects(0) = clCreateBuffer(deviceContext.context,
      CL_MEM_READ_WRITE, array.length * bytesInInt, null, null)
    timeLine.append("Array buffer object created")

    clEnqueueWriteBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    timeLine.append("Main buffer transferred")

    val programFile = Source.fromInputStream(
      getClass.getResourceAsStream(sourceCodePath), "UTF-8")
    val programSource = programFile.getLines().mkString("\n")
    programFile.close()

    val program = CLCache.getCachedProgram(deviceContext, List(programSource))

    val kernelFirst = clCreateKernel(program, "firstPhase", null)
    val kernelFollowing = clCreateKernel(program, "followingPhase", null)

    val arrayBytes = array.length * bytesInInt

    val phasesPerBlock = Stream.iterate(1)(_ + 1)
      .takeWhile(phases => (1L << (phases - 1)) <= array.length - 1)
    val kernelEventsStream = for (phasesInBlock <- phasesPerBlock;
         phase <- phasesInBlock to 1 by -1) yield {
      val firstPhaseInBlock = phase == phasesInBlock

      val kernel = if (firstPhaseInBlock) kernelFirst else kernelFollowing

      clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects(0)))
      clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(Array(array.length)))
      clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(Array(phase)))

      timeLine.append("Kernel set up")

      val global_work_size = Array(array.length.toLong / 2)
      val local_work_size = Array(256L)

      val sortEvent = new cl_event
      clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size,
        local_work_size, 0, null, sortEvent)

      timeLine.append("Kernel executed")

      sortEvent
    }
    val kernelEvents = kernelEventsStream.toArray

    buffer.rewind()
    clEnqueueReadBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    buffer.get(array)
    timeLine.append("Array buffer retrieved")

    val commandStartTimeHolder = Array(-1L)
    clGetEventProfilingInfo(kernelEvents.head, CL_PROFILING_COMMAND_START,
      Sizeof.cl_ulong, Pointer.to(commandStartTimeHolder), null)
    val commandEndTimeHolder = Array(-1L)
    clGetEventProfilingInfo(kernelEvents.last, CL_PROFILING_COMMAND_END,
      Sizeof.cl_ulong, Pointer.to(commandEndTimeHolder), null)
    val executionTime = commandEndTimeHolder(0) - commandStartTimeHolder(0)

    memObjects.foreach(clReleaseMemObject)
    clReleaseKernel(kernelFirst)
    clReleaseKernel(kernelFollowing)
    clReleaseCommandQueue(commandQueue)

    timeLine.append("Sorting ended")

    executionTime
  }
}
