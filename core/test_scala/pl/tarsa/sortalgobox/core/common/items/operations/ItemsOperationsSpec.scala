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
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.reflect.runtime.universe.TypeTag

class ItemsOperationsSpec extends CommonUnitSpecBase {
  typeBehavior[ComparableItemsOperations[String]]

  it must "correctly compare String values" in {
    val itemsOps = ItemsOperations.orderedItemsOps[String]

    itemsOps.compareEq("aa", "aa") mustBe true
    itemsOps.compareEq("aa", "bb") mustBe false
    itemsOps.compareEq("bb", "aa") mustBe false

    itemsOps.compareGt("aa", "aa") mustBe false
    itemsOps.compareGt("aa", "bb") mustBe false
    itemsOps.compareGt("bb", "aa") mustBe true

    itemsOps.compareGte("aa", "aa") mustBe true
    itemsOps.compareGte("aa", "bb") mustBe false
    itemsOps.compareGte("bb", "aa") mustBe true

    itemsOps.compareLt("aa", "aa") mustBe false
    itemsOps.compareLt("aa", "bb") mustBe true
    itemsOps.compareLt("bb", "aa") mustBe false

    itemsOps.compareLte("aa", "aa") mustBe true
    itemsOps.compareLte("aa", "bb") mustBe true
    itemsOps.compareLte("bb", "aa") mustBe false
  }

  new NumericTest[Byte](ItemsOperations.bytesOps, -5, 5)(ops => {
    ops.bitsSize mustBe 8

    ops.asLong(5) mustBe 5L
    ops.asLong(-5) mustBe -5L

    ops.getSlice(99, 2, 4) mustBe 8
    ops.getSlice(-99, 2, 4) mustBe 7
  }).register("Byte")

  new NumericTest[Short](ItemsOperations.shortOps, -5, 5)(ops => {
    ops.bitsSize mustBe 16

    ops.asLong(5) mustBe 5L
    ops.asLong(-5) mustBe -5L

    ops.getSlice(99, 2, 4) mustBe 8
    ops.getSlice(-99, 2, 4) mustBe 7
  }).register("Short")

  new NumericTest[Int](ItemsOperations.intOps, -5, 5)(ops => {
    ops.bitsSize mustBe 32

    ops.asLong(5) mustBe 5L
    ops.asLong(-5) mustBe -5L

    ops.getSlice(99, 2, 4) mustBe 8
    ops.getSlice(-99, 2, 4) mustBe 7
  }).register("Int")

  new NumericTest[Long](ItemsOperations.longOps, -5, 5)(ops => {
    ops.bitsSize mustBe 64

    ops.asLong(5) mustBe 5L
    ops.asLong(-5) mustBe -5L

    ops.getSlice(99, 2, 4) mustBe 8
    ops.getSlice(-99, 2, 4) mustBe 7
  }).register("Long")

  class NumericTest[@specialized(Group) Item: TypeTag](
      itemsOps: NumericItemsOperations[Item],
      lower: Item,
      higher: Item)(specializedTests: NumericItemsOperations[Item] => Unit) {
    def register(typeName: String): Unit = {
      typeBehavior[NumericItemsOperations[Item]]

      it must s"correctly compare $typeName values" in {
        itemsOps.compareEq(lower, lower) mustBe true
        itemsOps.compareEq(lower, higher) mustBe false
        itemsOps.compareEq(higher, lower) mustBe false

        itemsOps.compareGt(lower, lower) mustBe false
        itemsOps.compareGt(lower, higher) mustBe false
        itemsOps.compareGt(higher, lower) mustBe true

        itemsOps.compareGte(lower, lower) mustBe true
        itemsOps.compareGte(lower, higher) mustBe false
        itemsOps.compareGte(higher, lower) mustBe true

        itemsOps.compareLt(lower, lower) mustBe false
        itemsOps.compareLt(lower, higher) mustBe true
        itemsOps.compareLt(higher, lower) mustBe false

        itemsOps.compareLte(lower, lower) mustBe true
        itemsOps.compareLte(lower, higher) mustBe true
        itemsOps.compareLte(higher, lower) mustBe false

        specializedTests(itemsOps)
      }
    }
  }
}
