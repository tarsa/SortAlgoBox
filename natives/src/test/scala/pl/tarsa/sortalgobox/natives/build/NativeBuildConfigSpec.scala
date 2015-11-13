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

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class NativeBuildConfigSpec extends CommonUnitSpecBase {
  typeBehavior[NativeBuildConfig]

  it should "make proper command line" in {
    val defines = Seq(CompilerDefine("name1", None),
      CompilerDefine("name2", Some("value")))
    val options = CompilerOptions("compiler", Some("standard"), Some("level"),
      defines, Seq("-option"), "executable")
    val sources = Seq.empty
    val buildConfig = NativeBuildConfig(sources, "file.ext", options)

    val expected = Seq("compiler", "-std=standard", "level", "-option",
      "-Dname1", "-Dname2=value", "-o", "executable", "file.ext")
    val actual = buildConfig.makeCommandLine

    assertResult(expected)(actual)
  }

  it should "make proper CMakeLists" in {
    val components = Seq(
      NativeBuildComponentFromResource("/some/package/", "file.ext1"),
      NativeBuildComponentFromString("someContents", "aFileName.ext2"))
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

  it should "copy build components" in pending
}
