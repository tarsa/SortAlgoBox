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

import java.nio.file.{Files, Path}

case class NativeBuildConfig(components: Seq[NativeBuildComponent],
  mainSourceFile: String,
  compilerOptions: CompilerOptions = CompilerOptions.default) {

  def makeCommandLine: Seq[String] = {
    compilerOptions.serializeAll :+ mainSourceFile
  }

  def makeCMakeLists: Seq[String] = {
    val flags = compilerOptions.serializeFlags
    val defines = compilerOptions.serializeDefines
    Seq[String](
      "cmake_minimum_required (VERSION 2.8)",
      "project (SortAlgo)",
      flags.mkString("set(PROJECT_COMPILE_FLAGS \"", " ", "\")"),
      "set(CMAKE_CXX_FLAGS  \"${CMAKE_CXX_FLAGS} ${PROJECT_COMPILE_FLAGS}\" )",
      defines.mkString("add_definitions(", " ", ")"),
      s"add_executable(SortAlgo $mainSourceFile)"
    )
  }

  def copyBuildComponents(destination: Path): Unit = {
    components.foreach { component =>
      val componentPath = destination.resolve(component.fileName)
      val resourceName = component.resourceNamePrefix + component.fileName
      Files.copy(getClass.getResourceAsStream(resourceName), componentPath)
    }
  }
}
