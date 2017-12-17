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
package pl.tarsa.sortalgobox.core.common.items.operations

import pl.tarsa.sortalgobox.core.common.Specialization.Group

sealed trait ComparableItemsOperations[@specialized(Group) Item] {
  protected[items] def compareEq(value1: Item, value2: Item): Boolean
  protected[items] def compareGt(value1: Item, value2: Item): Boolean
  protected[items] def compareGte(value1: Item, value2: Item): Boolean
  protected[items] def compareLt(value1: Item, value2: Item): Boolean
  protected[items] def compareLte(value1: Item, value2: Item): Boolean
}

sealed trait NumericItemsOperations[@specialized(Group) Item]
    extends ComparableItemsOperations[Item] {
  protected[items] def bitsSize: Int
  protected[items] def asLong(value: Item): Long
  protected[items] def getSlice(value: Item,
                                lowestBitIndex: Int,
                                length: Int): Int
}

private[items] object ItemsOperations {
  def orderedItemsOps[Item: Ordering]: ComparableItemsOperations[Item] = {
    val ord = Ordering[Item]
    new ComparableItemsOperations[Item] {
      final def compareEq(value1: Item, value2: Item): Boolean =
        ord.equiv(value1, value2)
      final def compareGt(value1: Item, value2: Item): Boolean =
        ord.gt(value1, value2)
      final def compareGte(value1: Item, value2: Item): Boolean =
        ord.gteq(value1, value2)
      final def compareLt(value1: Item, value2: Item): Boolean =
        ord.lt(value1, value2)
      final def compareLte(value1: Item, value2: Item): Boolean =
        ord.lteq(value1, value2)
    }
  }

  final val bytesOps: NumericItemsOperations[Byte] = {
    new NumericItemsOperations[Byte] {
      final def bitsSize: Int =
        8
      final def asLong(value: Byte): Long =
        value.toLong
      final def getSlice(value: Byte, lowestBitIndex: Int, length: Int): Int =
        (value >> lowestBitIndex) & ((1 << length) - 1)
      final def compareEq(value1: Byte, value2: Byte): Boolean =
        value1 == value2
      final def compareGt(value1: Byte, value2: Byte): Boolean =
        value1 > value2
      final def compareGte(value1: Byte, value2: Byte): Boolean =
        value1 >= value2
      final def compareLt(value1: Byte, value2: Byte): Boolean =
        value1 < value2
      final def compareLte(value1: Byte, value2: Byte): Boolean =
        value1 <= value2
    }
  }

  final val shortOps: NumericItemsOperations[Short] = {
    new NumericItemsOperations[Short] {
      final def bitsSize: Int =
        16
      final def asLong(value: Short): Long =
        value.toLong
      final def getSlice(value: Short, lowestBitIndex: Int, length: Int): Int =
        (value >> lowestBitIndex) & ((1 << length) - 1)
      final def compareEq(value1: Short, value2: Short): Boolean =
        value1 == value2
      final def compareGt(value1: Short, value2: Short): Boolean =
        value1 > value2
      final def compareGte(value1: Short, value2: Short): Boolean =
        value1 >= value2
      final def compareLt(value1: Short, value2: Short): Boolean =
        value1 < value2
      final def compareLte(value1: Short, value2: Short): Boolean =
        value1 <= value2
    }
  }

  final val intOps: NumericItemsOperations[Int] = {
    new NumericItemsOperations[Int] {
      final def bitsSize: Int =
        32
      final def asLong(value: Int): Long =
        value.toLong
      final def getSlice(value: Int, lowestBitIndex: Int, length: Int): Int =
        (value >> lowestBitIndex) & ((1 << length) - 1)
      final def compareEq(value1: Int, value2: Int): Boolean =
        value1 == value2
      final def compareGt(value1: Int, value2: Int): Boolean =
        value1 > value2
      final def compareGte(value1: Int, value2: Int): Boolean =
        value1 >= value2
      final def compareLt(value1: Int, value2: Int): Boolean =
        value1 < value2
      final def compareLte(value1: Int, value2: Int): Boolean =
        value1 <= value2
    }
  }

  final val longOps: NumericItemsOperations[Long] = {
    new NumericItemsOperations[Long] {
      final def bitsSize: Int =
        64
      final def asLong(value: Long): Long =
        value.toLong
      final def getSlice(value: Long, lowestBitIndex: Int, length: Int): Int =
        (value >> lowestBitIndex).toInt & ((1 << length) - 1)
      final def compareEq(value1: Long, value2: Long): Boolean =
        value1 == value2
      final def compareGt(value1: Long, value2: Long): Boolean =
        value1 > value2
      final def compareGte(value1: Long, value2: Long): Boolean =
        value1 >= value2
      final def compareLt(value1: Long, value2: Long): Boolean =
        value1 < value2
      final def compareLte(value1: Long, value2: Long): Boolean =
        value1 <= value2
    }
  }
}
