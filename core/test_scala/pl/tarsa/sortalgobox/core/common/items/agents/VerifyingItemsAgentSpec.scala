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
package pl.tarsa.sortalgobox.core.common.items.agents

import java.io.{ByteArrayInputStream, InputStream}

import org.scalactic.source.Position
import org.scalatest.exceptions.TestFailedException
import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes._
import pl.tarsa.sortalgobox.core.common.items.buffers.{
  ComparableItemsBuffer,
  NumericItemsBuffer
}
import pl.tarsa.sortalgobox.core.common.items.operations.{
  ComparableItemsOperations,
  ItemsOperations,
  NumericItemsOperations
}
import pl.tarsa.sortalgobox.core.crossverify.{
  PrematureEndOfInputException,
  PureNumberDecoder
}

class VerifyingItemsAgentSpec extends ItemsAgentSpecBase[VerifyingItemsAgent] {

  it must "fail for some operations and no recorded bytes" in {
    a[PrematureEndOfInputException] mustBe thrownBy {
      readTestComparable(1, 2, 3) { (agent, buf) =>
        agent.compareEqI(buf, 1, 2)
      }()
    }
  }

  it must "fail for no operations and some recorded bytes" in {
    a[TestFailedException] mustBe thrownBy {
      readTestComparable(1, 2, 3)((_, _) => ())(Get.id, 2)
    }
  }

  it must "fail for mismatched operations and recorded bytes" in {
    a[TestFailedException] mustBe thrownBy {
      readTestComparable(1, 2, 3) { (agent, buf) =>
        agent.swap(buf, 1, 0)
      }(CompareEq.id, 3)
    }
  }

  override def pureTestComparable(
      body: (ItemsAgent, ComparableItemsOperations[Int]) => Unit)(
      recordedBytes: Int*)(implicit pos: Position): Unit = {
    val (stream, agent) = buildVerifyingAgent(recordedBytes)
    body(agent, ItemsOperations.intOps)
    stream.read() mustBe -1
  }

  override def pureTestNumeric(
      body: (ItemsAgent, NumericItemsOperations[Int]) => Unit)(
      recordedBytes: Int*)(implicit pos: Position): Unit = {
    val (stream, agent) = buildVerifyingAgent(recordedBytes)
    body(agent, ItemsOperations.intOps)
    stream.read() mustBe -1
  }

  override def readTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      recordedBytes: Int*)(implicit pos: Position): Unit = {
    val (stream, agent) = buildVerifyingAgent(recordedBytes)
    body(agent, ComparableItemsBuffer[Int](bufferId, inputItems.toArray, 0))
    stream.read() mustBe -1
  }

  override def readTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(recordedBytes: Int*)(
      implicit pos: Position): Unit = {
    val (stream, agent) = buildVerifyingAgent(recordedBytes)
    body(agent, NumericItemsBuffer(bufferId, inputItems.toArray, 0))
    stream.read() mustBe -1
  }

  override def writeTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      outputItems: Int*)(recordedBytes: Int*)(implicit pos: Position): Unit = {
    val array = inputItems.toArray
    val (stream, agent) = buildVerifyingAgent(recordedBytes)
    body(agent, ComparableItemsBuffer[Int](bufferId, array, 0))
    array.toSeq mustBe outputItems withClue "final array contents invalid"
    stream.read() mustBe -1
  }

  override def writeTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(outputItems: Int*)(
      recordedBytes: Int*)(implicit pos: Position): Unit = {
    val array = inputItems.toArray
    val (stream, agent) = buildVerifyingAgent(recordedBytes)
    body(agent, NumericItemsBuffer(bufferId, array, 0))
    array.toSeq mustBe outputItems withClue "final array contents invalid"
    stream.read() mustBe -1
  }

  private def buildVerifyingAgent(recordedBytes: Seq[Int])(
      implicit pos: Position): (InputStream, ItemsAgent) = {
    val recordingStream =
      new ByteArrayInputStream(recordedBytes.map(_.toByte).toArray)
    val replayer = new PureNumberDecoder(recordingStream)
    val recordingAgent = new VerifyingItemsAgent(replayer, verify)
    (recordingStream, recordingAgent)
  }

  private def verify(condition: Boolean)(implicit pos: Position): Unit = {
    if (!condition) {
      fail("verification failed in verifying agent ")
    }
  }
}
