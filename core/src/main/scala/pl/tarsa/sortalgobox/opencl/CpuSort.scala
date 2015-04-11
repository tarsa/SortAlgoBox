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
import pl.tarsa.sortalgobox.sorts.common.SortAlgorithm

import scala.Array._
import scala.io.Source

class CpuSort(sourceCodePath: String) extends SortAlgorithm[Int] {
  override def sort(array: Array[Int]): Unit = sort(array, FakeTimeLine)

  def sort(array: Array[Int], timeLine: TimeLine): Unit = {
    if (array.isEmpty) {
      return
    }

    val bytesInInt = 4
    val buffer = ByteBuffer.allocateDirect(array.length * bytesInInt)
      .order(ByteOrder.nativeOrder()).asIntBuffer()
    buffer.put(array)
    val pBuffer = Pointer.to(buffer)

    setExceptionsEnabled(true)

    val (deviceId, context) = CLContextsManager.createCpuContext()

    val commandQueue = clCreateCommandQueue(context, deviceId, 0, null)

    timeLine.append("Command queue set up")

    val memObjects = ofDim[cl_mem](1)
    memObjects(0) = clCreateBuffer(context,
      CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
      array.length * bytesInInt, pBuffer, null)
    timeLine.append("Array buffer object created")

    val programFile = Source.fromInputStream(
      getClass.getResourceAsStream(sourceCodePath), "UTF-8")
    val programSource = programFile.getLines().mkString("\n")
    programFile.close()

    val program = clCreateProgramWithSource(context, 1, Array(programSource),
      null, null)

    clBuildProgram(program, 0, null, null, null, null)

    val kernel = clCreateKernel(program, "sort", null)

    val arraySize = array.length

    clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects(0)))
    clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(Array(arraySize)))

    timeLine.append("Kernel set up")

    val global_work_size = Array(1L)
    val local_work_size = Array(1L)

    val time = System.nanoTime()
    clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size,
      local_work_size, 0, null, null)

    timeLine.append("Kernel executed")

    buffer.rewind()
    clEnqueueReadBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    buffer.get(array)
    timeLine.append("Array buffer retrieved")
    println(s"Time host: ${(System.nanoTime() - time) / 1e6}ms")

    memObjects.foreach(clReleaseMemObject)
    clReleaseKernel(kernel)
    clReleaseProgram(program)
    clReleaseCommandQueue(commandQueue)
    clReleaseContext(context)

    timeLine.append("Host histogram computation ended")
  }
}
