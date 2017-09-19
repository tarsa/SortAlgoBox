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

import akka.actor.{ActorRef, PoisonPill, Props}
import akka.testkit.{TestActorRef, TestActors, TestProbe}
import pl.tarsa.sortalgobox.core.Benchmark
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor.{
  BenchmarkFailed,
  BenchmarkSucceeded,
  BenchmarkingFinished,
  StartBenchmarking,
  warmUpArraySize
}
import pl.tarsa.sortalgobox.tests.ActorSpecBase

import scala.concurrent.duration._

class BenchmarkSuiteActorSpec extends ActorSpecBase {
  typeBehavior[BenchmarkSuiteActor]

  it must "succeed on empty benchmark suite" in new Fixture {
    probe.send(suiteActor, StartBenchmarking(Nil))
    probe.expectMsg(BenchmarkingFinished)
  }

  it must "report warm up failures" in new Fixture {
    probe.send(suiteActor, StartBenchmarking(Seq(null)))
    expectWorkerBenchmarkRequest(0, warmUpArraySize)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkFailed(0))
    probe.expectMsg(BenchmarkFailed(0, warmUpArraySize))
    probe.expectMsg(BenchmarkingFinished)
  }

  it must "report benchmark failures" in new Fixture {
    probe.send(suiteActor, StartBenchmarking(Seq(null)))
    expectWorkerBenchmarkRequest(0, warmUpArraySize)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(0, 1.second))
    expectWorkerBenchmarkRequest(0, 4096)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkFailed(0))
    probe.expectMsg(BenchmarkFailed(0, 4096))
    probe.expectMsg(BenchmarkingFinished)
  }

  it must "disable benchmarks independently" in new Fixture {
    probe.send(suiteActor, StartBenchmarking(Seq(null, null)))
    expectWorkerBenchmarkRequest(0, warmUpArraySize)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(0, 1.second))
    expectWorkerBenchmarkRequest(1, warmUpArraySize)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(1, 1.second))
    expectWorkerBenchmarkRequest(0, 4096)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(0, 2.seconds))
    probe.expectMsg(BenchmarkSucceeded(0, 4096, 2.seconds))
    expectWorkerBenchmarkRequest(1, 4096)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(1, 0.5.seconds))
    expectWorkerBenchmarkRequest(1, 4096)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(1, 0.5.seconds))
    probe.expectMsg(BenchmarkSucceeded(1, 4096, 0.5.seconds))
    expectWorkerBenchmarkRequest(1, 5329)
    workerProbe.reply(BenchmarkWorkerActor.BenchmarkSucceeded(1, 2.seconds))
    probe.expectMsg(BenchmarkSucceeded(1, 5329, 2.seconds))
    probe.expectMsg(BenchmarkingFinished)
  }

  it must "send proper requests to worker actor" in {
    val instance =
      TestActorRef(new BenchmarkSuiteActor(null)).underlyingActor
    val worker = actorSystem.actorOf(BenchmarkWorkerActor.props)
    val benchmark = new Benchmark {
      override def name: String = ""

      override def forSize(n: Int,
                           validate: Boolean,
                           buffer: Option[Array[Int]]): Long = {
        n mustBe 123
        validate mustBe true
        buffer.get must have length 123
        1234
      }
    }
    val result =
      instance.runBenchmark(worker, 3, 123, validate = true, benchmark)
    result mustBe BenchmarkSucceeded(3, 123, 1234.nanos)
    worker ! PoisonPill
  }

  it must "work after failures on real setup" in {
    val suiteActor = TestActorRef(BenchmarkSuiteActor.props)
    val probe = TestProbe()
    val benchmark = new Benchmark {
      override def forSize(n: Int,
                           validate: Boolean,
                           buffer: Option[Array[Int]]): Long =
        throw testException

      override def name: String = ""
    }
    probe.send(suiteActor, StartBenchmarking(Seq(benchmark, benchmark)))
    probe.expectMsg(BenchmarkFailed(0, warmUpArraySize))
    probe.expectMsg(BenchmarkFailed(1, warmUpArraySize))
    probe.expectMsg(BenchmarkingFinished)
  }

  class Fixture {
    val workerProbe = TestProbe()

    val suiteActor: ActorRef = {
      val workerProps = TestActors.forwardActorProps(workerProbe.ref)
      actorSystem.actorOf(Props(new BenchmarkSuiteActor(workerProps)))
    }

    val probe = TestProbe()

    def expectWorkerBenchmarkRequest(id: Int, bufferSize: Int): Unit = {
      workerProbe.expectMsgPF() {
        case BenchmarkWorkerActor.BenchmarkRequest(`id`, `bufferSize`, _) =>
      }
    }
  }
}
