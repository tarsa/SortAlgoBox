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

import pl.tarsa.sortalgobox.natives.build.NativesCacheSpec._
import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

class NativesCacheSpec extends NativesUnitSpecBase {
  typeBehavior[NativesCache]

  it must "compile file without defines" in {
    val buildConfig = NativeBuildConfig(components, "source.cpp")
    val execResult = testNativesCache.runCachedProgram(buildConfig, Nil)
    execResult.stdOut.trim mustBe "Hello."
  }

  it must "compile file with header" in {
    val buildConfig =
      NativeBuildConfig(components,
                        "source.cpp",
                        CompilerOptions(
                          options = CompilerOptions.defaultOptions ++
                            Seq("-include", "source.hpp")))
    val execResult = testNativesCache.runCachedProgram(buildConfig, Nil)
    execResult.stdOut.trim mustBe "Hello from main.hpp"
  }

  it must "compile file with define" in {
    val buildConfig =
      NativeBuildConfig(components,
                        "source.cpp",
                        CompilerOptions(
                          defines = CompilerOptions.defaultDefines ++
                            Seq(CompilerDefine("SOURCE", Some("test")))))
    val execResult = testNativesCache.runCachedProgram(buildConfig, Nil)
    execResult.stdOut.trim mustBe "Hello from test!"
  }

  it must "fail when compilation is unsuccessful" in {
    val buildConfig = NativeBuildConfig(Nil, "non_existing.cpp")
    an[Exception] mustBe thrownBy {
      testNativesCache.runCachedProgram(buildConfig, Nil)
    }
  }
}

object NativesCacheSpec extends NativeComponentsSupport {
  val components: Seq[NativeBuildComponent] = makeResourceComponents(
    ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
    ("/pl/tarsa/sortalgobox/natives/", "source.cpp"),
    ("/pl/tarsa/sortalgobox/natives/", "source.hpp"))
}
