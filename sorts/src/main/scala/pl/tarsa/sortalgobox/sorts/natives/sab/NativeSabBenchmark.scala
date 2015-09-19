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
package pl.tarsa.sortalgobox.sorts.natives.sab

import java.io.PrintStream
import java.util.Scanner
import pl.tarsa.sortalgobox.core.NativeBenchmark
import pl.tarsa.sortalgobox.core.exceptions.VerificationFailedException
import pl.tarsa.sortalgobox.natives.build._
import pl.tarsa.sortalgobox.random.NativeMwc64x

class NativeSabBenchmark(sortAlgoName: String, sortHeader: String,
  nativesCache: NativesCache = NativesCache,
  sortCached: Boolean = false, sortSimd: Boolean = false)
  extends NativeBenchmark {

  val name = getClass.getSimpleName

  val buildConfig = {
    val sortTypeDefines = Seq(
      ("SORT_CACHED", sortCached),
      ("SORT_SIMD", sortSimd)
    ).collect { case (defineName, true) =>
      CompilerDefine(defineName, None)
    }
    val algoDefines = Seq(
      CompilerDefine("SORT_ALGO", Some(sortAlgoName)),
      CompilerDefine("SORT_HEADER", Some(sortHeader)),
      CompilerDefine("SORT_MECHANICS", Some("sabmain.hpp"))) ++ sortTypeDefines
    val compilerOptions = CompilerOptions(defines =
      CompilerOptions.defaultDefines ++ algoDefines)
    NativeBuildConfig(NativeSabBenchmark.components(sortHeader), "main.cpp",
      compilerOptions)
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
      if (!valid) {
        throw new VerificationFailedException()
      }
    }
    generatorProcess.waitFor()
    result
  }
}

object NativeSabBenchmark extends NativeComponentsSupport {
  val sabNamePrefix = "/pl/tarsa/sortalgobox/sorts/natives/sab/"

  def components(sortHeader: String) = NativeMwc64x.header ++ makeComponents(
    ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
    ("/pl/tarsa/sortalgobox/natives/", "utilities.hpp"),
    ("/pl/tarsa/sortalgobox/sorts/natives/", "main.cpp"),
    (sabNamePrefix, "sabmain.hpp"),
    (sabNamePrefix, "sortalgocommon.hpp"),
    (sabNamePrefix, sortHeader))
}
