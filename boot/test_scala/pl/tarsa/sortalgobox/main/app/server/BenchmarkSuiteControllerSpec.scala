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
package pl.tarsa.sortalgobox.main.app.server

import akka.testkit.TestProbe
import pl.tarsa.sortalgobox.core.NoOpBenchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.BenchmarkData.BenchmarksWithResults
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.{
  BenchmarkFailed,
  BenchmarkResult,
  BenchmarkSucceeded,
  CurrentBenchmarkResults,
  GetCurrentBenchmarkResults,
  StartBenchmarking,
  warmUpArraySize
}
import pl.tarsa.sortalgobox.tests.HttpRouteSpecBase

import scala.concurrent.Future
import scala.concurrent.duration._

class BenchmarkSuiteControllerSpec extends HttpRouteSpecBase {
  typeBehavior[BenchmarkSuiteController]

  it must "start benchmarking" in {
    val suiteProbe = TestProbe()
    val suite = Seq(null)
    val controller = new BenchmarkSuiteController(suite, suiteProbe.ref, null)
    Get("/start") ~> controller.route ~> {
      val trigger = suiteProbe.expectMsgType[StartBenchmarking]
      assert(trigger.benchmarkSuite eq suite)
      trigger.listening mustBe false
      check { responseAs[String] mustBe "triggered" }
    }
  }

  it must "print empty status when idle" in {
    checkPrintedStatus()()() {
      """Status: idle
      """.stripMargin.trim
    }
  }

  it must "print empty status when busy" in {
    checkPrintedStatus()()(benchmarkingInProgress = true) {
      """Status: active
      """.stripMargin.trim
    }
  }

  it must "print benchmarks results" in {
    val benchmarksResults =
      Seq(BenchmarkFailed(0, warmUpArraySize),
          BenchmarkSucceeded(1, warmUpArraySize, 5.millis),
          BenchmarkFailed(1, 123))
    checkPrintedStatus("a", "b")(benchmarksResults: _*)() {
      """Status: idle
        |        FAILED a
        |      5,000 ms b
        |New size: 123
        |        FAILED b
      """.stripMargin.trim
    }
  }

  it must "shutdown" in {
    var shutdownActionCalled = false
    val shutdownAction = () => {
      shutdownActionCalled = true
      Future.unit
    }
    val controller = new BenchmarkSuiteController(null, null, shutdownAction)
    Get("/shutdown") ~> controller.route ~> {
      shutdownActionCalled mustBe true
      check { responseAs[String] mustBe "" }
    }
  }

  def checkPrintedStatus(benchmarksNames: String*)(
      benchmarksResults: BenchmarkResult*)(
      benchmarkingInProgress: Boolean = false)(expectedResult: String): Unit = {
    val benchmarksWithResults = BenchmarksWithResults(
      benchmarksNames.map(name => NoOpBenchmark(name, null)),
      benchmarksResults
    )
    val suiteProbe = TestProbe()
    val controller =
      new BenchmarkSuiteController(null, suiteProbe.ref, null)
    Get("/status") ~> controller.route ~> {
      suiteProbe.expectMsg(GetCurrentBenchmarkResults)
      suiteProbe.reply(
        CurrentBenchmarkResults(benchmarksWithResults, benchmarkingInProgress))
      check { responseAs[String] mustBe expectedResult }
    }
  }
}
