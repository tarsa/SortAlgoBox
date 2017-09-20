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

import akka.actor.{Actor, ActorRef, FSM, PoisonPill, Props}
import pl.tarsa.sortalgobox.core.Benchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.BenchmarkData._
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.BenchmarkState._
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor._
import pl.tarsa.sortalgobox.natives.build.NativesCache

import scala.collection.BitSet
import scala.concurrent.duration._

class BenchmarkSuiteActor(workerProps: Props)
    extends Actor
    with FSM[BenchmarkState, BenchmarkData] {

  startWith(Idle, NoData)

  when(Idle) {
    case Event(StartBenchmarking(benchmarks), NoData) =>
      val initiator = sender()
      if (benchmarks.isEmpty) {
        initiator ! BenchmarkingFinished
        stay()
      } else {
        val workerActor = context.actorOf(workerProps)
        val focusedBenchmarks =
          BenchmarksWithFocus.fromBenchmarkSuite(benchmarks)
        val warmUpData =
          WarmUpData(Actors(initiator, workerActor), focusedBenchmarks)
        workerActor ! focusedBenchmarks.currentBenchmarkRequest(warmUpArraySize,
                                                                validate = true)
        goto(WarmingUp) using warmUpData
      }
  }

  when(WarmingUp) {
    case Event(workerResponse: BenchmarkWorkerActor.BenchmarkResult,
               WarmUpData(actors, focusedBenchmarks)) =>
      val response =
        BenchmarkResult.fromWorkerResult(workerResponse, warmUpArraySize)
      val updatedFocusedBenchmarks =
        if (response.isFailed) {
          actors.initiator ! response
          focusedBenchmarks.withCurrentBenchmarkDisabled
        } else {
          focusedBenchmarks
        }
      updatedFocusedBenchmarks.focusedOnNextBenchmark match {
        case Some(nextWarmUpBenchmarksFocus) =>
          actors.worker ! nextWarmUpBenchmarksFocus.currentBenchmarkRequest(
            warmUpArraySize,
            validate = true)
          stay() using WarmUpData(actors, nextWarmUpBenchmarksFocus)
        case None =>
          if (updatedFocusedBenchmarks.activeBenchmarks.isEmpty) {
            actors.initiator ! BenchmarkingFinished
            actors.worker ! PoisonPill
            NativesCache.cleanup()
            goto(Idle) using NoData
          } else {
            val benchmarkingData =
              BenchmarkingData(actors,
                               4096,
                               SingleBenchmarkInfo(0, Duration.Zero),
                               updatedFocusedBenchmarks.rewind)
            actors.worker ! benchmarkingData.focusedBenchmarks
              .currentBenchmarkRequest(4096, validate = true)
            goto(Benchmarking) using benchmarkingData
          }
      }
  }

  when(Benchmarking) {
    case Event(workerResponse: BenchmarkWorkerActor.BenchmarkResult,
               BenchmarkingData(actors,
                                currentProblemSize,
                                currentBenchmarkInfo,
                                focusedBenchmarks)) =>
      val response =
        BenchmarkResult.fromWorkerResult(workerResponse, currentProblemSize)
      val (updatedFocusedBenchmarks, updatedCurrentBenchmarkInfo) =
        response match {
          case BenchmarkSucceeded(_, _, timeTaken) =>
            val updatedBenchmarkInfo =
              SingleBenchmarkInfo(currentBenchmarkInfo.iterations + 1,
                                  currentBenchmarkInfo.totalTime + timeTaken)
            if (updatedBenchmarkInfo.averageTimeOpt.get >= 1.second) {
              actors.initiator ! BenchmarkSucceeded(
                focusedBenchmarks.currentBenchmarkIndex,
                currentProblemSize,
                updatedBenchmarkInfo.averageTimeOpt.get) // FIXME Option.get
              (focusedBenchmarks.withCurrentBenchmarkDisabled,
               updatedBenchmarkInfo)
            } else {
              (focusedBenchmarks, updatedBenchmarkInfo)
            }
          case BenchmarkFailed(_, _) =>
            actors.initiator ! response
            (focusedBenchmarks.withCurrentBenchmarkDisabled,
             currentBenchmarkInfo)
        }
      if (updatedFocusedBenchmarks.isCurrentBenchmarkActive &&
          !updatedCurrentBenchmarkInfo.hasEnoughIterations) {
        actors.worker ! updatedFocusedBenchmarks.currentBenchmarkRequest(
          currentProblemSize,
          validate = false)
        stay() using BenchmarkingData(actors,
                                      currentProblemSize,
                                      updatedCurrentBenchmarkInfo,
                                      updatedFocusedBenchmarks)
      } else {
        if (updatedFocusedBenchmarks.isCurrentBenchmarkActive) {
          actors.initiator ! BenchmarkSucceeded(
            updatedFocusedBenchmarks.currentBenchmarkIndex,
            currentProblemSize,
            updatedCurrentBenchmarkInfo.averageTimeOpt.get) // FIXME Option.get
        }
        updatedFocusedBenchmarks.focusedOnNextBenchmark match {
          case Some(nextWarmUpBenchmarksFocus) =>
            actors.worker ! nextWarmUpBenchmarksFocus.currentBenchmarkRequest(
              currentProblemSize,
              validate = true)
            stay() using BenchmarkingData(actors,
                                          currentProblemSize,
                                          SingleBenchmarkInfo(0, Duration.Zero),
                                          nextWarmUpBenchmarksFocus)
          case None =>
            if (updatedFocusedBenchmarks.activeBenchmarks.isEmpty) {
              actors.initiator ! BenchmarkingFinished
              actors.worker ! PoisonPill
              NativesCache.cleanup()
              goto(Idle) using NoData
            } else {
              val rewoundFocusedBenchmarks = updatedFocusedBenchmarks.rewind
              val nextProblemSize = (currentProblemSize * 1.3).toInt + 5
              actors.worker ! rewoundFocusedBenchmarks.currentBenchmarkRequest(
                nextProblemSize,
                validate = true)
              stay() using BenchmarkingData(actors,
                                            nextProblemSize,
                                            SingleBenchmarkInfo(0,
                                                                Duration.Zero),
                                            rewoundFocusedBenchmarks)
            }
        }
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

  sealed trait BenchmarkState
  object BenchmarkState {
    case object Idle extends BenchmarkState
    case object WarmingUp extends BenchmarkState
    case object Benchmarking extends BenchmarkState
  }

  sealed trait BenchmarkData

  object BenchmarkData {

    case class Actors(initiator: ActorRef, worker: ActorRef)

    case class BenchmarksWithFocus(benchmarkSuite: Seq[Benchmark],
                                   currentBenchmarkIndex: Int,
                                   activeBenchmarks: BitSet) {
      def currentBenchmarkRequest(
          bufferSize: Int,
          validate: Boolean): BenchmarkWorkerActor.BenchmarkRequest = {
        val benchmark = benchmarkSuite(currentBenchmarkIndex)
        BenchmarkWorkerActor.BenchmarkRequest(
          currentBenchmarkIndex,
          bufferSize,
          buffer => benchmark.forSize(bufferSize, validate, Some(buffer))
        )
      }

      def withCurrentBenchmarkDisabled: BenchmarksWithFocus =
        copy(activeBenchmarks = activeBenchmarks - currentBenchmarkIndex)

      def focusedOnNextBenchmark: Option[BenchmarksWithFocus] = {
        activeBenchmarks.from(currentBenchmarkIndex + 1).headOption.map {
          nextBenchmarkIndex =>
            copy(currentBenchmarkIndex = nextBenchmarkIndex)
        }
      }

      def rewind: BenchmarksWithFocus =
        copy(currentBenchmarkIndex = activeBenchmarks.min)

      def isCurrentBenchmarkActive: Boolean =
        activeBenchmarks.contains(currentBenchmarkIndex)
    }

    object BenchmarksWithFocus {
      def fromBenchmarkSuite(
          benchmarks: Seq[Benchmark]): BenchmarksWithFocus = {
        BenchmarksWithFocus(benchmarks, 0, BitSet(benchmarks.indices: _*))
      }
    }

    case class SingleBenchmarkInfo(iterations: Int, totalTime: FiniteDuration) {
      def hasEnoughIterations: Boolean =
        iterations >= 20 || totalTime >= 1.second

      def averageTimeOpt: Option[FiniteDuration] = {
        if (iterations == 0) None
        else Some(totalTime / iterations)
      }
    }

    case object NoData extends BenchmarkData

    case class WarmUpData(actors: Actors,
                          focusedBenchmarks: BenchmarksWithFocus)
        extends BenchmarkData

    case class BenchmarkingData(actors: Actors,
                                currentProblemSize: Int,
                                currentBenchmarkInfo: SingleBenchmarkInfo,
                                focusedBenchmarks: BenchmarksWithFocus)
        extends BenchmarkData
  }

  sealed trait BenchmarkMessage

  case class StartBenchmarking(benchmarkSuite: Seq[Benchmark])
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
