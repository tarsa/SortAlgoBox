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
package pl.tarsa.sortalgobox.fxgui

import javafx.scene.chart.XYChart.Data

import akka.actor.{ActorSystem, Props}
import pl.tarsa.sortalgobox.core.Benchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.BenchmarkSucceeded

import scalafx.application.{JFXApp, Platform}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Side
import scalafx.scene.Scene
import scalafx.scene.chart.{CategoryAxis, LineChart, NumberAxis, XYChart}

class FxBenchmarkGui(benchmarks: Seq[Benchmark]) extends JFXApp {

  val seriesWithBuffers: Seq[(XYChart.Series[String, Number],
                              ObservableBuffer[Data[String, Number]])] =
    benchmarks.map { benchmark =>
      val buffer = ObservableBuffer[Data[String, Number]]()
      (new XYChart.Series[String, Number](
         XYChart.Series(benchmark.name, buffer)),
       buffer)
    }

  stage = new JFXApp.PrimaryStage {
    title = "Sorting Algorithms Benchmark"
    scene = new Scene {
      root = new LineChart(CategoryAxis("Size"), NumberAxis("Time (Log) ms")) {
        title = "Sorting Algorithms Results"
        legendSide = Side.Right
        data = seriesWithBuffers.map(_._1.delegate)
      }
    }
  }

  def updateConsumer(successResult: BenchmarkSucceeded): Unit = {
    import successResult.{id, size, timeTaken}
    Platform.runLater {
      val safeTimeMillis = Math.max(1.0, timeTaken.toNanos / 1e6)
      val sizeString = size.toString
      seriesWithBuffers(id)._2 +=
        XYChart.Data[String, Number](sizeString, Math.log10(safeTimeMillis))
    }
  }

  var actorSystem: ActorSystem = _

  def startUp(): Unit = {
    actorSystem = ActorSystem("fx-benchmark-suite")
    val benchmarkSuiteActor = actorSystem.actorOf(BenchmarkSuiteActor.props)
    val fxBenchmarkActorProps = Props(
      new FxBenchmarkActor(updateConsumer, benchmarks, benchmarkSuiteActor))
    actorSystem.actorOf(fxBenchmarkActorProps)
  }

  override def stopApp(): Unit =
    actorSystem.terminate()

  startUp()
}
