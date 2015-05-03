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

import pl.tarsa.sortalgobox.opencl.{GpuBitonicSort, CpuBitonicSort}
import pl.tarsa.sortalgobox.sorts.bitonic.BitonicSort
import pl.tarsa.sortalgobox.sorts.common.SortAlgorithm

import scala.util.Random

abstract class Benchmark {
  def sorts: List[(String, SortAlgorithm[Int])]

  def isSorted(ints: Array[Int]): Boolean = {
    ints.indices.tail.forall(i => ints(i - 1) <= ints(i))
  }

  def start(): Unit = {
    val generator = new Random(5)
    val activeSorts = Array.fill[Boolean](sorts.length)(true)
    for (size <- Iterator.iterate(1234)(x => (x * 1.3).toInt + 5)
      .takeWhile(_ < 123456789)) {
      newSize(size)
      val original = Array.fill[Int](size)(generator.nextInt())
      val buffer = Array.ofDim[Int](size)
      for ((sortAlgo, sortId) <- sorts.map(_._2).zipWithIndex
           if activeSorts(sortId)) {
        var totalTime = 0L
        var iterations = 0
        while (iterations < 20 && totalTime < 1000) {
          System.arraycopy(original, 0, buffer, 0, size)
          val currentStartTime = System.currentTimeMillis()
          sortAlgo.sort(buffer)
          val currentTotalTime = System.currentTimeMillis() - currentStartTime
          assert(isSorted(buffer))
          totalTime += currentTotalTime
          iterations += 1
        }
        val time = totalTime.toDouble / iterations
        if (time > 1000) {
          activeSorts(sortId) = false
        }
        newData(sortId, time)
      }
    }
  }

  def warmUp(): Unit = {
    val ints = Array(5, 3, 2, 8)
    for (_ <- 0 to 5) {
      sorts.foreach { case (_, sortAlgo) =>
        sortAlgo.sort(ints.clone())
      }
    }
  }

  def newSize(size: Int): Unit

  def newData(sortId: Int, time: Double): Unit
}
