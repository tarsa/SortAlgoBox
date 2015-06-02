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

import pl.tarsa.sortalgobox.natives.NativeStdSort
import pl.tarsa.sortalgobox.natives.sab._
import pl.tarsa.sortalgobox.opencl._
import pl.tarsa.sortalgobox.random.Mwc64x
import pl.tarsa.sortalgobox.sorts.bitonic.BitonicSort
import pl.tarsa.sortalgobox.sorts.common._
import pl.tarsa.sortalgobox.standard.{ParallelArraysSort, SequentialArraysSort}

object BenchmarksConfigurations {
  val plainSorts: List[(String, SortAlgorithm[Int])] = List(
    "BitonicSort" -> new BitonicSort[Int],
    "SequentialArraysSort" -> new SequentialArraysSort,
    "ParallelArraySort" -> new ParallelArraysSort)

  val measuredSorts: List[(String, MeasuredSortAlgorithm[Int])] = List(
    "CpuBitonicSort" -> CpuBitonicSort,
    "GpuBitonicSort" -> GpuBitonicSort,
    "CpuQuickSort" -> CpuQuickSort) ::: plainSorts.map {
    case (name, sortAlgorithm) =>
      name -> MeasuringSortAlgorithmWrapper(sortAlgorithm)
  }

  val nativeBenchmarks: List[(String, Benchmark)] = List(
    "NativeSabHeapBinaryAheadSimpleVariantA" ->
      new NativeSabHeapBinaryAheadSimpleVariantA(),
    "NativeSabHeapBinaryAheadSimpleVariantB" ->
      new NativeSabHeapBinaryAheadSimpleVariantB(),
    "NativeSabHeapBinaryCached" ->
      new NativeSabHeapBinaryCached(),
    "NativeSabHeapBinaryCascadingVariantA" ->
      new NativeSabHeapBinaryCascadingVariantA(),
    "NativeSabHeapBinaryCascadingVariantB" ->
      new NativeSabHeapBinaryCascadingVariantB(),
    "NativeSabHeapBinaryCascadingVariantC" ->
      new NativeSabHeapBinaryCascadingVariantC(),
    "NativeSabHeapBinaryCascadingVariantD" ->
      new NativeSabHeapBinaryCascadingVariantD(),
    "NativeSabHeapBinaryClusteredVariantA" ->
      new NativeSabHeapBinaryClusteredVariantA(),
    "NativeSabHeapBinaryClusteredVariantB" ->
      new NativeSabHeapBinaryClusteredVariantB(),
    "NativeSabHeapBinaryOneBasedVariantA" ->
      new NativeSabHeapBinaryOneBasedVariantA(),
    "NativeSabHeapBinaryOneBasedVariantB" ->
      new NativeSabHeapBinaryOneBasedVariantB(),
    "NativeSabHeapHybrid" ->
      new NativeSabHeapHybrid(),
    "NativeSabHeapHybridCascading" ->
      new NativeSabHeapHybridCascading(),
    "NativeSabHeapQuaternaryCascadingVariantA" ->
      new NativeSabHeapQuaternaryCascadingVariantA(),
    "NativeSabHeapQuaternaryVariantA" ->
      new NativeSabHeapQuaternaryVariantA(),
    "NativeSabHeapQuaternaryVariantB" ->
      new NativeSabHeapQuaternaryVariantB(),
    "NativeSabHeapSimdDwordCascadingVariantB" ->
      new NativeSabHeapSimdDwordCascadingVariantB(),
    "NativeSabHeapSimdDwordCascadingVariantC" ->
      new NativeSabHeapSimdDwordCascadingVariantC(),
    "NativeSabHeapSimdDwordVariantB" ->
      new NativeSabHeapSimdDwordVariantB(),
    "NativeSabHeapSimdDwordVariantC" ->
      new NativeSabHeapSimdDwordVariantC(),
    "NativeSabHeapTernaryCascadingVariantA" ->
      new NativeSabHeapTernaryCascadingVariantA(),
    "NativeSabHeapTernaryClusteredVariantA" ->
      new NativeSabHeapTernaryClusteredVariantA(),
    "NativeSabHeapTernaryClusteredVariantB" ->
      new NativeSabHeapTernaryClusteredVariantB(),
    "NativeSabHeapTernaryOneBasedVariantA" ->
      new NativeSabHeapTernaryOneBasedVariantA(),
    "NativeSabHeapTernaryOneBasedVariantB" ->
      new NativeSabHeapTernaryOneBasedVariantB(),
    "NativeSabQuickRandomized" ->
      new NativeSabQuickRandomized(),
    "NativeStdSort" -> new NativeStdSort)

  val benchmarks: List[(String, Benchmark)] = nativeBenchmarks :::
    measuredSorts.map {
      case (name, sortAlgorithm) => (name, sortToBenchmark(sortAlgorithm))
    }

  def sortToBenchmark(sort: MeasuredSortAlgorithm[Int]) = new Benchmark {
    override def forSize(n: Int, validate: Boolean,
      buffer: Option[Array[Int]]): Long = {

      val array = buffer.getOrElse(Array.ofDim[Int](n))
      val rng = new Mwc64x
      array.indices.foreach(array(_) = rng.nextInt())
      val totalTime = sort.sort(array)
      if (validate) {
        assert(isSorted(array))
      }
      totalTime
    }
  }

  def isSorted(ints: Array[Int]): Boolean = {
    ints.indices.tail.forall(i => ints(i - 1) <= ints(i))
  }
}
