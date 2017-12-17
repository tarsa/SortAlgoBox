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

import java.io.ByteArrayOutputStream

import org.scalactic.source.Position
import pl.tarsa.sortalgobox.core.common.items.buffers.{
  ComparableItemsBuffer,
  NumericItemsBuffer
}
import pl.tarsa.sortalgobox.core.common.items.operations.{
  ComparableItemsOperations,
  ItemsOperations,
  NumericItemsOperations
}
import pl.tarsa.sortalgobox.core.crossverify.PureNumberEncoder

class RecordingItemsAgentSpec extends ItemsAgentSpecBase[RecordingItemsAgent] {
  override def pureTestComparable(
      body: (ItemsAgent, ComparableItemsOperations[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
    val (outputStream, agent) = buildRecordingAgent
    body(agent, ItemsOperations.intOps)
    assertRecordedBytes(outputStream, expectedRecordedBytes)
  }

  override def pureTestNumeric(
      body: (ItemsAgent, NumericItemsOperations[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
    val (outputStream, agent) = buildRecordingAgent
    body(agent, ItemsOperations.intOps)
    assertRecordedBytes(outputStream, expectedRecordedBytes)
  }

  override def readTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
    val (outputStream, agent) = buildRecordingAgent
    body(agent, ComparableItemsBuffer[Int](bufferId, inputItems.toArray, 0))
    assertRecordedBytes(outputStream, expectedRecordedBytes)
  }

  override def readTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
    val (outputStream, agent) = buildRecordingAgent
    body(agent, NumericItemsBuffer(bufferId, inputItems.toArray, 0))
    assertRecordedBytes(outputStream, expectedRecordedBytes)
  }

  override def writeTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      outputItems: Int*)(expectedRecordedBytes: Int*)(
      implicit pos: Position): Unit = {
    val array = inputItems.toArray
    val (stream, agent) = buildRecordingAgent
    body(agent, ComparableItemsBuffer[Int](bufferId, array, 0))
    array.toSeq mustBe outputItems withClue "final array contents invalid"
    assertRecordedBytes(stream, expectedRecordedBytes)
  }

  override def writeTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(outputItems: Int*)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
    val array = inputItems.toArray
    val (stream, agent) = buildRecordingAgent
    body(agent, NumericItemsBuffer(bufferId, array, 0))
    array.toSeq mustBe outputItems withClue "final array contents invalid"
    assertRecordedBytes(stream, expectedRecordedBytes)
  }

  private def buildRecordingAgent: (ByteArrayOutputStream, ItemsAgent) = {
    val recordingStream = new ByteArrayOutputStream()
    val recorder = new PureNumberEncoder(recordingStream)
    val recordingAgent = new RecordingItemsAgent(recorder)
    (recordingStream, recordingAgent)
  }

  private def assertRecordedBytes(
      recordingStream: ByteArrayOutputStream,
      expectedBytes: Seq[Int])(implicit pos: Position): Unit = {
    val recordedBytes = recordingStream.toByteArray
    recordedBytes.length mustBe expectedBytes.length withClue
      "recorded bytes number incorrect"
    forAll(expectedBytes.zip(recordedBytes)) {
      case (expectedValue, recordedValue) =>
        expectedValue mustBe (recordedValue & 0xFF)
    } withClue "recorded bytes content incorrect"
  }
}
