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

import java.nio.{ByteOrder, ByteBuffer}

import org.jocl.CL._
import org.jocl.{Sizeof, cl_mem, Pointer}
import pl.tarsa.sortalgobox.opencl.common.{CLContextsManager, TimeLine, FakeTimeLine}
import pl.tarsa.sortalgobox.sorts.common.SortAlgorithm

import scala.Array._
import scala.io.Source

object GpuBitonicSort extends SortAlgorithm[Int] {
  val sourceCodePath = "/GpuBitonicSort.cl"

  override def sort(array: Array[Int]): Unit = sort(array, FakeTimeLine)

  def sort(array: Array[Int], timeLine: TimeLine): Unit = {

    if (array.isEmpty || array.length == 1) {
      return
    }

    val bytesInInt = 4
    val buffer = ByteBuffer.allocateDirect(array.length * bytesInInt)
      .order(ByteOrder.nativeOrder()).asIntBuffer()
    buffer.put(array)
    val pBuffer = Pointer.to(buffer)

    setExceptionsEnabled(true)

    val (deviceId, context) = CLContextsManager.createGpuContext()

    val commandQueue = clCreateCommandQueue(context, deviceId, 0, null)

    timeLine.append("Command queue set up")

    val memObjects = ofDim[cl_mem](1)

    memObjects(0) = clCreateBuffer(context,
      CL_MEM_READ_WRITE, array.length * bytesInInt, null, null)
    timeLine.append("Array buffer object created")

    clEnqueueWriteBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    timeLine.append("Main buffer transferred")

    val programFile = Source.fromInputStream(
      getClass.getResourceAsStream(sourceCodePath), "UTF-8")
    val programSource = programFile.getLines().mkString("\n")
    programFile.close()

    val program = clCreateProgramWithSource(context, 1, Array(programSource),
      null, null)

    clBuildProgram(program, 0, null, null, null, null)

    val kernelFirst = clCreateKernel(program, "firstPhase", null)
    val kernelFollowing = clCreateKernel(program, "followingPhase", null)

    val arrayBytes = array.length * bytesInInt

    val phasesPerBlock = Stream.iterate(1)(_ + 1)
      .takeWhile(phases => (1L << (phases - 1)) <= array.length - 1)
    for (phasesInBlock <- phasesPerBlock;
         phase <- phasesInBlock to 1 by -1) {
      val firstPhaseInBlock = phase == phasesInBlock

      val kernel = if (firstPhaseInBlock) kernelFirst else kernelFollowing

      clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects(0)))
      clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(Array(array.length)))
      clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(Array(phase)))

      timeLine.append("Kernel set up")

      val global_work_size = Array(array.length.toLong / 2)
      val local_work_size = Array(256L)

      clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size,
        local_work_size, 0, null, null)

      timeLine.append("Kernel executed")
    }

    buffer.rewind()
    clEnqueueReadBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    buffer.get(array)
    timeLine.append("Array buffer retrieved")

    memObjects.foreach(clReleaseMemObject)
    clReleaseKernel(kernelFirst)
    clReleaseKernel(kernelFollowing)
    clReleaseProgram(program)
    clReleaseCommandQueue(commandQueue)
    clReleaseContext(context)

    timeLine.append("Host histogram computation ended")
  }
}
