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
import pl.tarsa.sortalgobox.core.crossverify.PureNumberEncoder

import scala.{specialized => spec}

final class RecordingItemsAgent(recorder: PureNumberEncoder)
    extends ItemsAgent {
  import recorder._

  private def run = PlainItemsAgent

  override def size[Buffer <: ItemsBuffer[Buffer]](buffer: Buffer): Int = {
    serializeAction(ActionTypes.Size)
    serializeByteSafe(buffer.id)
    val result = run.size(buffer)
    serializeInt(result)
    result
  }

  override def get[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                    index: Int): Item = {
    serializeAction(ActionTypes.Get)
    serializeByteSafe(buffer.id)
    serializeInt(index)
    run.get(buffer, index)
  }

  override def get[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                    index: Int): Item = {
    serializeAction(ActionTypes.Get)
    serializeByteSafe(buffer.id)
    serializeInt(index)
    run.get(buffer, index)
  }

  override def set[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                    index: Int,
                                    value: Item): Unit = {
    serializeAction(ActionTypes.Set)
    serializeByteSafe(buffer.id)
    serializeInt(index)
    run.set(buffer, index, value)
  }

  override def set[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                    index: Int,
                                    value: Item): Unit = {
    serializeAction(ActionTypes.Set)
    serializeByteSafe(buffer.id)
    serializeInt(index)
    run.set(buffer, index, value)
  }

  override def swap[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                     index1: Int,
                                     index2: Int): Unit = {
    serializeAction(ActionTypes.Swap)
    serializeByteSafe(buffer.id)
    serializeInt(index1)
    serializeInt(index2)
    run.swap(buffer, index1, index2)
  }

  override def swap[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                     index1: Int,
                                     index2: Int): Unit = {
    serializeAction(ActionTypes.Swap)
    serializeByteSafe(buffer.id)
    serializeInt(index1)
    serializeInt(index2)
    run.swap(buffer, index1, index2)
  }

  override def itemBitsSize(buffer: NumericItemsBuffer[_]): Int = {
    serializeAction(ActionTypes.ItemBitsSize)
    serializeByteSafe(buffer.id)
    val result = run.itemBitsSize(buffer)
    serializeByteSafe(result)
    result
  }

  override def asLong[@spec(Grp) Item](itemsOps: NumericItemsOperations[Item],
                                       value: Item): Long = {
    serializeAction(ActionTypes.AsLong)
    val result = run.asLong(itemsOps, value)
    serializeLong(result)
    result
  }

  override def getSlice[@spec(Grp) Item](itemsOps: NumericItemsOperations[Item],
                                         value: Item,
                                         lowestBitIndex: Int,
                                         length: Int): Int = {
    serializeAction(ActionTypes.GetSlice)
    serializeByteSafe(lowestBitIndex)
    serializeByteSafe(length)
    val result = run.getSlice(itemsOps, value, lowestBitIndex, length)
    serializeInt(result)
    result
  }

  override def compareEq[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    serializeAction(ActionTypes.CompareEq)
    val result = run.compareEq(itemsOps, value1, value2)
    serializeBit(result)
    result
  }

  override def compareGt[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    serializeAction(ActionTypes.CompareGt)
    val result = run.compareGt(itemsOps, value1, value2)
    serializeBit(result)
    result
  }

  override def compareGte[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    serializeAction(ActionTypes.CompareGte)
    val result = run.compareGte(itemsOps, value1, value2)
    serializeBit(result)
    result
  }

  override def compareLt[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    serializeAction(ActionTypes.CompareLt)
    val result = run.compareLt(itemsOps, value1, value2)
    serializeBit(result)
    result
  }

  override def compareLte[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean = {
    serializeAction(ActionTypes.CompareLte)
    val result = run.compareLte(itemsOps, value1, value2)
    serializeBit(result)
    result
  }

  private def serializeAction(action: ActionTypes.Action): Unit =
    serializeByteSafe(action.id)

  private def serializeByteSafe(value: Int): Unit = {
    val asByte = value.toByte.ensuring(_ == value)
    serializeByte(asByte)
  }
}
