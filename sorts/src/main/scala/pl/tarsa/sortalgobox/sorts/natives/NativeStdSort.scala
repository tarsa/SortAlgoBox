/*
 * Copyright (C) 2015, 2016 Piotr Tarsa ( http://github.com/tarsa )
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
package pl.tarsa.sortalgobox.sorts.natives

import java.lang.Long._

import pl.tarsa.sortalgobox.core.NativeBenchmark
import pl.tarsa.sortalgobox.natives.build._
import pl.tarsa.sortalgobox.random.NativeMwc64x

class NativeStdSort(nativesCache: NativesCache = NativesCache)
  extends NativeBenchmark {

  val name = getClass.getSimpleName

  val buildConfig = {
    val algoDefines = Seq(CompilerDefine("SORT_MECHANICS", Some("main.hpp")))
    val compilerOptions = CompilerOptions(defines =
      CompilerOptions.defaultDefines ++ algoDefines)
    NativeBuildConfig(NativeStdSort.components, "main.cpp", compilerOptions)
  }

  override def forSize(n: Int, validate: Boolean,
    buffer: Option[Array[Int]]): Long = {
    val input = Seq(if (validate) 1 else 0, n).map(_.toString)
    val execResult = nativesCache.runCachedProgram(buildConfig, input)
    val lines = execResult.stdOut.lines.toList
    if (validate) {
      val valid = lines(1) == "pass"
      assert(valid)
    }
    parseLong(lines.head, 16)
  }
}

object NativeStdSort extends NativeComponentsSupport {
  val components = NativeMwc64x.header ++ makeResourceComponents(
    ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
    ("/pl/tarsa/sortalgobox/natives/", "utilities.hpp"),
    ("/pl/tarsa/sortalgobox/sorts/natives/", "main.cpp"),
    ("/pl/tarsa/sortalgobox/sorts/natives/", "standard_items_handler.hpp"),
    ("/pl/tarsa/sortalgobox/sorts/natives/std__sort/", "main.hpp"))
}
