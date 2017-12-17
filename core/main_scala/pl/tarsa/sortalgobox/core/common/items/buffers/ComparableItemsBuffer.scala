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
package pl.tarsa.sortalgobox.core.common.items.buffers

import pl.tarsa.sortalgobox.core.common.Specialization.Group
import pl.tarsa.sortalgobox.core.common.items.operations.{
  ComparableItemsOperations,
  ItemsOperations
}

sealed trait ComparableItemsBuffer[@specialized(Group) Item]
    extends ItemsBuffer[ComparableItemsBuffer[Item]] {
  protected[items] def get(index: Int): Item
  protected[items] def set(index: Int, item: Item): Unit
  def itemsOps: ComparableItemsOperations[Item]
}

object ComparableItemsBuffer {
  def apply[T: Ordering](id: Int,
                         array: Array[T],
                         base: Int): ComparableItemsBuffer[T] = {
    array match {
      case bytes: Array[Byte] =>
        new Bytes(id, base, bytes)
      case shorts: Array[Short] =>
        new Shorts(id, base, shorts)
      case integers: Array[Int] =>
        new Integers(id, base, integers)
      case longs: Array[Long] =>
        new Longs(id, base, longs)
      case others: Array[T] =>
        new Others[T](id, base, others)
    }
  }

  final private class Bytes(val id: Int, val base: Int, array: Array[Byte])
      extends ComparableItemsBuffer[Byte] {
    def length: Int =
      array.length
    def get(index: Int): Byte =
      array(index - base)
    def set(index: Int, item: Byte): Unit =
      array(index - base) = item
    def itemsOps: ComparableItemsOperations[Byte] =
      ItemsOperations.bytesOps
  }

  final private class Shorts(val id: Int, val base: Int, array: Array[Short])
      extends ComparableItemsBuffer[Short] {
    def length: Int =
      array.length
    def get(index: Int): Short =
      array(index - base)
    def set(index: Int, item: Short): Unit =
      array(index - base) = item
    def itemsOps: ComparableItemsOperations[Short] =
      ItemsOperations.shortOps
  }

  final private class Integers(val id: Int, val base: Int, array: Array[Int])
      extends ComparableItemsBuffer[Int] {
    def length: Int =
      array.length
    def get(index: Int): Int =
      array(index - base)
    def set(index: Int, item: Int): Unit =
      array(index - base) = item
    def itemsOps: ComparableItemsOperations[Int] =
      ItemsOperations.intOps
  }

  final private class Longs(val id: Int, val base: Int, array: Array[Long])
      extends ComparableItemsBuffer[Long] {
    def length: Int =
      array.length
    def get(index: Int): Long =
      array(index - base)
    def set(index: Int, item: Long): Unit =
      array(index - base) = item
    def itemsOps: ComparableItemsOperations[Long] =
      ItemsOperations.longOps
  }

  final private class Others[Item: Ordering](val id: Int,
                                             val base: Int,
                                             array: Array[Item])
      extends ComparableItemsBuffer[Item] {
    def length: Int =
      array.length
    def get(index: Int): Item =
      array(index - base)
    def set(index: Int, item: Item): Unit =
      array(index - base) = item
    val itemsOps: ComparableItemsOperations[Item] =
      ItemsOperations.orderedItemsOps[Item]
  }
}
