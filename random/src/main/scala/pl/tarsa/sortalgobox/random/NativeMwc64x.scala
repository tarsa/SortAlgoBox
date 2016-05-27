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
package pl.tarsa.sortalgobox.random

import java.io.PrintStream
import java.util.Scanner

import pl.tarsa.sortalgobox.natives.build._

class NativeMwc64x(nativesCache: NativesCache = NativesCache) {
  def generate(n: Int): Array[Int] = {
    val buildConfig = NativeBuildConfig(NativeMwc64x.sources, "mwc64x.cpp")
    val generatorProcess = nativesCache.runCachedProgram(buildConfig)
    val pipeTo = new PrintStream(generatorProcess.getOutputStream)
    pipeTo.println(n)
    pipeTo.flush()
    val pipeFrom = new Scanner(generatorProcess.getInputStream)
    val result = Array.fill[Int](n)(pipeFrom.nextLong(16).toInt)
    generatorProcess.waitFor()
    result
  }
}

object NativeMwc64x extends NativeComponentsSupport {
  val header = makeResourceComponents(
    ("/pl/tarsa/sortalgobox/random/mwc64x/native/", "mwc64x.hpp"))

  val sources = header ++ makeResourceComponents(
    ("/pl/tarsa/sortalgobox/random/mwc64x/native/", "mwc64x.cpp"))
}
