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

class PlainItemsAgentSpec extends ItemsAgentSpecBase[PlainItemsAgent.type] {
  private val agent: ItemsAgent = PlainItemsAgent

  override def pureTestComparable(
      body: (ItemsAgent, ComparableItemsOperations[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit =
    body(agent, ItemsOperations.intOps)

  override def pureTestNumeric(
      body: (ItemsAgent, NumericItemsOperations[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit =
    body(agent, ItemsOperations.intOps)

  override def readTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit =
    body(agent, ComparableItemsBuffer[Int](5, inputItems.toArray, 0))

  override def readTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit =
    body(agent, NumericItemsBuffer(5, inputItems.toArray, 0))

  override def writeTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      outputItems: Int*)(expectedRecordedBytes: Int*)(
      implicit pos: Position): Unit = {
    val itemsArray = inputItems.toArray
    body(agent, ComparableItemsBuffer[Int](5, itemsArray, 0))
    itemsArray.toSeq mustBe outputItems withClue "final array contents invalid"
  }

  override def writeTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(outputItems: Int*)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
    val itemsArray = inputItems.toArray
    body(agent, NumericItemsBuffer(5, itemsArray, 0))
    itemsArray.toSeq mustBe outputItems withClue "final array contents invalid"
  }
}
