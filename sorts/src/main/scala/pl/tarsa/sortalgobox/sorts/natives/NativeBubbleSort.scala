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
package pl.tarsa.sortalgobox.sorts.natives

import java.io.PrintStream
import java.util.Scanner

import pl.tarsa.sortalgobox.core.NativeBenchmark
import pl.tarsa.sortalgobox.natives.build._
import pl.tarsa.sortalgobox.random.NativeMwc64x

class NativeBubbleSort(nativesCache: NativesCache = NativesCache)
  extends NativeBenchmark {

  val name = getClass.getSimpleName

  val buildConfig = {
    val algoDefines = Seq(CompilerDefine("SORT_MECHANICS", Some("main.hpp")))
    val compilerOptions = CompilerOptions(defines =
      CompilerOptions.defaultDefines ++ algoDefines)
    NativeBuildConfig(NativeBubbleSort.components, "main.cpp", compilerOptions)
  }

  override def forSize(n: Int, validate: Boolean,
    buffer: Option[Array[Int]]): Long = {

    val generatorProcess = nativesCache.runCachedProgram(buildConfig)
    val pipeTo = new PrintStream(generatorProcess.getOutputStream)
    pipeTo.println(if (validate) 1 else 0)
    pipeTo.println(n)
    pipeTo.flush()
    val pipeFrom = new Scanner(generatorProcess.getInputStream)
    val result = pipeFrom.nextLong(16).toInt
    if (validate) {
      val valid = pipeFrom.next() == "pass"
      assert(valid)
    }
    generatorProcess.waitFor()
    result
  }
}

object NativeBubbleSort extends NativeComponentsSupport {
  val components = NativeMwc64x.header ++ makeComponents(
    ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
    ("/pl/tarsa/sortalgobox/natives/", "utilities.hpp"),
    ("/pl/tarsa/sortalgobox/sorts/natives/", "main.cpp"),
    ("/pl/tarsa/sortalgobox/sorts/natives/bubble/", "main.hpp"))
}
