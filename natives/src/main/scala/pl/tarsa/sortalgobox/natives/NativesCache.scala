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

import scala.io.Source

class NativesCache {
  val executableFileName = "program"

  def runCachedProgram(sources: Seq[NativeSource]): Process = {
    val workDir = programsCache.getOrElseUpdate(sources, buildProgram(sources))
    new ProcessBuilder(s"./$executableFileName").directory(workDir).start()
  }

  private val programsCache = collection.mutable.Map[Seq[NativeSource], File]()

  private def buildProgram(sources: Seq[NativeSource]): File = {
    val rootTempDir = new File(System.getProperty("java.io.tmpdir"),
      "SortAlgoBox")
    rootTempDir.mkdir()
    val workDir = Files.createTempDirectory(rootTempDir.toPath, "native")
    sources.foreach { source =>
      val sourcePath = workDir.resolve(source.fileName)
      val resourceName = source.resourceNamePrefix + source.fileName
      Files.copy(getClass.getResourceAsStream(resourceName), sourcePath)
    }
    val buildProcess = new ProcessBuilder(Seq("g++", "-fopenmp", "-O2",
      "-std=c++11", "-o", executableFileName) ++ sources.map(_.fileName): _*)
      .directory(workDir.toFile).start()
    val buildExitValue = buildProcess.waitFor()
    if (buildExitValue != 0) {
      val output = Source.fromInputStream(buildProcess.getErrorStream, "UTF-8")
        .mkString
      removeDir(workDir.toFile)
      val msg = s"Build process exit value: $buildExitValue, output: $output"
      throw new Exception(msg)
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
