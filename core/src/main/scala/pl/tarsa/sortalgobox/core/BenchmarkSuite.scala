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
package pl.tarsa.sortalgobox.core

import pl.tarsa.sortalgobox.core.exceptions.VerificationFailedException
import pl.tarsa.sortalgobox.natives.build.NativesCache

abstract class BenchmarkSuite {
  def benchmarks: Seq[Benchmark]

  val nanosecondsInMillisecond = 1e6
  val nanosecondsInSecond = 1e9

  def run(): Unit = {
    val activeBenchmarks = Array.fill[Boolean](benchmarks.length)(true)
    warmUp(activeBenchmarks)
    for (size <- Iterator.iterate(4096)(x => (x * 1.3).toInt + 5)
      .takeWhile(_ < 123456789 && activeBenchmarks.exists(identity))) {
      newSize(size)
      val buffer = Array.ofDim[Int](size)
      for ((benchmark, benchmarkId) <- benchmarks.zipWithIndex
           if activeBenchmarks(benchmarkId)) {
        try {
          var totalTime = 0L
          var iterations = 0
          while (iterations < 20 && totalTime < nanosecondsInSecond) {
            val currentTotalTime = benchmark.forSize(size,
              validate = iterations == 0, Some(buffer))
            totalTime += currentTotalTime
            iterations += 1
          }
          val timeInMs = totalTime / (iterations * nanosecondsInMillisecond)
          if (timeInMs > 1000) {
            activeBenchmarks(benchmarkId) = false
          }
          newData(benchmarkId, BenchmarkSucceeded(timeInMs))
        } catch {
          case e: VerificationFailedException =>
            activeBenchmarks(benchmarkId) = false
            newData(benchmarkId, BenchmarkFailed(0))
        }
      }
    }
    NativesCache.cleanup()
  }

  def warmUp(activeBenchmarks: Array[Boolean]): Unit = {
    benchmarks.zipWithIndex.foreach { case (benchmark, benchmarkId) =>
      try {
        benchmark.forSize(12345, validate = true)
      } catch {
        case e: VerificationFailedException =>
          activeBenchmarks(benchmarkId) = false
          Console.err.println(Console.RED + "FAILED" + Console.RESET +
            " benchmark: " + benchmark.name)
      }
    }
  }

  def newSize(size: Int): Unit

  def newData(benchmarkId: Int, result: BenchmarkResult): Unit
}
