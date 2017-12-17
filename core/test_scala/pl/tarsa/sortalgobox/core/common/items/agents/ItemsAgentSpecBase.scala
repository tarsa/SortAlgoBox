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
import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes._
import pl.tarsa.sortalgobox.core.common.items.buffers.{
  ComparableItemsBuffer,
  NumericItemsBuffer
}
import pl.tarsa.sortalgobox.core.common.items.operations.{
  ComparableItemsOperations,
  NumericItemsOperations
}
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.reflect.runtime.universe.TypeTag

abstract class ItemsAgentSpecBase[TestedClass: TypeTag]
    extends CommonUnitSpecBase {
  typeBehavior[TestedClass]

  it must "return size for empty buffer" in {
    readTestComparable() { (agent, buf) =>
      agent.size(buf) mustBe 0
    }(Size.id, bufferId, 0 * 2)
    readTestNumeric() { (agent, buf) =>
      agent.size(buf) mustBe 0
    }(Size.id, bufferId, 0 * 2)
  }

  it must "return size for non-empty buffer" in {
    readTestComparable(5, 2, 3, 8) { (agent, buf) =>
      agent.size(buf) mustBe 4
    }(Size.id, bufferId, 4 * 2)
    readTestNumeric(5, 2, 3, 8) { (agent, buf) =>
      agent.size(buf) mustBe 4
    }(Size.id, bufferId, 4 * 2)
  }

  it must "get items" in {
    readTestComparable(5, 2, 3, 8) { (agent, buf) =>
      agent.get(buf, 0) mustBe 5
      agent.get(buf, 1) mustBe 2
      agent.get(buf, 2) mustBe 3
      agent.get(buf, 3) mustBe 8
    }((0 to 3).flatMap(i => Seq(Get.id, bufferId, i * 2)): _*)
    readTestNumeric(5, 2, 3, 8) { (agent, buf) =>
      agent.get(buf, 0) mustBe 5
      agent.get(buf, 1) mustBe 2
      agent.get(buf, 2) mustBe 3
      agent.get(buf, 3) mustBe 8
    }((0 to 3).flatMap(i => Seq(Get.id, bufferId, i * 2)): _*)
  }

  it must "set items" in {
    writeTestComparable(5, 3, 2, 8) { (agent, buf) =>
      agent.set(buf, 3, 1)
      agent.set(buf, 1, 2)
      agent.set(buf, 0, 9)
      agent.set(buf, 3, 7)
    }(9, 2, 2, 7)(
      Seq(3, 1, 0, 3).flatMap(i => Seq(Set.id, bufferId, i * 2)): _*)
    writeTestNumeric(5, 3, 2, 8) { (agent, buf) =>
      agent.set(buf, 3, 1)
      agent.set(buf, 1, 2)
      agent.set(buf, 0, 9)
      agent.set(buf, 3, 7)
    }(9, 2, 2, 7)(
      Seq(3, 1, 0, 3).flatMap(i => Seq(Set.id, bufferId, i * 2)): _*)
  }

  it must "swap items" in {
    writeTestComparable(5, 3, 2, 8) { (agent, buf) =>
      agent.swap(buf, 3, 0)
    }(8, 3, 2, 5)(Swap.id, bufferId, 3 * 2, 0 * 2)
    writeTestNumeric(5, 3, 2, 8) { (agent, buf) =>
      agent.swap(buf, 3, 0)
    }(8, 3, 2, 5)(Swap.id, bufferId, 3 * 2, 0 * 2)
  }

  it must "return numeric item size in bits" in {
    readTestNumeric() { (agent, buf) =>
      agent.itemBitsSize(buf) mustBe 32
    }(ItemBitsSize.id, bufferId, 32)
  }

  it must "convert numeric item to Long" in {
    pureTestNumeric { (agent, ops) =>
      agent.asLong(ops, -5) mustBe -5L
      agent.asLong(ops, 5) mustBe 5L
    }(AsLong.id, -5 * -2 - 1, AsLong.id, 5 * 2)
    readTestNumeric(-5, 5) { (agent, buf) =>
      agent.asLongI(buf, 0) mustBe -5L
    }(Seq(Get.id, bufferId, 0 * 2, AsLong.id, -5 * -2 - 1): _*)
    readTestNumeric(-5, 5) { (agent, buf) =>
      agent.asLongI(buf, 1) mustBe 5L
    }(Seq(Get.id, bufferId, 1 * 2, AsLong.id, 5 * 2): _*)
  }

  it must "get slice of numeric item" in {
    pureTestNumeric { (agent, ops) =>
      agent.getSlice(ops, 99, 2, 4) mustBe 8
      agent.getSlice(ops, -99, 2, 4) mustBe 7
    }(GetSlice.id, 2, 4, 8 * 2, GetSlice.id, 2, 4, 7 * 2)
    readTestNumeric(-5, 5) { (agent, buf) =>
      agent.getSliceI(buf, 0, 1, 3) mustBe 5
      agent.getSliceI(buf, 1, 2, 3) mustBe 1
    }(Seq(Get.id, bufferId, 0 * 2, GetSlice.id, 1, 3, 5 * 2) ++
      Seq(Get.id, bufferId, 1 * 2, GetSlice.id, 2, 3, 1 * 2): _*)
  }

  it must "compare Comparable items" in {
    def opsTest(body: (ItemsAgent, ComparableItemsOperations[Int]) => Unit)(
        expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
      readTestComparable(5, 3, 2, 8, 5) { (agent, buf) =>
        body(agent, buf.itemsOps)
      }(expectedRecordedBytes: _*)
    }

    def bufTest(
        body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit,
        expectedRecordedBytes: Seq[Int])(implicit pos: Position): Unit = {
      readTestComparable(5, 3, 2, 8, 5) { (agent, buf) =>
        body(agent, buf)
      }(expectedRecordedBytes: _*)
    }

    opsTest(_.compareEq(_, 1, 2) mustBe false)(CompareEq.id, 0)
    opsTest(_.compareEq(_, 1, 1) mustBe true)(CompareEq.id, 1)
    opsTest(_.compareEq(_, 2, 1) mustBe false)(CompareEq.id, 0)

    bufTest(_.compareEqI(_, 0, 1) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareEq.id, 0))
    bufTest(_.compareEqI(_, 0, 0) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareEq.id, 1))
    bufTest(_.compareEqI(_, 1, 0) mustBe false,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareEq.id, 0))

    bufTest(_.compareEqIV(_, 0, 4) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))
    bufTest(_.compareEqIV(_, 0, 5) mustBe true,
            Seq(Get.id, bufferId, 0, CompareEq.id, 1))
    bufTest(_.compareEqIV(_, 0, 6) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))

    bufTest(_.compareEqVI(_, 4, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))
    bufTest(_.compareEqVI(_, 5, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareEq.id, 1))
    bufTest(_.compareEqVI(_, 6, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))

    opsTest(_.compareGt(_, 1, 2) mustBe false)(CompareGt.id, 0)
    opsTest(_.compareGt(_, 1, 1) mustBe false)(CompareGt.id, 0)
    opsTest(_.compareGt(_, 2, 1) mustBe true)(CompareGt.id, 1)

    bufTest(_.compareGtI(_, 0, 1) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareGt.id, 1))
    bufTest(_.compareGtI(_, 0, 0) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtI(_, 1, 0) mustBe false,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareGt.id, 0))

    bufTest(_.compareGtIV(_, 0, 4) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGt.id, 1))
    bufTest(_.compareGtIV(_, 0, 5) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtIV(_, 0, 6) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))

    bufTest(_.compareGtVI(_, 4, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtVI(_, 5, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtVI(_, 6, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGt.id, 1))

    opsTest(_.compareGte(_, 1, 2) mustBe false)(CompareGte.id, 0)
    opsTest(_.compareGte(_, 1, 1) mustBe true)(CompareGte.id, 1)
    opsTest(_.compareGte(_, 2, 1) mustBe true)(CompareGte.id, 1)

    bufTest(_.compareGteI(_, 0, 1) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareGte.id, 1))
    bufTest(_.compareGteI(_, 0, 0) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteI(_, 1, 0) mustBe false,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareGte.id, 0))

    bufTest(_.compareGteIV(_, 0, 4) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteIV(_, 0, 5) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteIV(_, 0, 6) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGte.id, 0))

    bufTest(_.compareGteVI(_, 4, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGte.id, 0))
    bufTest(_.compareGteVI(_, 5, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteVI(_, 6, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))

    opsTest(_.compareLt(_, 1, 2) mustBe true)(CompareLt.id, 1)
    opsTest(_.compareLt(_, 1, 1) mustBe false)(CompareLt.id, 0)
    opsTest(_.compareLt(_, 2, 1) mustBe false)(CompareLt.id, 0)

    bufTest(_.compareLtI(_, 0, 1) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareLt.id, 0))
    bufTest(_.compareLtI(_, 0, 0) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtI(_, 1, 0) mustBe true,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareLt.id, 1))

    bufTest(_.compareLtIV(_, 0, 4) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtIV(_, 0, 5) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtIV(_, 0, 6) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLt.id, 1))

    bufTest(_.compareLtVI(_, 4, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLt.id, 1))
    bufTest(_.compareLtVI(_, 5, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtVI(_, 6, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))

    opsTest(_.compareLte(_, 1, 2) mustBe true)(CompareLte.id, 1)
    opsTest(_.compareLte(_, 1, 1) mustBe true)(CompareLte.id, 1)
    opsTest(_.compareLte(_, 2, 1) mustBe false)(CompareLte.id, 0)

    bufTest(_.compareLteI(_, 0, 1) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareLte.id, 0))
    bufTest(_.compareLteI(_, 0, 0) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteI(_, 1, 0) mustBe true,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareLte.id, 1))

    bufTest(_.compareLteIV(_, 0, 4) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLte.id, 0))
    bufTest(_.compareLteIV(_, 0, 5) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteIV(_, 0, 6) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))

    bufTest(_.compareLteVI(_, 4, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteVI(_, 5, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteVI(_, 6, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLte.id, 0))
  }

  it must "compare Numeric items" in {
    def opsTest(body: (ItemsAgent, NumericItemsOperations[Int]) => Unit)(
        expectedRecordedBytes: Int*)(implicit pos: Position): Unit = {
      readTestNumeric(5, 3, 2, 8, 5) { (agent, buf) =>
        body(agent, buf.itemsOps)
      }(expectedRecordedBytes: _*)
    }

    def bufTest(
        body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit,
        expectedRecordedBytes: Seq[Int])(implicit pos: Position): Unit = {
      readTestNumeric(5, 3, 2, 8, 5) { (agent, buf) =>
        body(agent, buf)
      }(expectedRecordedBytes: _*)
    }

    opsTest(_.compareEq(_, 1, 2) mustBe false)(CompareEq.id, 0)
    opsTest(_.compareEq(_, 1, 1) mustBe true)(CompareEq.id, 1)
    opsTest(_.compareEq(_, 2, 1) mustBe false)(CompareEq.id, 0)

    bufTest(_.compareEqI(_, 0, 1) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareEq.id, 0))
    bufTest(_.compareEqI(_, 0, 0) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareEq.id, 1))
    bufTest(_.compareEqI(_, 1, 0) mustBe false,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareEq.id, 0))

    bufTest(_.compareEqIV(_, 0, 4) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))
    bufTest(_.compareEqIV(_, 0, 5) mustBe true,
            Seq(Get.id, bufferId, 0, CompareEq.id, 1))
    bufTest(_.compareEqIV(_, 0, 6) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))

    bufTest(_.compareEqVI(_, 4, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))
    bufTest(_.compareEqVI(_, 5, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareEq.id, 1))
    bufTest(_.compareEqVI(_, 6, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareEq.id, 0))

    opsTest(_.compareGt(_, 1, 2) mustBe false)(CompareGt.id, 0)
    opsTest(_.compareGt(_, 1, 1) mustBe false)(CompareGt.id, 0)
    opsTest(_.compareGt(_, 2, 1) mustBe true)(CompareGt.id, 1)

    bufTest(_.compareGtI(_, 0, 1) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareGt.id, 1))
    bufTest(_.compareGtI(_, 0, 0) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtI(_, 1, 0) mustBe false,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareGt.id, 0))

    bufTest(_.compareGtIV(_, 0, 4) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGt.id, 1))
    bufTest(_.compareGtIV(_, 0, 5) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtIV(_, 0, 6) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))

    bufTest(_.compareGtVI(_, 4, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtVI(_, 5, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGt.id, 0))
    bufTest(_.compareGtVI(_, 6, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGt.id, 1))

    opsTest(_.compareGte(_, 1, 2) mustBe false)(CompareGte.id, 0)
    opsTest(_.compareGte(_, 1, 1) mustBe true)(CompareGte.id, 1)
    opsTest(_.compareGte(_, 2, 1) mustBe true)(CompareGte.id, 1)

    bufTest(_.compareGteI(_, 0, 1) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareGte.id, 1))
    bufTest(_.compareGteI(_, 0, 0) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteI(_, 1, 0) mustBe false,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareGte.id, 0))

    bufTest(_.compareGteIV(_, 0, 4) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteIV(_, 0, 5) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteIV(_, 0, 6) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGte.id, 0))

    bufTest(_.compareGteVI(_, 4, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareGte.id, 0))
    bufTest(_.compareGteVI(_, 5, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))
    bufTest(_.compareGteVI(_, 6, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareGte.id, 1))

    opsTest(_.compareLt(_, 1, 2) mustBe true)(CompareLt.id, 1)
    opsTest(_.compareLt(_, 1, 1) mustBe false)(CompareLt.id, 0)
    opsTest(_.compareLt(_, 2, 1) mustBe false)(CompareLt.id, 0)

    bufTest(_.compareLtI(_, 0, 1) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareLt.id, 0))
    bufTest(_.compareLtI(_, 0, 0) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtI(_, 1, 0) mustBe true,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareLt.id, 1))

    bufTest(_.compareLtIV(_, 0, 4) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtIV(_, 0, 5) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtIV(_, 0, 6) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLt.id, 1))

    bufTest(_.compareLtVI(_, 4, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLt.id, 1))
    bufTest(_.compareLtVI(_, 5, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))
    bufTest(_.compareLtVI(_, 6, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLt.id, 0))

    opsTest(_.compareLte(_, 1, 2) mustBe true)(CompareLte.id, 1)
    opsTest(_.compareLte(_, 1, 1) mustBe true)(CompareLte.id, 1)
    opsTest(_.compareLte(_, 2, 1) mustBe false)(CompareLte.id, 0)

    bufTest(_.compareLteI(_, 0, 1) mustBe false,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 2, CompareLte.id, 0))
    bufTest(_.compareLteI(_, 0, 0) mustBe true,
            Seq(Get.id, bufferId, 0, Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteI(_, 1, 0) mustBe true,
            Seq(Get.id, bufferId, 2, Get.id, bufferId, 0, CompareLte.id, 1))

    bufTest(_.compareLteIV(_, 0, 4) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLte.id, 0))
    bufTest(_.compareLteIV(_, 0, 5) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteIV(_, 0, 6) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))

    bufTest(_.compareLteVI(_, 4, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteVI(_, 5, 0) mustBe true,
            Seq(Get.id, bufferId, 0, CompareLte.id, 1))
    bufTest(_.compareLteVI(_, 6, 0) mustBe false,
            Seq(Get.id, bufferId, 0, CompareLte.id, 0))
  }

  def pureTestComparable(
      body: (ItemsAgent, ComparableItemsOperations[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit

  def pureTestNumeric(body: (ItemsAgent, NumericItemsOperations[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit

  def readTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit

  def readTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit

  def writeTestComparable(inputItems: Int*)(
      body: (ItemsAgent, ComparableItemsBuffer[Int]) => Unit)(
      outputItems: Int*)(expectedRecordedBytes: Int*)(
      implicit pos: Position): Unit

  def writeTestNumeric(inputItems: Int*)(
      body: (ItemsAgent, NumericItemsBuffer[Int]) => Unit)(outputItems: Int*)(
      expectedRecordedBytes: Int*)(implicit pos: Position): Unit

  // TODO make a def?
  val bufferId: Byte = 11
}
