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
  ItemsOperations,
  NumericItemsOperations
}

sealed trait NumericItemsBuffer[@specialized(Group) Item]
    extends ItemsBuffer[NumericItemsBuffer[Item]] {
  protected[items] def get(index: Int): Item
  protected[items] def set(index: Int, item: Item): Unit
  def itemsOps: NumericItemsOperations[Item]
}

object NumericItemsBuffer {
  def apply(id: Int, array: Array[Byte], base: Int): NumericItemsBuffer[Byte] =
    new Bytes(id, base, array)

  def apply(id: Int,
            array: Array[Short],
            base: Int): NumericItemsBuffer[Short] =
    new Shorts(id, base, array)

  def apply(id: Int, array: Array[Int], base: Int): NumericItemsBuffer[Int] =
    new Integers(id, base, array)

  def apply(id: Int, array: Array[Long], base: Int): NumericItemsBuffer[Long] =
    new Longs(id, base, array)

  def apply[Item: Evidence](id: Int,
                            array: Array[Item],
                            base: Int): NumericItemsBuffer[Item] = {
    array match {
      case bytes: Array[Byte] =>
        apply(id, bytes, base)
      case shorts: Array[Short] =>
        apply(id, shorts, base)
      case integers: Array[Int] =>
        apply(id, integers, base)
      case longs: Array[Long] =>
        apply(id, longs, base)
      case _ =>
        val evidence = implicitly[Evidence[Item]]
        val arrayType = array.elemTag.runtimeClass.getSimpleName
        throw new IllegalArgumentException(
          s"Invalid evidence $evidence for array type $arrayType")
    }
  }

  final private class Bytes(val id: Int, val base: Int, array: Array[Byte])
      extends NumericItemsBuffer[Byte] {
    def length: Int =
      array.length
    def get(index: Int): Byte =
      array(index - base)
    def set(index: Int, item: Byte): Unit =
      array(index - base) = item
    def itemsOps: NumericItemsOperations[Byte] =
      ItemsOperations.bytesOps
  }

  final private class Shorts(val id: Int, val base: Int, array: Array[Short])
      extends NumericItemsBuffer[Short] {
    def length: Int =
      array.length
    def get(index: Int): Short =
      array(index - base)
    def set(index: Int, item: Short): Unit =
      array(index - base) = item
    def itemsOps: NumericItemsOperations[Short] =
      ItemsOperations.shortOps
  }

  final private class Integers(val id: Int, val base: Int, array: Array[Int])
      extends NumericItemsBuffer[Int] {
    def length: Int =
      array.length
    def get(index: Int): Int =
      array(index - base)
    def set(index: Int, item: Int): Unit =
      array(index - base) = item
    def itemsOps: NumericItemsOperations[Int] =
      ItemsOperations.intOps
  }

  final private class Longs(val id: Int, val base: Int, array: Array[Long])
      extends NumericItemsBuffer[Long] {
    def length: Int =
      array.length
    def get(index: Int): Long =
      array(index - base)
    def set(index: Int, item: Long): Unit =
      array(index - base) = item
    def itemsOps: NumericItemsOperations[Long] =
      ItemsOperations.longOps
  }

  sealed trait Evidence[Item]

  object Evidence {
    implicit object BytesEvidence extends Evidence[Byte]
    implicit object ShortsEvidence extends Evidence[Short]
    implicit object IntegersEvidence extends Evidence[Int]
    implicit object LongsEvidence extends Evidence[Long]
  }
}
