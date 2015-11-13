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
package pl.tarsa.sortalgobox.core

import java.io._
import java.nio.file.{Files, Path}
import java.util

import pl.tarsa.sortalgobox.natives.build.{NativeBuildConfig, NativesCache}

import scala.io.StdIn

class CMakeProjectGenerator(benchmarks: Seq[NativeBenchmark]) {
  def run(): Unit = {
    val projectDir = NativesCache.rootTempDir.resolve("project")

    deleteRecursively(projectDir.toFile)

    val benchmark = chooseBenchmark

    val buildConfig = benchmark.buildConfig

    projectDir.toFile.mkdirs()
    buildConfig.copyBuildComponents(projectDir)

    writeCMakeLists(projectDir, buildConfig)

    println("Done")
  }

  def chooseBenchmark = {
    println("Available benchmarks:")
    benchmarks.zipWithIndex.foreach { case (benchmark, number) =>
      println(s"$number. ${benchmark.name}")
    }
    val chosenNumber = StdIn.readLine("Choose benchmark number: ").toInt
    benchmarks(chosenNumber)
  }

  def writeCMakeLists(projectDir: Path, buildConfig: NativeBuildConfig) {
    val cmakeFile = projectDir.resolve("CMakeLists.txt")
    val cmakeLines = buildConfig.makeCMakeLists
    Files.write(cmakeFile, util.Arrays.asList(cmakeLines: _*))
  }

  def deleteRecursively(file: File): Unit = {
    Option(file.listFiles()).foreach(_.foreach(deleteRecursively))
    file.delete()
  }
}
