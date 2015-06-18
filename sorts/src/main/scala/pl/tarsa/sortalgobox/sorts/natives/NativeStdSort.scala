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
package pl.tarsa.sortalgobox.sorts.natives

import java.io.PrintStream
import java.util.Scanner

import pl.tarsa.sortalgobox.core.NativeBenchmark
import pl.tarsa.sortalgobox.natives._
import pl.tarsa.sortalgobox.random.NativeMwc64x

class NativeStdSort(nativesCache: NativesCache = NativesCache)
  extends NativeBenchmark {

  val name = getClass.getSimpleName

  val buildConfig = NativeBuildConfig(NativeStdSort.components, "main.cpp")

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

object NativeStdSort extends NativeComponentsSupport {
  val components = NativeMwc64x.header ++ makeComponents(
    ("/pl/tarsa/sortalgobox/sorts/natives/std__sort/", "main.cpp"))
}
