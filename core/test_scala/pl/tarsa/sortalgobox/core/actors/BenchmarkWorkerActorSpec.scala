/*
 * Copyright (C) 2017 Piotr Tarsa ( http://github.com/tarsa )
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
package pl.tarsa.sortalgobox.core.actors

import akka.testkit.{TestActorRef, TestProbe}
import pl.tarsa.sortalgobox.core.actors.BenchmarkWorkerActor.{
  BenchmarkFailed,
  BenchmarkRequest,
  BenchmarkSucceeded
}
import pl.tarsa.sortalgobox.tests.ActorSpecBase

import scala.concurrent.duration.Duration

class BenchmarkWorkerActorSpec extends ActorSpecBase {
  typeBehavior[BenchmarkWorkerActor]

  it must "report time of successful action" in {
    val workerActor = TestActorRef[BenchmarkWorkerActor]
    val probe = TestProbe()
    probe.send(workerActor, BenchmarkRequest(5, () => ()))
    val result = probe.expectMsgType[BenchmarkSucceeded]
    result.id mustBe 5
    result.timeTaken mustBe >(Duration.Zero)
  }

  it must "report failure" in {
    val workerActor = TestActorRef[BenchmarkWorkerActor]
    val probe = TestProbe()
    probe.send(workerActor, BenchmarkRequest(3, () => sys.error("boom")))
    probe.expectMsg(BenchmarkFailed(3))
  }

  it must "work after failure" in {
    val workerActor = TestActorRef[BenchmarkWorkerActor]
    val probe = TestProbe()
    probe.send(workerActor, BenchmarkRequest(1, () => sys.error("boom")))
    probe.expectMsg(BenchmarkFailed(1))
    probe.send(workerActor, BenchmarkRequest(2, () => ()))
    probe.expectMsgType[BenchmarkSucceeded].id mustBe 2
  }
}
