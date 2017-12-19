/*
 * Copyright (C) 2015 - 2017 Piotr Tarsa ( http://github.com/tarsa )
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

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class CompilerOptionsSpec extends CommonUnitSpecBase {
  typeBehavior[CompilerOptions]

  it must "serialize default compiler options" in {
    val serialized = CompilerOptions.default.serializeAll
    serialized mustBe Seq("g++",
                          "-std=c++11",
                          "-O2",
                          "-fopenmp",
                          "-mavx2",
                          "-o",
                          "program")
  }

  it must "serialize empty options" in {
    val serialized =
      CompilerOptions("aCompiler", None, None, Nil, Nil, "aProgram").serializeAll
    serialized mustBe Seq("aCompiler", "-o", "aProgram")
  }

  it must "serialize custom options" in {
    val serialized = CompilerOptions(
      "aCompiler",
      Some("abc34"),
      Some("-O7"),
      Seq(CompilerDefine("name1", None),
          CompilerDefine("name2", Some("value"))),
      Seq("-option1", "-option2"),
      "anExecutable"
    ).serializeAll
    serialized mustBe Seq("aCompiler",
                          "-std=abc34",
                          "-O7",
                          "-option1",
                          "-option2",
                          "-Dname1",
                          "-Dname2=value",
                          "-o",
                          "anExecutable")
  }
}
