/*
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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.{ReentrantLock, Lock}

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

trait CLProgramsCache {
  private val programsCacheLock: Lock = new ReentrantLock(true)

  private val programsCache = collection.mutable.Map[(CLDeviceContext,
    List[String]), CLBuildStatus]()

  private val buildIdGenerator = new AtomicLong

  private def withProgramsCacheLock[T](body: => T): T = {
    programsCacheLock.lockInterruptibly()
    try {
      body
    } finally {
      programsCacheLock.unlock()
    }
  }

  def getCachedProgram(deviceContext: CLDeviceContext,
    programSources: List[String]): cl_program = {
    val buildConfig = (deviceContext, programSources)
    val nextBuildId = buildIdGenerator.incrementAndGet()
    val cachedBuildStatus = withProgramsCacheLock {
      programsCache.getOrElseUpdate(buildConfig, {
        CLBuildStarting(nextBuildId, new CountDownLatch(1))
      })
    }
    cachedBuildStatus match {
      case CLBuildStarting(buildId, latch) if buildId == nextBuildId =>
        val newBuildStatus = buildProgram(deviceContext, programSources)
          .fold[CLBuildStatus](CLBuildFailed, CLBuildSucceeded)
        withProgramsCacheLock {
          programsCache.update(buildConfig, newBuildStatus)
        }
        latch.countDown()
      case CLBuildStarting(_, latch) =>
        latch.await()
      case _ =>
    }
    val buildStatus = withProgramsCacheLock(programsCache(buildConfig))
    buildStatus match {
      case CLBuildSucceeded(clProgram) =>
        clProgram
      case CLBuildFailed(message) =>
        throw new Exception(message)
      case _ =>
        throw new Exception("OpenCL cache in wrong state")
    }
  }

  private def buildProgram(deviceContext: CLDeviceContext,
    programSources: List[String]): Either[String, cl_program] = {
    try {
      val program = clCreateProgramWithSource(deviceContext.context,
        programSources.size, programSources.toArray, null, null)

      clBuildProgram(program, 0, null, null, null, null)

      Right(program)
    } catch {
      case e: CLException =>
        Left(e.toString)
    }
  }
}

object CLCache extends CLContextsCache with CLProgramsCache
