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

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import pl.tarsa.sortalgobox.core.Benchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.{
  BenchmarkFailed,
  BenchmarkSucceeded,
  CurrentBenchmarkResults,
  GetCurrentBenchmarkResults,
  StartBenchmarking
}

import scala.concurrent.Future
import scala.concurrent.duration._

class BenchmarkSuiteController(benchmarks: Seq[Benchmark],
                               benchmarkSuiteActor: ActorRef,
                               shutdownAction: () => Future[Unit]) {
  import akka.http.scaladsl.server.Directives._

  def routes(head: Route, tail: Route*): Route =
    tail.foldLeft(head)(_ ~ _)

  val route: Route = {
    routes(
      pathSingleSlash {
        get {
          complete {
            val content = IndexPage.render()
            HttpEntity(ContentTypes.`text/html(UTF-8)`, content)
          }
        }
      },
      path("start") {
        get {
          complete {
            benchmarkSuiteActor ! StartBenchmarking(benchmarks,
                                                    listening = false)
            "triggered"
          }
        }
      },
      path("status") {
        get {
          implicit val timeout: Timeout = Timeout(1.second)
          onComplete(benchmarkSuiteActor ? GetCurrentBenchmarkResults) {
            case util.Success(results: CurrentBenchmarkResults) =>
              complete(printReport(results))
            case wrongResult =>
              complete(wrongResult.toString)
          }
        }
      },
      path("shutdown") {
        get {
          onComplete(shutdownAction())(_ => complete(""))
        }
      }
    )
  }

  private def printReport(results: CurrentBenchmarkResults) = {
    val status = if (results.benchmarkingInProgress) "active" else "idle"
    val header = s"Status: $status"
    val (rows, _) = results.benchmarksWithResults.benchmarksResults
      .foldLeft((Vector.empty[String], BenchmarkSuiteActor.warmUpArraySize)) {
        case ((previousRows, previousSize), benchmarkResult) =>
          val newSizeMsgOpt =
            if (benchmarkResult.size == previousSize) None
            else Some(f"New size: ${benchmarkResult.size}%,d")
          val benchmarkName =
            results.benchmarksWithResults
              .benchmarksSuite(benchmarkResult.id)
              .name
          val timingStr =
            benchmarkResult match {
              case BenchmarkSucceeded(_, _, timeTaken) =>
                f"${timeTaken.toMicros / 1e3}%,.3f ms"
              case BenchmarkFailed(_, _) =>
                "FAILED"
            }
          val row = f"$timingStr%14s $benchmarkName"
          val newRows = Seq(newSizeMsgOpt, Some(row)).flatten
          (previousRows ++ newRows, benchmarkResult.size)
      }
    (header +: rows).mkString("\n")
  }
}
