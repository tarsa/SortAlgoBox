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

  it should "make proper CMakeLists" in pending

  it should "copy build components" in pending
}
