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
package pl.tarsa.sortalgobox.natives

import java.io.File
import java.nio.file.{Paths, Path, Files}

import scala.io.Source

class NativesCache {
  def runCachedProgram(buildConfig: NativeBuildConfig): Process = {
    val workDir = programsCache.getOrElseUpdate(buildConfig,
      buildProgram(buildConfig))
    new ProcessBuilder(s"./${buildConfig.compilerOptions.executableFileName}")
      .directory(workDir).start()
  }

  private val programsCache = collection.mutable.Map[NativeBuildConfig, File]()

  private def buildProgram(buildConfig: NativeBuildConfig): File = {
    NativesCache.rootTempDir.toFile.mkdir()
    val workDir = Files.createTempDirectory(NativesCache.rootTempDir, "native")
    val workDirFile = workDir.toFile
    copyBuildComponents(buildConfig, workDir)
    val buildProcess = new ProcessBuilder(buildConfig.makeCommandLine: _*)
      .directory(workDirFile).start()
    val buildExitValue = buildProcess.waitFor()
    if (buildExitValue != 0) {
      val output = Source.fromInputStream(buildProcess.getErrorStream, "UTF-8")
        .mkString
      removeDir(workDirFile)
      val msg = s"Build process exit value: $buildExitValue, output:\n$output"
      throw new Exception(msg)
    }
    workDirFile
  }

  private def copyBuildComponents(buildConfig: NativeBuildConfig,
    workDir: Path): Unit = {

    buildConfig.components.foreach { component =>
      val sourcePath = workDir.resolve(component.fileName)
      val resourceName = component.resourceNamePrefix + component.fileName
      Files.copy(getClass.getResourceAsStream(resourceName), sourcePath)
    }
  }

  def cleanup(): Unit = {
    programsCache.values.foreach(removeDir)
    programsCache.clear()
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
