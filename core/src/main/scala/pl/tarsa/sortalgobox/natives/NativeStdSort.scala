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

import java.io.PrintStream
import java.util.Scanner

import pl.tarsa.sortalgobox.Benchmark

class NativeStdSort extends Benchmark {
  override def forSize(n: Int, buffer: Option[Array[Int]]): Int = {
    val generatorProcess = NativesCache.runCachedProgram(
      "/pl/tarsa/sortalgobox/natives/std__sort/main.cpp")
    val pipeTo = new PrintStream(generatorProcess.getOutputStream)
    pipeTo.println(n)
    pipeTo.flush()
    val pipeFrom = new Scanner(generatorProcess.getInputStream)
    val result = pipeFrom.nextLong(16).toInt
    generatorProcess.waitFor()
    result
  }
}
