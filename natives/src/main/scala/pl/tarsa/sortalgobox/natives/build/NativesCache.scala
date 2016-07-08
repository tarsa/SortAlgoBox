/*
 * Copyright (C) 2015, 2016 Piotr Tarsa ( http://github.com/tarsa )
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
 */
package pl.tarsa.sortalgobox.natives.build

import java.io.File
import java.nio.file.Files

import org.apache.commons.io.FileUtils
import pl.tarsa.sortalgobox.common.SortAlgoBoxConfiguration.rootTempDir
import pl.tarsa.sortalgobox.common.cache.BuildCache
import pl.tarsa.sortalgobox.common.system.{ProcessRunResult, ProcessUtils}

class NativesCache extends BuildCache {
  override type BuildKey = NativeBuildConfig
  override type BuildValue = File
  override type BuildError = String

  def startCachedProgram(buildConfig: NativeBuildConfig): Process = {
    val workDir = getWorkDir(buildConfig)
    new ProcessBuilder(s"./${buildConfig.compilerOptions.executableFileName}")
      .directory(workDir).start()
  }

  def runCachedProgram(buildConfig: NativeBuildConfig, stdInLines: Seq[String]):
  ProcessRunResult = {
    val workDir = getWorkDir(buildConfig)
    val cmdLine = Seq(s"./${buildConfig.compilerOptions.executableFileName}")
    ProcessUtils.runSynchronously(cmdLine, workDir, stdInLines)
  }

  private def getWorkDir(buildConfig: NativeBuildConfig): File = {
    cachedBuild(buildConfig) match {
      case BuildSucceeded(workDir) =>
        workDir
      case BuildFailed(message) =>
        throw new Exception(message)
      case _ =>
        throw new Exception("Natives cache in wrong state")
    }
  }

  override protected def build(buildConfig: NativeBuildConfig):
  Either[String, File] = {
    val workDir = Files.createTempDirectory(rootTempDir, "native")
    val workDirFile = workDir.toFile
    buildConfig.copyBuildComponents(workDir)
    val result = ProcessUtils.runSynchronously(
      buildConfig.makeCommandLine, workDirFile, Nil)
    if (result.exitValue == 0) {
      Right(workDirFile)
    } else {
      FileUtils.deleteDirectory(workDirFile)
      Left(result.toString)
    }
  }

  /** Warning: this method is not synchronized */
  def cleanup(): Unit = cleanUpCache {
    case BuildSucceeded(directory) =>
      FileUtils.deleteDirectory(directory)
    case _ =>
  }
}

object NativesCache extends NativesCache
