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

import java.util.Scanner

import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

class NativesCacheTest extends NativesUnitSpecBase {
  typeBehavior[NativesCache]

  it should "compile file without defines" in {
    val buildConfig = NativeBuildConfig(NativesCacheTest.components,
      "source.cpp")
    val process = testNativesCache.runCachedProgram(buildConfig)
    val pipeFrom = new Scanner(process.getInputStream)
    assertResult("Hello.")(pipeFrom.nextLine())
  }

  it should "compile file with header" in {
    val compilerOptions = CompilerOptions(
      options = CompilerOptions.defaultOptions ++ Seq("-include", "source.hpp"))
    val buildConfig = NativeBuildConfig(NativesCacheTest.components,
      "source.cpp", compilerOptions)
    val process = testNativesCache.runCachedProgram(buildConfig)
    val pipeFrom = new Scanner(process.getInputStream)
    assertResult("Hello from main.hpp")(pipeFrom.nextLine())
  }

  it should "compile file with define" in {
    val compilerOptions = CompilerOptions(defines =
      CompilerOptions.defaultDefines ++
        Seq(CompilerDefine("SOURCE", Some("test"))))
    val buildConfig = NativeBuildConfig(NativesCacheTest.components,
      "source.cpp", compilerOptions)
    val process = testNativesCache.runCachedProgram(buildConfig)
    val pipeFrom = new Scanner(process.getInputStream)
    assertResult("Hello from test!")(pipeFrom.nextLine())
  }
}

object NativesCacheTest extends NativeComponentsSupport {
  val components = makeComponents(
    ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
    ("/pl/tarsa/sortalgobox/natives/", "source.cpp"),
    ("/pl/tarsa/sortalgobox/natives/", "source.hpp"))
}
