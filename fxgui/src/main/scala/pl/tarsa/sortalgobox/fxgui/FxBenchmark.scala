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
package pl.tarsa.sortalgobox.fxgui

import javafx.scene.chart.XYChart.Data

import pl.tarsa.sortalgobox.Benchmark
import pl.tarsa.sortalgobox.opencl.{CpuBitonicSort, GpuBitonicSort}
import pl.tarsa.sortalgobox.sorts.bitonic.BitonicSort

import scala.concurrent.Future
import scalafx.application.{Platform, JFXApp}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.Scene
import scalafx.scene.chart.{NumberAxis, CategoryAxis, LineChart, XYChart}

object FxBenchmark extends Benchmark with JFXApp {
  val sorts = List(
    "BitonicSort" -> new BitonicSort[Int],
    "CpuBitonicSort" -> CpuBitonicSort,
    "GpuBitonicSort" -> GpuBitonicSort)

  val seriesWithBuffers = sorts.map { case (name, _) =>
    val buffer = ObservableBuffer[Data[String, Number]]()
    (new XYChart.Series[String, Number](XYChart.Series(name, buffer)), buffer)
  }

  stage = new JFXApp.PrimaryStage {
    title = "Sorting Algorithms Benchmark"
    scene = new Scene {
      root = new LineChart(CategoryAxis("Size"), NumberAxis("Time (Log) ms")) {
        title = "Sorting Algorithms Results"
        legendSide = Side.RIGHT
        data = seriesWithBuffers.map(_._1.delegate)
      }
    }
  }

  var size = 0

  override def newSize(size: Int): Unit = {
    this.size = size
  }

  override def newData(sortId: Int, time: Double): Unit = {
    val sizeString = size.toString
    Platform.runLater {
      val safeTime = Math.max(1.0, time)
      seriesWithBuffers(sortId)._2 +=
        XYChart.Data[String, Number](sizeString, Math.log10(safeTime))
    }
  }

  Future(start())(scala.concurrent.ExecutionContext.Implicits.global)
}
