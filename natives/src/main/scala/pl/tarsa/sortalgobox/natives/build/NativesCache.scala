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
package pl.tarsa.sortalgobox.natives.build

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.{Lock, ReentrantLock}

import scala.io.Source

class NativesCache {
  private val programsCacheLock: Lock = new ReentrantLock(true)

  private val programsCache = collection.mutable.Map[
    NativeBuildConfig, NativeBuildStatus]()

  private val buildIdGenerator = new AtomicLong

  private def withProgramsCacheLock[T](body: => T): T = {
    programsCacheLock.lockInterruptibly()
    try {
      body
    } finally {
      programsCacheLock.unlock()
    }
  }

  def runCachedProgram(buildConfig: NativeBuildConfig): Process = {
    val nextBuildId = buildIdGenerator.incrementAndGet()
    val cachedBuildStatus = withProgramsCacheLock {
      programsCache.getOrElseUpdate(buildConfig, {
        NativeBuildStarting(nextBuildId, new CountDownLatch(1))
      })
    }
    cachedBuildStatus match {
      case NativeBuildStarting(buildId, latch) if buildId == nextBuildId =>
        val newBuildStatus = buildProgram(buildConfig).fold[NativeBuildStatus](
          NativeBuildFailed, NativeBuildSucceeded)
        withProgramsCacheLock {
          programsCache.update(buildConfig, newBuildStatus)
        }
        latch.countDown()
      case NativeBuildStarting(_, latch) =>
        latch.await()
      case _ =>
    }
    val buildStatus = withProgramsCacheLock(programsCache(buildConfig))
    buildStatus match {
      case NativeBuildSucceeded(workDir) =>
        new ProcessBuilder(
          s"./${buildConfig.compilerOptions.executableFileName}")
          .directory(workDir).start()
      case NativeBuildFailed(message) =>
        throw new Exception(message)
      case _ =>
        throw new Exception("Natives cache in wrong state")
    }
  }

  private def buildProgram(buildConfig: NativeBuildConfig):
  Either[String, File] = {
    NativesCache.rootTempDir.toFile.mkdir()
    val workDir = Files.createTempDirectory(NativesCache.rootTempDir, "native")
    val workDirFile = workDir.toFile
    buildConfig.copyBuildComponents(workDir)
    val buildProcess = new ProcessBuilder(buildConfig.makeCommandLine: _*)
      .directory(workDirFile).start()
    val buildExitValue = buildProcess.waitFor()
    if (buildExitValue != 0) {
      val output = Source.fromInputStream(buildProcess.getErrorStream, "UTF-8")
        .mkString
      removeDir(workDirFile)
      val msg = s"Build process exit value: $buildExitValue, output:\n$output"
      Left(msg)
    } else {
      Right(workDirFile)
    }
  }

  def cleanup(): Unit = {
    withProgramsCacheLock {
      programsCache.values.foreach {
        case NativeBuildSucceeded(directory) =>
          removeDir(directory)
        case _ =>
      }
      programsCache.clear()
    }
  }

  private def removeDir(dir: File): Unit = {
    Option(dir.listFiles()).foreach(_.foreach(removeDir))
    dir.delete()
  }
}

object NativesCache extends NativesCache {
  val rootTempDir = Paths.get(System.getProperty("java.io.tmpdir"),
    "SortAlgoBox")
}
