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

import akka.testkit.{TestActorRef, TestProbe}
import pl.tarsa.sortalgobox.core.actors.BenchmarkWorkerActor.{
  BenchmarkFailed,
  BenchmarkRequest,
  BenchmarkSucceeded
}
import pl.tarsa.sortalgobox.tests.ActorSpecBase

import scala.concurrent.duration._

class BenchmarkWorkerActorSpec extends ActorSpecBase {
  typeBehavior[BenchmarkWorkerActor]

  it must "report time of successful action" in new Fixture {
    probe.send(workerActor, BenchmarkRequest(5, 1, _ => 2.millis))
    probe.expectMsg(BenchmarkSucceeded(5, 2.millis))
  }

  it must "report failure" in new Fixture {
    probe.send(workerActor, BenchmarkRequest(3, 1, _ => throw testException))
    probe.expectMsg(BenchmarkFailed(3))
  }

  it must "work after failure" in new Fixture {
    probe.send(workerActor, BenchmarkRequest(1, 1, _ => throw testException))
    probe.expectMsg(BenchmarkFailed(1))
    probe.send(workerActor, BenchmarkRequest(2, 1, _ => 3000.millis))
    probe.expectMsg(BenchmarkSucceeded(2, 3.seconds))
  }

  it must "run action with correctly sized buffer" in new Fixture {
    probe.send(workerActor, BenchmarkRequest(4, 123, buffer => {
      buffer.length mustBe 123
      1000.nanos
    }))
    probe.expectMsg(BenchmarkSucceeded(4, 1.micro))
  }

  it must "reuse previously created buffer of same size" in new Fixture {
    var bufferOpt: Option[Array[Int]] = None
    probe.send(workerActor, BenchmarkRequest(6, 123, buffer => {
      bufferOpt = Some(buffer)
      0.seconds
    }))
    probe.expectMsg(BenchmarkSucceeded(6, Duration.Zero))
    bufferOpt mustBe 'defined
    probe.send(workerActor, BenchmarkRequest(7, 123, buffer => {
      assert(bufferOpt.get eq buffer)
      0.seconds
    }))
    probe.expectMsg(BenchmarkSucceeded(7, Duration.Zero))
  }

  it must "create new buffer if old one has different size" in new Fixture {
    var bufferOpt: Option[Array[Int]] = None
    probe.send(workerActor, BenchmarkRequest(8, 123, buffer => {
      bufferOpt = Some(buffer)
      0.seconds
    }))
    probe.expectMsg(BenchmarkSucceeded(8, Duration.Zero))
    bufferOpt mustBe 'defined
    probe.send(workerActor, BenchmarkRequest(9, 234, buffer => {
      assert(bufferOpt.get ne buffer)
      0.seconds
    }))
    probe.expectMsg(BenchmarkSucceeded(9, Duration.Zero))
  }

  it must "bubble exception from benchmark body" in new Fixture {
    intercept[TestException] {
      workerActor.receive(BenchmarkRequest(-1, 0, _ => throw testException),
                          probe.ref)
    }
    probe.expectMsg(BenchmarkFailed(-1))
  }

  class Fixture {
    val workerActor: TestActorRef[BenchmarkWorkerActor] =
      TestActorRef[BenchmarkWorkerActor]

    val probe = TestProbe()
  }
}
