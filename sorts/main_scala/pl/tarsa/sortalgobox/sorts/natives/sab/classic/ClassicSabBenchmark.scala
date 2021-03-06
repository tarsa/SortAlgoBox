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
package pl.tarsa.sortalgobox.sorts.natives.sab.classic

import java.lang.Long.parseLong

import pl.tarsa.sortalgobox.core.NativeBenchmark
import pl.tarsa.sortalgobox.core.exceptions.VerificationFailedException
import pl.tarsa.sortalgobox.natives.build.{
  CompilerDefine,
  CompilerOptions,
  NativeBuildComponent,
  NativeBuildConfig,
  NativeComponentsSupport,
  NativesCache
}
import pl.tarsa.sortalgobox.random.NativeMwc64x

import scala.concurrent.duration.{Duration, FiniteDuration}

class ClassicSabBenchmark(sortAlgoName: String,
                          sortHeader: String,
                          nativesCache: NativesCache)
    extends NativeBenchmark {

  override val buildConfig: NativeBuildConfig = {
    val algoDefines = Seq(
      CompilerDefine("ITEMS_HANDLER_TYPE", Some("ITEMS_HANDLER_AGENT_PLAIN")),
      CompilerDefine("SORT_ALGO", Some(sortAlgoName)),
      CompilerDefine("SORT_HEADER", Some(sortHeader)),
      CompilerDefine("SORT_MECHANICS", Some("sabmain.hpp"))
    )
    val compilerOptions = CompilerOptions(
      defines = CompilerOptions.defaultDefines ++ algoDefines)
    NativeBuildConfig(ClassicSabBenchmark.components(sortHeader),
                      "main.cpp",
                      compilerOptions)
  }

  override def forSize(itemsNumber: Int,
                       validate: Boolean,
                       buffer: Option[Array[Int]]): FiniteDuration = {
    val input = Seq(if (validate) 1 else 0, itemsNumber).map(_.toString)
    val execResult = nativesCache.runCachedProgram(buildConfig, input)
    val lines = execResult.stdOut.lines.toList
    if (validate) {
      val valid = lines.isDefinedAt(1) && lines(1) == "pass"
      if (!valid) {
        val errorMsg = s"""$sortAlgoName failed:
            |- exit code: ${execResult.exitValue}
            |- stdOut: ${execResult.stdOut}
            |- stdErr: ${execResult.stdErr}
          """.stripMargin
        throw new VerificationFailedException(errorMsg)
      }
    }
    val nanosTaken = parseLong(lines.head, 16)
    Duration.fromNanos(nanosTaken)
  }
}

object ClassicSabBenchmark extends NativeComponentsSupport {
  val sabNamePrefix = "/pl/tarsa/sortalgobox/sorts/natives/sab/classic/"

  def components(sortHeader: String): Seq[NativeBuildComponent] = {
    NativeMwc64x.header ++ makeResourceComponents(
      ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
      ("/pl/tarsa/sortalgobox/natives/", "utilities.hpp"),
      ("/pl/tarsa/sortalgobox/natives/agents/", "items_agent.hpp"),
      ("/pl/tarsa/sortalgobox/sorts/natives/common/", "main.cpp"),
      ("/pl/tarsa/sortalgobox/sorts/natives/common/", "items_handler.hpp"),
      (sabNamePrefix, "sabmain.hpp"),
      (sabNamePrefix, "sortalgocommon.hpp"),
      (sabNamePrefix, sortHeader)
    )
  }
}
