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

import akka.actor.{ActorRef, FSM, PoisonPill, Props}
import pl.tarsa.sortalgobox.core.Benchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.BenchmarkData._
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.BenchmarkState._
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor._
import pl.tarsa.sortalgobox.natives.build.NativesCache

import scala.collection.BitSet
import scala.concurrent.duration._

class BenchmarkSuiteActor(workerProps: Props)
    extends FSM[BenchmarkState, BenchmarkData] {

  startWith(Idling, IdlingData(BenchmarksWithResults.empty))

  when(Idling) {
    case Event(StartBenchmarking(benchmarks, listening), IdlingData(_)) =>
      val listenerOpt = {
        val initiator = sender()
        if (listening) Some(initiator) else None
      }
      RunningBenchmarksWithResults.fromBenchmarkSuite(benchmarks) match {
        case Some(runningBenchmarks) =>
          val workerActor = context.actorOf(workerProps)
          goto(WarmingUp) using WarmingUpData(Actors(listenerOpt, workerActor),
                                              runningBenchmarks)
        case None =>
          listenerOpt.foreach(_ ! BenchmarkingFinished)
          stay() using IdlingData(BenchmarksWithResults.empty)
      }
  }

  when(WarmingUp) {
    case Event(workerResponse: BenchmarkWorkerActor.BenchmarkResult,
               WarmingUpData(actors, focusedBenchmarks)) =>
      val response = BenchmarkResult.fromWorkerResult(workerResponse)
      val updatedRunningBenchmarks =
        if (response.isFailed) {
          actors.listenerOpt.foreach(_ ! response)
          focusedBenchmarks.afterFailure
            .recordCurrentBenchmarkResult(warmUpArraySize)
            .withCurrentBenchmarkDisabled
        } else {
          focusedBenchmarks
        }
      updatedRunningBenchmarks.focusedOnNextBenchmark match {
        case Some(nextStepWarmingUpBenchmarks) =>
          goto(WarmingUp) using WarmingUpData(actors,
                                              nextStepWarmingUpBenchmarks)
        case None =>
          updatedRunningBenchmarks.nextRoundOpt match {
            case Some(nextRoundBenchmarks) =>
              goto(Benchmarking) using BenchmarkingData(actors,
                                                        initialBenchmarkingSize,
                                                        nextRoundBenchmarks)
            case None =>
              goto(Idling) using IdlingData(
                updatedRunningBenchmarks.benchmarksWithResults)
          }
      }
  }

  when(Benchmarking) {
    case Event(workerResponse: BenchmarkWorkerActor.BenchmarkResult,
               benchmarkingData: BenchmarkingData) =>
      import benchmarkingData._
      val response = BenchmarkResult.fromWorkerResult(workerResponse)
      val updatedRunningBenchmarks =
        response match {
          case BenchmarkSucceeded(_, _, timeTaken) =>
            val withRecordedSuccess =
              runningBenchmarksWithResults.afterSuccess(timeTaken)
            if (timeTaken >= 1.second) {
              withRecordedSuccess.withCurrentBenchmarkDisabled
            } else {
              withRecordedSuccess
            }
          case BenchmarkFailed(_, _) =>
            runningBenchmarksWithResults.afterFailure.withCurrentBenchmarkDisabled
        }
      if (updatedRunningBenchmarks.currentBenchmarkStats.needsMoreIterations(
            20)) {
        goto(Benchmarking) using benchmarkingData.copy(
          runningBenchmarksWithResults = updatedRunningBenchmarks)
      } else {
        actors.listenerOpt.foreach { listener =>
          listener ! updatedRunningBenchmarks.currentBenchmarkStats.toResult(
            updatedRunningBenchmarks.currentBenchmarkIndex,
            benchmarkingData.currentProblemSize)
        }
        val afterRecordingCurrentBenchmark =
          updatedRunningBenchmarks.recordCurrentBenchmarkResult(
            currentProblemSize)
        afterRecordingCurrentBenchmark.focusedOnNextBenchmark match {
          case Some(nextStepBenchmarksWithResults) =>
            goto(Benchmarking) using benchmarkingData.copy(
              runningBenchmarksWithResults = nextStepBenchmarksWithResults)
          case None =>
            afterRecordingCurrentBenchmark.nextRoundOpt match {
              case Some(nextRoundBenchmarks) =>
                goto(Benchmarking) using BenchmarkingData(
                  actors,
                  (currentProblemSize * 1.3).toInt + 5,
                  nextRoundBenchmarks)
              case None =>
                goto(Idling) using IdlingData(
                  updatedRunningBenchmarks.benchmarksWithResults)
            }
        }
      }
  }

  onTransition {
    case _ -> WarmingUp =>
      nextStateData match {
        case warmingUpData: WarmingUpData =>
          import warmingUpData._
          actors.worker ! runningBenchmarksWithResults
            .currentBenchmarkRequest(warmUpArraySize, validate = true)
        case _ => illegalState()
      }
    case _ -> Benchmarking =>
      nextStateData match {
        case benchmarkingData: BenchmarkingData =>
          import benchmarkingData._
          import runningBenchmarksWithResults._
          actors.worker ! currentBenchmarkRequest(
            currentProblemSize,
            currentBenchmarkStats.iterations == 0)
        case _ => illegalState()
      }
  }

  onTransition {
    case state -> Idling if state != Idling =>
      stateData match {
        case activeStateData: ActiveStateData =>
          val actors = activeStateData.actors
          actors.listenerOpt.foreach(_ ! BenchmarkingFinished)
          actors.worker ! PoisonPill
          NativesCache.cleanup()
        case _ => illegalState()
      }
  }

  initialize()

  private def illegalState(): Nothing =
    throw new IllegalStateException()
}

object BenchmarkSuiteActor {
  val props: Props = {
    val workerProps = Props(new BenchmarkWorkerActor)
    Props(new BenchmarkSuiteActor(workerProps))
  }

  val warmUpArraySize = 12345

  val initialBenchmarkingSize = 4096

  sealed trait BenchmarkState
  object BenchmarkState {
    case object Idling extends BenchmarkState
    sealed trait ActiveState extends BenchmarkState
    case object WarmingUp extends ActiveState
    case object Benchmarking extends ActiveState
  }

  sealed trait BenchmarkData {
    def benchmarksWithResults: BenchmarksWithResults
  }

  object BenchmarkData {

    case class Actors(listenerOpt: Option[ActorRef], worker: ActorRef)

    case class SingleBenchmarkStats(iterations: Int,
                                    totalTime: FiniteDuration,
                                    hasFailed: Boolean) {
      def needsMoreIterations(maxIterations: Int): Boolean =
        !hasFailed && iterations < maxIterations && totalTime < 1.second

      def withSuccess(timeTaken: FiniteDuration): SingleBenchmarkStats =
        copy(iterations = iterations + 1, totalTime = totalTime + timeTaken)

      def withFailure: SingleBenchmarkStats =
        copy(hasFailed = true)

      def toResult(id: Int, problemSize: Int): BenchmarkResult = {
        if (hasFailed) {
          BenchmarkFailed(id, problemSize)
        } else {
          val averageTime = totalTime / iterations
          BenchmarkSucceeded(id, problemSize, averageTime)
        }
      }
    }

    object SingleBenchmarkStats {
      val zero = SingleBenchmarkStats(0, Duration.Zero, hasFailed = false)
    }

    case class BenchmarksWithResults(benchmarksSuite: Seq[Benchmark],
                                     benchmarksResults: Seq[BenchmarkResult])

    object BenchmarksWithResults {
      val empty = BenchmarksWithResults(Nil, Nil)
    }

    case class RunningBenchmarksWithResults(
        benchmarksWithResults: BenchmarksWithResults,
        currentBenchmarkIndex: Int,
        currentBenchmarkStats: SingleBenchmarkStats,
        activeBenchmarks: BitSet) {
      import benchmarksWithResults.{benchmarksSuite, benchmarksResults}

      def currentBenchmarkRequest(
          problemSize: Int,
          validate: Boolean): BenchmarkWorkerActor.BenchmarkRequest = {
        val benchmark = benchmarksSuite(currentBenchmarkIndex)
        BenchmarkWorkerActor.BenchmarkRequest(
          currentBenchmarkIndex,
          problemSize,
          buffer => benchmark.forSize(problemSize, validate, Some(buffer))
        )
      }

      def afterSuccess(
          timeTaken: FiniteDuration): RunningBenchmarksWithResults = {
        copy(
          currentBenchmarkStats = currentBenchmarkStats.withSuccess(timeTaken))
      }

      def afterFailure: RunningBenchmarksWithResults =
        copy(currentBenchmarkStats = currentBenchmarkStats.withFailure)

      def recordCurrentBenchmarkResult(
          problemSize: Int): RunningBenchmarksWithResults = {
        copy(
          benchmarksWithResults = benchmarksWithResults.copy(
            benchmarksResults = benchmarksResults :+ currentBenchmarkStats
              .toResult(currentBenchmarkIndex, problemSize)))
      }

      def withCurrentBenchmarkDisabled: RunningBenchmarksWithResults =
        copy(activeBenchmarks = activeBenchmarks - currentBenchmarkIndex)

      def focusedOnNextBenchmark: Option[RunningBenchmarksWithResults] = {
        activeBenchmarks.from(currentBenchmarkIndex + 1).headOption.map {
          nextBenchmarkIndex =>
            copy(currentBenchmarkIndex = nextBenchmarkIndex,
                 currentBenchmarkStats = SingleBenchmarkStats.zero)
        }
      }

      def nextRoundOpt: Option[RunningBenchmarksWithResults] = {
        activeBenchmarks.headOption.map { firstActiveBenchmarkIndex =>
          copy(currentBenchmarkIndex = firstActiveBenchmarkIndex,
               currentBenchmarkStats = SingleBenchmarkStats.zero)
        }
      }

      def isCurrentBenchmarkActive: Boolean =
        activeBenchmarks.contains(currentBenchmarkIndex)
    }

    object RunningBenchmarksWithResults {
      def fromBenchmarkSuite(
          benchmarks: Seq[Benchmark]): Option[RunningBenchmarksWithResults] = {
        if (benchmarks.isEmpty) {
          None
        } else {
          Some(
            RunningBenchmarksWithResults(
              BenchmarksWithResults(benchmarks, Vector.empty),
              0,
              SingleBenchmarkStats.zero,
              BitSet(benchmarks.indices: _*)))
        }
      }
    }

    case class IdlingData(lastBenchmarkResults: BenchmarksWithResults)
        extends BenchmarkData {
      override def benchmarksWithResults: BenchmarksWithResults =
        lastBenchmarkResults
    }

    sealed abstract class ActiveStateData extends BenchmarkData {
      def actors: Actors
      def runningBenchmarksWithResults: RunningBenchmarksWithResults

      override def benchmarksWithResults: BenchmarksWithResults =
        runningBenchmarksWithResults.benchmarksWithResults
    }

    case class WarmingUpData(
        actors: Actors,
        runningBenchmarksWithResults: RunningBenchmarksWithResults
    ) extends ActiveStateData

    case class BenchmarkingData(
        actors: Actors,
        currentProblemSize: Int,
        runningBenchmarksWithResults: RunningBenchmarksWithResults
    ) extends ActiveStateData
  }

  sealed trait BenchmarkMessage

  case class StartBenchmarking(benchmarkSuite: Seq[Benchmark],
                               listening: Boolean)
      extends BenchmarkMessage

  case object BenchmarkingFinished extends BenchmarkMessage

  sealed abstract class BenchmarkResult(val isFailed: Boolean)
      extends BenchmarkMessage {
    def id: Int
    def size: Int
  }

  case class BenchmarkSucceeded(id: Int, size: Int, timeTaken: FiniteDuration)
      extends BenchmarkResult(isFailed = false)

  case class BenchmarkFailed(id: Int, size: Int)
      extends BenchmarkResult(isFailed = true)

  object BenchmarkResult {
    private val BWA = BenchmarkWorkerActor

    def fromWorkerResult(workerResult: BWA.BenchmarkResult): BenchmarkResult = {
      workerResult match {
        case BWA.BenchmarkSucceeded(id, problemSize, timeTaken) =>
          BenchmarkSucceeded(id, problemSize, timeTaken)
        case BWA.BenchmarkFailed(id, problemSize) =>
          BenchmarkFailed(id, problemSize)
      }
    }
  }
}
