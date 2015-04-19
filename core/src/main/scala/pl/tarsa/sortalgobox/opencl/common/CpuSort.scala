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
package pl.tarsa.sortalgobox.opencl.common

import java.nio.{ByteBuffer, ByteOrder}

import org.jocl.CL._
import org.jocl.{Pointer, Sizeof, cl_mem}
import pl.tarsa.sortalgobox.sorts.common.SortAlgorithm

import scala.Array._
import scala.io.Source

class CpuSort(sourceCodePath: String) extends SortAlgorithm[Int] {
  override def sort(array: Array[Int]): Unit = sort(
    array, FakeTimeLine, Nil, Nil)

  def sort(array: Array[Int], timeLine: TimeLine, additionalBuffers: List[Int],
    precomputedArrays: List[Array[Int]]): Unit = {

    if (array.isEmpty || array.length == 1) {
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

    val memObjects = ofDim[cl_mem](1 + additionalBuffers.size +
      precomputedArrays.size)
    val additionalBuffersStart = 1
    val precomputedArraysStart = additionalBuffersStart +
      additionalBuffers.size

    memObjects(0) = clCreateBuffer(context,
      CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
      array.length * bytesInInt, pBuffer, null)
    additionalBuffers.zipWithIndex.foreach { case (n, i) =>
      memObjects(additionalBuffersStart + i) = clCreateBuffer(context,
        CL_MEM_READ_WRITE, n * bytesInInt, null, null)
    }
    precomputedArrays.zipWithIndex.foreach { case (precomputedArray, i) =>
      val buffer = ByteBuffer.allocateDirect(precomputedArray.length *
        bytesInInt).order(ByteOrder.nativeOrder()).asIntBuffer()
      buffer.put(precomputedArray)
      val pBuffer = Pointer.to(buffer)
      memObjects(precomputedArraysStart + i) = clCreateBuffer(context,
        CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
        precomputedArray.length * bytesInInt, pBuffer, null)
    }
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
    additionalBuffers.zipWithIndex.foreach { case (n, i) =>
      val baseIndex = (additionalBuffersStart + i) * 2
      clSetKernelArg(kernel, baseIndex + 0, Sizeof.cl_mem,
        Pointer.to(memObjects(1 + i)))
      clSetKernelArg(kernel, baseIndex + 1, Sizeof.cl_int,
        Pointer.to(Array(n)))
    }
    precomputedArrays.zipWithIndex.foreach { case (precomputedArray, i) =>
      val baseIndex = (precomputedArraysStart + i) * 2
      clSetKernelArg(kernel, baseIndex + 0, Sizeof.cl_mem,
        Pointer.to(memObjects(1 + i)))
      clSetKernelArg(kernel, baseIndex + 1, Sizeof.cl_int,
        Pointer.to(Array(precomputedArray.length)))
    }

    timeLine.append("Kernel set up")

    val global_work_size = Array(1L)
    val local_work_size = Array(1L)

    clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size,
      local_work_size, 0, null, null)

    timeLine.append("Kernel executed")

    buffer.rewind()
    clEnqueueReadBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    buffer.get(array)
    timeLine.append("Array buffer retrieved")

    memObjects.foreach(clReleaseMemObject)
    clReleaseKernel(kernel)
    clReleaseProgram(program)
    clReleaseCommandQueue(commandQueue)
    clReleaseContext(context)

    timeLine.append("Host histogram computation ended")
  }
}
