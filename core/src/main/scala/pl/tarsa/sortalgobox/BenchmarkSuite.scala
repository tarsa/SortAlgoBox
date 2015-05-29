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
package pl.tarsa.sortalgobox

import pl.tarsa.sortalgobox.natives.NativesCache
import pl.tarsa.sortalgobox.random.Mwc64x

abstract class BenchmarkSuite {
  def benchmarks: List[(String, Benchmark)]

  def run(): Unit = {
    warmUp()
    val generator = new Mwc64x
    val activeBenchmarks = Array.fill[Boolean](benchmarks.length)(true)
    for (size <- Iterator.iterate(1234)(x => (x * 1.3).toInt + 5)
      .takeWhile(_ < 123456789 && activeBenchmarks.exists(identity))) {
      newSize(size)
      val buffer = Array.ofDim[Int](size)
        for ((benchmark, sortId) <- benchmarks.map(_._2).zipWithIndex
           if activeBenchmarks(sortId)) {
        var totalTime = 0L
        var iterations = 0
        while (iterations < 20 && totalTime < 1000) {
          val currentTotalTime = benchmark.forSize(size, Some(buffer))
          totalTime += currentTotalTime
          iterations += 1
        }
        val time = totalTime.toDouble / iterations
        if (time > 1000) {
          activeBenchmarks(sortId) = false
        }
        newData(sortId, time)
      }
    }
    NativesCache.cleanup()
  }

  def warmUp(): Unit = {
    benchmarks.foreach(_._2.forSize(1234567))
  }

  def newSize(size: Int): Unit

  def newData(sortId: Int, time: Double): Unit
}
