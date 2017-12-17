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

import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes
import pl.tarsa.sortalgobox.core.common.Specialization.{Group => Grp}
import pl.tarsa.sortalgobox.core.common.items.buffers.{
  ComparableItemsBuffer,
  ItemsBuffer,
  NumericItemsBuffer
}
import pl.tarsa.sortalgobox.core.common.items.operations.{
  ComparableItemsOperations,
  NumericItemsOperations
}
import pl.tarsa.sortalgobox.core.crossverify.PureNumberDecoder

import scala.{specialized => spec}

final class VerifyingItemsAgent(replayer: PureNumberDecoder,
                                verify: Boolean => Unit)
    extends ItemsAgent {
  private def run = PlainItemsAgent

  override def size[Buffer <: ItemsBuffer[Buffer]](buffer: Buffer): Int = {
    verifyAction(ActionTypes.Size)
    verifyByte(buffer.id.toByte)
    val result = run.size(buffer)
    verifyInt(result)
    result
  }

  override def get[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                    index: Int): Item = {
    verifyAction(ActionTypes.Get)
    verifyByte(buffer.id.toByte)
    verifyInt(index)
    run.get(buffer, index)
  }

  override def get[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                    index: Int): Item = {
    verifyAction(ActionTypes.Get)
    verifyByte(buffer.id.toByte)
    verifyInt(index)
    run.get(buffer, index)
  }

  override def set[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                    index: Int,
                                    value: Item): Unit = {
    verifyAction(ActionTypes.Set)
    verifyByte(buffer.id.toByte)
    verifyInt(index)
    run.set(buffer, index, value)
  }

  override def set[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                    index: Int,
                                    value: Item): Unit = {
    verifyAction(ActionTypes.Set)
    verifyByte(buffer.id.toByte)
    verifyInt(index)
    run.set(buffer, index, value)
  }

  override def swap[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                     index1: Int,
                                     index2: Int): Unit = {
    verifyAction(ActionTypes.Swap)
    verifyByte(buffer.id.toByte)
    verifyInt(index1)
    verifyInt(index2)
    run.swap(buffer, index1, index2)
  }

  override def swap[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                     index1: Int,
                                     index2: Int): Unit = {
    verifyAction(ActionTypes.Swap)
    verifyByte(buffer.id.toByte)
    verifyInt(index1)
    verifyInt(index2)
    run.swap(buffer, index1, index2)
  }

  override def itemBitsSize(buffer: NumericItemsBuffer[_]): Int = {
    verifyAction(ActionTypes.ItemBitsSize)
    verifyByte(buffer.id.toByte)
    val result = run.itemBitsSize(buffer)
    verifyByte(result.toByte)
    result
  }

  override def asLong[@spec(Grp) Item](itemsOps: NumericItemsOperations[Item],
                                       value: Item): Long = {
    verifyAction(ActionTypes.AsLong)
    val result = run.asLong(itemsOps, value)
    verifyLong(result)
    result
  }

  override def getSlice[@spec(Grp) Item](itemsOps: NumericItemsOperations[Item],
                                         value: Item,
                                         lowestBitIndex: Int,
                                         length: Int): Int = {
    verifyAction(ActionTypes.GetSlice)
    verifyByte(lowestBitIndex.toByte)
    verifyByte(length.toByte)
    val result = run.getSlice(itemsOps, value, lowestBitIndex, length)
    verifyInt(result)
    result
  }

  override def compareEq[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    verifyAction(ActionTypes.CompareEq)
    val result = run.compareEq(itemsOps, value1, value2)
    verifyBit(result)
    result
  }

  override def compareGt[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    verifyAction(ActionTypes.CompareGt)
    val result = run.compareGt(itemsOps, value1, value2)
    verifyBit(result)
    result
  }

  override def compareGte[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    verifyAction(ActionTypes.CompareGte)
    val result = run.compareGte(itemsOps, value1, value2)
    verifyBit(result)
    result
  }

  override def compareLt[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    verifyAction(ActionTypes.CompareLt)
    val result = run.compareLt(itemsOps, value1, value2)
    verifyBit(result)
    result
  }

  override def compareLte[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    verifyAction(ActionTypes.CompareLte)
    val result = run.compareLte(itemsOps, value1, value2)
    verifyBit(result)
    result
  }

  private def verifyBit(expected: Boolean): Unit = {
    val actual = replayer.deserializeBit()
    verify(expected == actual)
  }

  private def verifyAction(action: ActionTypes.Action): Unit =
    verifyByte(action.id.toByte)

  private def verifyByte(expected: Byte): Unit = {
    val actual = replayer.deserializeByte()
    verify(expected == actual)
  }

  private def verifyInt(expected: Int): Unit = {
    val actual = replayer.deserializeInt()
    verify(expected == actual)
  }

  private def verifyLong(expected: Long): Unit = {
    val actual = replayer.deserializeLong()
    verify(expected == actual)
  }
}
