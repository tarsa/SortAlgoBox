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

import java.nio.file.Files

import pl.tarsa.sortalgobox.common.{SortAlgoBoxConfiguration, SortAlgoBoxConstants}
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class NativeBuildConfigSpec extends CommonUnitSpecBase {
  typeBehavior[NativeBuildConfig]

  it must "make proper command line" in {
    val defines = Seq(CompilerDefine("name1", None),
      CompilerDefine("name2", Some("value")))
    val options = CompilerOptions("compiler", Some("standard"), Some("level"),
      defines, Seq("-option"), "executable")
    val buildConfig = NativeBuildConfig(null, "file.ext", options)

    val expected = Seq("compiler", "-std=standard", "level", "-option",
      "-Dname1", "-Dname2=value", "-o", "executable", "file.ext")
    val actual = buildConfig.makeCommandLine

    assertResult(expected)(actual)
  }

  it must "make proper CMakeLists" in {
    val components = null
    val mainSourceFile = "abc.xyz"
    val compilerOptions = CompilerOptions(
      compiler = "aCompiler",
      languageStandardOpt = Some("standard"),
      optimizationLevelOpt = Some("level"),
      defines = Seq(CompilerDefine("name1", None),
        CompilerDefine("name2", Some("value"))),
      options = Seq("-option1", "-option2"),
      executableFileName = "abc.xxx")


    val expected = Seq(
      "cmake_minimum_required (VERSION 2.8)",
      "project (SortAlgo)",
      """set(PROJECT_COMPILE_FLAGS "-std=standard level -option1 -option2")""",
      """set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} """ +
        """${PROJECT_COMPILE_FLAGS}" )""",
      "add_definitions(-Dname1 -Dname2=value)",
      "add_executable(SortAlgo abc.xyz)")
    val actual = NativeBuildConfig(components, mainSourceFile, compilerOptions)
      .makeCMakeLists

    assertResult(expected)(actual)
  }

  it must "copy build components" in {
    val fileName1 = "test_file"
    val fileName2 = "aFileName.ext"
    val fileName3 = "generated.txt"

    val resourceNamePrefix = "/pl/tarsa/sortalgobox/build/"
    val contents = "someContents"
    val generator = () => "abc" * 5

    val components = Seq(
      NativeBuildComponentFromResource(resourceNamePrefix, fileName1),
      NativeBuildComponentFromString(contents, fileName2),
      NativeBuildComponentFromGenerator(generator, fileName3,
        prependLicense = false))

    val destination = Files.createTempDirectory(
      SortAlgoBoxConfiguration.rootTempDir, "test")
    val file1 = destination.resolve(fileName1)
    val file2 = destination.resolve(fileName2)
    val file3 = destination.resolve(fileName3)

    NativeBuildConfig(components, null, null).copyBuildComponents(destination)

    assert(Files.readAllBytes(file1) ===
      "Lorem ipsum dolor sit amet.\n".getBytes("UTF-8"))
    assert(Files.readAllBytes(file2) ===
      SortAlgoBoxConstants.licenseHeader ++ contents.getBytes("UTF-8"))
    assert(Files.readAllBytes(file3) ===
      generator().getBytes("UTF-8"))

    Files.delete(file1)
    Files.delete(file2)
    Files.delete(file3)
    Files.delete(destination)
  }
}
