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
import java.nio.file.Files

import org.apache.commons.io.FileUtils
import pl.tarsa.sortalgobox.common.SortAlgoBoxConfiguration.rootTempDir
import pl.tarsa.sortalgobox.common.cache.BuildCache

import scala.io.Source

class NativesCache extends BuildCache {
  override type BuildKey = NativeBuildConfig
  override type BuildValue = File
  override type BuildError = String

  def runCachedProgram(buildConfig: NativeBuildConfig): Process = {
    cachedBuild(buildConfig) match {
      case BuildSucceeded(workDir) =>
        new ProcessBuilder(
          s"./${buildConfig.compilerOptions.executableFileName}")
          .directory(workDir).start()
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
    val buildProcess = new ProcessBuilder(buildConfig.makeCommandLine: _*)
      .directory(workDirFile).start()
    val buildExitValue = buildProcess.waitFor()
    if (buildExitValue != 0) {
      val output = Source.fromInputStream(buildProcess.getErrorStream, "UTF-8")
        .mkString
      FileUtils.deleteDirectory(workDirFile)
      val msg = s"Build process exit value: $buildExitValue, output:\n$output"
      Left(msg)
    } else {
      Right(workDirFile)
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
