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

import scala.{specialized => spec}

object PlainItemsAgent extends ItemsAgent {
  final override def size[Buffer <: ItemsBuffer[Buffer]](buffer: Buffer): Int =
    buffer.length

  final override def get[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                          index: Int): Item =
    buffer.get(index)

  final override def get[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                          index: Int): Item =
    buffer.get(index)

  final override def set[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                          index: Int,
                                          value: Item): Unit =
    buffer.set(index, value)

  final override def set[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                          index: Int,
                                          value: Item): Unit =
    buffer.set(index, value)

  final override def swap[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                           index1: Int,
                                           index2: Int): Unit = {
    val item = buffer.get(index1)
    buffer.set(index1, buffer.get(index2))
    buffer.set(index2, item)
  }

  final override def swap[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                           index1: Int,
                                           index2: Int): Unit = {
    val item = buffer.get(index1)
    buffer.set(index1, buffer.get(index2))
    buffer.set(index2, item)
  }

  final override def itemBitsSize(buffer: NumericItemsBuffer[_]): Int =
    buffer.itemsOps.bitsSize

  final override def asLong[@spec(Grp) Item](
      itemsOps: NumericItemsOperations[Item],
      value: Item): Long =
    itemsOps.asLong(value)

  final override def getSlice[@spec(Grp) Item](
      itemsOps: NumericItemsOperations[Item],
      value: Item,
      lowestBitIndex: Int,
      length: Int): Int =
    itemsOps.getSlice(value, lowestBitIndex, length)

  final override def compareEq[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean =
    itemsOps.compareEq(value1, value2)

  final override def compareGt[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean =
    itemsOps.compareGt(value1, value2)

  final override def compareGte[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean =
    itemsOps.compareGte(value1, value2)

  final override def compareLt[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean =
    itemsOps.compareLt(value1, value2)

  final override def compareLte[@spec(Grp) Item](
      itemsOps: ComparableItemsOperations[Item],
      value1: Item,
      value2: Item): Boolean =
    itemsOps.compareLte(value1, value2)
}
