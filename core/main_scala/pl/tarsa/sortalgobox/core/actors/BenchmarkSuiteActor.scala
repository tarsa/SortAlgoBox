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
package pl.tarsa.sortalgobox.core.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import pl.tarsa.sortalgobox.core.Benchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor._
import pl.tarsa.sortalgobox.natives.build.NativesCache

import scala.concurrent.Await
import scala.concurrent.duration._

class BenchmarkSuiteActor(workerProps: Props) extends Actor {
  override def receive: Receive = {
    case StartBenchmarking(benchmarks) =>
      // context.actorOf will get stuck on child failures
      // because we're blocking in this actor
      val workerActor = context.system.actorOf(workerProps)
      val initiator = sender()
      val activeBenchmarks = Array.fill[Boolean](benchmarks.length)(true)
      benchmarks.zipWithIndex.foreach {
        case (benchmark, benchmarkId) =>
          val result = runBenchmark(workerActor,
                                    benchmarkId,
                                    warmUpArraySize,
                                    validate = true,
                                    benchmark)
          if (result.isFailed) {
            initiator ! result
            activeBenchmarks(benchmarkId) = false
          }
      }
      for (size <- Iterator
             .iterate(4096)(x => (x * 1.3).toInt + 5)
             .takeWhile(_ < 123456789 && activeBenchmarks.contains(true))) {
        for ((benchmark, benchmarkId) <- benchmarks.zipWithIndex
             if activeBenchmarks(benchmarkId)) {
          var totalTime = Duration.Zero
          var iterations = 0
          while (activeBenchmarks(benchmarkId) && iterations < 20 &&
                 totalTime < 1.second) {
            val result = runBenchmark(workerActor,
                                      benchmarkId,
                                      size,
                                      validate = iterations == 0,
                                      benchmark)
            result match {
              case BenchmarkSucceeded(_, _, timeTaken) =>
                totalTime += timeTaken
                iterations += 1
              case failure: BenchmarkFailed =>
                initiator ! failure
                activeBenchmarks(benchmarkId) = false
            }
          }
          if (iterations > 0) {
            val averageTime = totalTime / iterations
            initiator ! BenchmarkSucceeded(benchmarkId, size, averageTime)
            if (averageTime > 1.second) {
              activeBenchmarks(benchmarkId) = false
            }
          }
        }
      }
      NativesCache.cleanup()
      workerActor ! PoisonPill
      initiator ! BenchmarkingFinished
  }

  def runBenchmark(workerActor: ActorRef,
                   index: Int,
                   bufferSize: Int,
                   validate: Boolean,
                   benchmark: Benchmark): BenchmarkResult = {
    implicit val timeout: Timeout = 21474835.seconds
    val responseFut = workerActor ? BenchmarkWorkerActor.BenchmarkRequest(
      index,
      bufferSize,
      buffer => benchmark.forSize(bufferSize, validate, Some(buffer))
    )
    Await.result(responseFut, timeout.duration) match {
      case response: BenchmarkWorkerActor.BenchmarkResult =>
        BenchmarkResult.fromWorkerResult(response, bufferSize)
    }
  }
}

object BenchmarkSuiteActor {
  val props: Props = {
    val workerProps = Props(new BenchmarkWorkerActor)
    Props(new BenchmarkSuiteActor(workerProps))
  }

  val warmUpArraySize = 12345

  val initialBenchmarkingSize = 4096

  case class StartBenchmarking(benchmarkSuite: Seq[Benchmark])

  case object BenchmarkingFinished

  sealed abstract class BenchmarkResult(val isFailed: Boolean) {
    def id: Int
    def size: Int
  }

  case class BenchmarkSucceeded(id: Int, size: Int, timeTaken: FiniteDuration)
      extends BenchmarkResult(isFailed = false)

  case class BenchmarkFailed(id: Int, size: Int)
      extends BenchmarkResult(isFailed = true)

  object BenchmarkResult {
    def fromWorkerResult(workerResult: BenchmarkWorkerActor.BenchmarkResult,
                         size: Int): BenchmarkResult = {
      workerResult match {
        case BenchmarkWorkerActor.BenchmarkSucceeded(id, timeTaken) =>
          BenchmarkSucceeded(id, size, timeTaken)
        case BenchmarkWorkerActor.BenchmarkFailed(id) =>
          BenchmarkFailed(id, size)
      }
    }
  }
}
