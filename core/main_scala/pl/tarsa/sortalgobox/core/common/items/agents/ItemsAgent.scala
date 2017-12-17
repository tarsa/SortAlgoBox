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

abstract class ItemsAgent private[items] {
  def size[Buffer <: ItemsBuffer[Buffer]](buffer: Buffer): Int

  def get[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                           index: Int): Item

  def get[@spec(Grp) Item](buffer: NumericItemsBuffer[Item], index: Int): Item

  def set[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                           index: Int,
                           value: Item): Unit

  def set[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                           index: Int,
                           value: Item): Unit

  def swap[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                            index1: Int,
                            index2: Int): Unit

  def swap[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                            index1: Int,
                            index2: Int): Unit

  def itemBitsSize(buffer: NumericItemsBuffer[_]): Int

  def asLong[@spec(Grp) Item](itemsOps: NumericItemsOperations[Item],
                              value: Item): Long

  final def asLongI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                     index: Int): Long =
    asLong(buffer.itemsOps, get(buffer, index))

  def getSlice[@spec(Grp) Item](itemsOps: NumericItemsOperations[Item],
                                value: Item,
                                lowestBitIndex: Int,
                                length: Int): Int

  final def getSliceI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                       index: Int,
                                       lowestBitIndex: Int,
                                       length: Int): Int =
    getSlice(buffer.itemsOps, get(buffer, index), lowestBitIndex, length)

  def compareEq[@spec(Grp) Item](itemsOps: ComparableItemsOperations[Item],
                                 value1: Item,
                                 value2: Item): Boolean

  final def compareEqI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                        index1: Int,
                                        index2: Int): Boolean =
    compareEq(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareEqI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                        index1: Int,
                                        index2: Int): Boolean =
    compareEq(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareEqIV[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         index1: Int,
                                         value2: Item): Boolean =
    compareEq(buffer.itemsOps, get(buffer, index1), value2)

  final def compareEqIV[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         index1: Int,
                                         value2: Item): Boolean =
    compareEq(buffer.itemsOps, get(buffer, index1), value2)

  final def compareEqVI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         value1: Item,
                                         index2: Int): Boolean =
    compareEq(buffer.itemsOps, value1, get(buffer, index2))

  final def compareEqVI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         value1: Item,
                                         index2: Int): Boolean =
    compareEq(buffer.itemsOps, value1, get(buffer, index2))

  def compareGt[@spec(Grp) Item](itemsOps: ComparableItemsOperations[Item],
                                 value1: Item,
                                 value2: Item): Boolean

  final def compareGtI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                        index1: Int,
                                        index2: Int): Boolean =
    compareGt(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareGtI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                        index1: Int,
                                        index2: Int): Boolean =
    compareGt(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareGtIV[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         index1: Int,
                                         value2: Item): Boolean =
    compareGt(buffer.itemsOps, get(buffer, index1), value2)

  final def compareGtIV[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         index1: Int,
                                         value2: Item): Boolean =
    compareGt(buffer.itemsOps, get(buffer, index1), value2)

  final def compareGtVI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         value1: Item,
                                         index2: Int): Boolean =
    compareGt(buffer.itemsOps, value1, get(buffer, index2))

  final def compareGtVI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         value1: Item,
                                         index2: Int): Boolean =
    compareGt(buffer.itemsOps, value1, get(buffer, index2))

  def compareGte[@spec(Grp) Item](itemsOps: ComparableItemsOperations[Item],
                                  value1: Item,
                                  value2: Item): Boolean

  final def compareGteI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         index1: Int,
                                         index2: Int): Boolean =
    compareGte(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareGteI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         index1: Int,
                                         index2: Int): Boolean =
    compareGte(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareGteIV[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                          index1: Int,
                                          value2: Item): Boolean =
    compareGte(buffer.itemsOps, get(buffer, index1), value2)

  final def compareGteIV[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                          index1: Int,
                                          value2: Item): Boolean =
    compareGte(buffer.itemsOps, get(buffer, index1), value2)

  final def compareGteVI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                          value1: Item,
                                          index2: Int): Boolean =
    compareGte(buffer.itemsOps, value1, get(buffer, index2))

  final def compareGteVI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                          value1: Item,
                                          index2: Int): Boolean =
    compareGte(buffer.itemsOps, value1, get(buffer, index2))

  def compareLt[@spec(Grp) Item](itemsOps: ComparableItemsOperations[Item],
                                 value1: Item,
                                 value2: Item): Boolean

  final def compareLtI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                        index1: Int,
                                        index2: Int): Boolean =
    compareLt(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareLtI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                        index1: Int,
                                        index2: Int): Boolean =
    compareLt(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareLtIV[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         index1: Int,
                                         value2: Item): Boolean =
    compareLt(buffer.itemsOps, get(buffer, index1), value2)

  final def compareLtIV[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         index1: Int,
                                         value2: Item): Boolean =
    compareLt(buffer.itemsOps, get(buffer, index1), value2)

  final def compareLtVI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         value1: Item,
                                         index2: Int): Boolean =
    compareLt(buffer.itemsOps, value1, get(buffer, index2))

  final def compareLtVI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         value1: Item,
                                         index2: Int): Boolean =
    compareLt(buffer.itemsOps, value1, get(buffer, index2))

  def compareLte[@spec(Grp) Item](itemsOps: ComparableItemsOperations[Item],
                                  value1: Item,
                                  value2: Item): Boolean

  final def compareLteI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                         index1: Int,
                                         index2: Int): Boolean =
    compareLte(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareLteI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                         index1: Int,
                                         index2: Int): Boolean =
    compareLte(buffer.itemsOps, get(buffer, index1), get(buffer, index2))

  final def compareLteIV[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                          index1: Int,
                                          value2: Item): Boolean =
    compareLte(buffer.itemsOps, get(buffer, index1), value2)

  final def compareLteIV[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                          index1: Int,
                                          value2: Item): Boolean =
    compareLte(buffer.itemsOps, get(buffer, index1), value2)

  final def compareLteVI[@spec(Grp) Item](buffer: ComparableItemsBuffer[Item],
                                          value1: Item,
                                          index2: Int): Boolean =
    compareLte(buffer.itemsOps, value1, get(buffer, index2))

  final def compareLteVI[@spec(Grp) Item](buffer: NumericItemsBuffer[Item],
                                          value1: Item,
                                          index2: Int): Boolean =
    compareLte(buffer.itemsOps, value1, get(buffer, index2))
}
