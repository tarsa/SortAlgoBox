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
import java.nio.file.Files

class NativesCache {
  val fileNamePrefix = "main"
  val fileNameSource = s"$fileNamePrefix.cpp"

  def runCachedProgram(resourceName: String): Process = {
    val workDir = programsCache.getOrElseUpdate(resourceName,
      buildProgram(resourceName))
    new ProcessBuilder(s"./$fileNamePrefix").directory(workDir).start()
  }

  private val programsCache = collection.mutable.Map[String, File]()

  private def buildProgram(resourceName: String): File = {
    val rootTempDir = new File(System.getProperty("java.io.tmpdir"),
      "SortAlgoBox")
    rootTempDir.mkdir()
    val workDir = Files.createTempDirectory(rootTempDir.toPath, "native")
    val sourcePath = workDir.resolve(fileNameSource)
    Files.copy(getClass.getResourceAsStream(resourceName), sourcePath)
    val buildProcess = new ProcessBuilder("g++", "-fopenmp", "-O2",
      "-std=c++11", "-o", fileNamePrefix, fileNameSource)
      .directory(workDir.toFile).start()
    val buildExitValue = buildProcess.waitFor()
    if (buildExitValue != 0) {
      throw new Exception(s"Build process exit value: $buildExitValue")
    }
    workDir.toFile
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

object NativesCache extends NativesCache
