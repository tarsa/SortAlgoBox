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
package pl.tarsa.sortalgobox.random

import pl.tarsa.sortalgobox.opencl.common.{CLDeviceContext, CLContextsCache}

import java.nio.{ByteOrder, ByteBuffer}

import org.jocl.CL._
import org.jocl._

import scala.Array._
import scala.io.Source

class GpuMwc64x {
  val sourceCodePaths = List(
    "/pl/tarsa/sortalgobox/random/mwc64x/skip_mwc.cl",
    "/pl/tarsa/sortalgobox/random/mwc64x/mwc64x_rng.cl",
    "/pl/tarsa/sortalgobox/random/mwc64x/mwc64xvec2_rng.cl",
    "/pl/tarsa/sortalgobox/random/mwc64x/mwc64xvec4_rng.cl",
    "/pl/tarsa/sortalgobox/random/mwc64x/mwc64xvec8_rng.cl",
    "/pl/tarsa/sortalgobox/random/mwc64x/dump_kernels.cl")
  val allowedVectorLengths = Set(1, 2, 4, 8)

  def generate(n: Int, workItems: Int, vectorLength: Int = 1): Array[Int] = {
    if (!allowedVectorLengths.contains(vectorLength)) {
      throw new IllegalArgumentException("Wrong vector length")
    }
    val array = Array.ofDim[Int](n)
    CLContextsCache.withGpuContext { deviceContext =>
      generateWithContext(array, workItems, vectorLength, deviceContext)
    }
    array
  }

  def generateWithContext(array: Array[Int], workItems: Int, vectorLength: Int,
    deviceContext: CLDeviceContext): Unit = {
    if (array.isEmpty) {
      return
    }

    val bytesInInt = 4

    setExceptionsEnabled(true)

    val commandQueue = clCreateCommandQueue(deviceContext.context,
      deviceContext.deviceId, 0, null)

    val memObjects = ofDim[cl_mem](1)

    memObjects(0) = clCreateBuffer(deviceContext.context,
      CL_MEM_READ_WRITE, array.length * bytesInInt, null, null)

    val programSources = sourceCodePaths.map { sourceCodePath =>
      val programFile = Source.fromInputStream(
        getClass.getResourceAsStream(sourceCodePath), "UTF-8")
      val programSource = programFile.getLines().mkString("\n")
      programFile.close()
      programSource
    }

    val program = clCreateProgramWithSource(deviceContext.context,
      programSources.size, programSources.toArray, null, null)

    clBuildProgram(program, 0, null, null, null, null)

    val kernelName = s"DumpSamples_$vectorLength"
    val kernel = clCreateKernel(program, kernelName, null)

    val chunks = workItems * vectorLength
    val chunkSize = (array.length + chunks - 1) / chunks

    clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects(0)))
    clSetKernelArg(kernel, 1, Sizeof.cl_int, Pointer.to(Array(array.length)))
    clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(Array(chunkSize)))

    val global_work_size = Array(workItems.toLong)
    val local_work_size = Array(256L)

    clEnqueueNDRangeKernel(commandQueue, kernel, 1, null, global_work_size,
      local_work_size, 0, null, null)


    val buffer = ByteBuffer.allocateDirect(array.length * bytesInInt)
      .order(ByteOrder.nativeOrder()).asIntBuffer()
    val pBuffer = Pointer.to(buffer)

    clEnqueueReadBuffer(commandQueue, memObjects(0), CL_TRUE, 0,
      array.length * bytesInInt, pBuffer, 0, null, null)
    buffer.get(array)

    memObjects.foreach(clReleaseMemObject)
    clReleaseKernel(kernel)
    clReleaseProgram(program)
    clReleaseCommandQueue(commandQueue)
  }
}
