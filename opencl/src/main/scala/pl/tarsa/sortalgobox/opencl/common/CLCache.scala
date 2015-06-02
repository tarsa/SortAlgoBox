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

import org.jocl.CL._
import org.jocl._

trait CLContextsCache {
  lazy val cpuContext = CLContextsManager.createCpuContext()
  lazy val gpuContext = CLContextsManager.createGpuContext()

  def withCpuContext[T](f: CLDeviceContext => T): T = {
    f(cpuContext)
  }

  def withGpuContext[T](f: CLDeviceContext => T): T = {
    f(gpuContext)
  }
}

trait CLProgramsCache { CLContextsCache =>
  def getCachedProgram(deviceContext: CLDeviceContext,
    programSources: List[String]): cl_program = {
    programsCache.getOrElseUpdate((deviceContext, programSources),
      buildProgram(deviceContext, programSources))
  }

  private val programsCache = collection.mutable.Map[(CLDeviceContext,
    List[String]), cl_program]()

  private def buildProgram(deviceContext: CLDeviceContext,
    programSources: List[String]): cl_program = {
    val program = clCreateProgramWithSource(deviceContext.context,
      programSources.size, programSources.toArray, null, null)

    clBuildProgram(program, 0, null, null, null, null)

    program
  }
}

object CLCache extends CLContextsCache with CLProgramsCache
