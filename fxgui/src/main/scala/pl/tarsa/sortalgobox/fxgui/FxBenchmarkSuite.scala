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
package pl.tarsa.sortalgobox.fxgui

import javafx.scene.chart.XYChart.Data

import pl.tarsa.sortalgobox.core._

import scala.concurrent.Future
import scalafx.application.{JFXApp, Platform}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.Scene
import scalafx.scene.chart.{CategoryAxis, LineChart, NumberAxis, XYChart}

class FxBenchmarkSuite(val benchmarks: Seq[Benchmark])
  extends BenchmarkSuite with JFXApp {

  val seriesWithBuffers = benchmarks.map { benchmark: Benchmark =>
    val buffer = ObservableBuffer[Data[String, Number]]()
    (new XYChart.Series[String, Number](
      XYChart.Series(benchmark.name, buffer)), buffer)
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

  override def newData(sortId: Int, result: BenchmarkResult): Unit = {
    result match {
      case BenchmarkSucceeded(timeInMs) =>
        val sizeString = size.toString
        Platform.runLater {
          val safeTime = Math.max(1.0, timeInMs)
          seriesWithBuffers(sortId)._2 +=
            XYChart.Data[String, Number](sizeString, Math.log10(safeTime))
        }
      case _ =>
    }
  }

  Future(run())(scala.concurrent.ExecutionContext.Implicits.global)
}
