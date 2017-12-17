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
import pl.tarsa.sortalgobox.core.common.items.buffers.NumericItemsBuffer.Evidence
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class NumericItemsBufferSpec extends CommonUnitSpecBase {

  new Test[Byte](5, 2, 3, 8)(2, 8, 3, 5).register("Byte")

  new Test[Short](5, 2, 3, 8)(2, 8, 3, 5).register("Short")

  new Test[Int](5, 2, 3, 8)(2, 8, 3, 5).register("Int")

  new Test[Long](5, 2, 3, 8)(2, 8, 3, 5).register("Long")

  class Test[@specialized(Group) Item: Evidence: ClassTag: TypeTag](
      before: Item*)(after: Item*) {
    def register(typeName: String): Unit = {
      typeBehavior[NumericItemsBuffer[Item]]

      it must s"correctly handle $typeName buffer with base 0" in {
        val array = before.toArray.clone()
        val buffer = NumericItemsBuffer(5, array, 0)

        buffer.id mustBe 5
        buffer.base mustBe 0
        buffer.length mustBe array.length

        array.indices.foreach { i =>
          buffer.get(i) mustBe array(i)
        }

        val tmp1 = buffer.get(0)
        val tmp2 = buffer.get(1)
        val tmp3 = buffer.get(3)
        buffer.set(0, tmp2)
        buffer.set(1, tmp3)
        buffer.set(3, tmp1)

        array must contain theSameElementsInOrderAs after

        an[ArrayIndexOutOfBoundsException] mustBe thrownBy { buffer.get(-1) }
        an[ArrayIndexOutOfBoundsException] mustBe thrownBy {
          buffer.get(array.length)
        }
      }

      it must s"correctly handle $typeName buffer with base 1" in {
        val array = before.toArray.clone()
        val buffer = NumericItemsBuffer(5, array, 1)

        buffer.id mustBe 5
        buffer.base mustBe 1
        buffer.length mustBe array.length

        array.indices.foreach { i =>
          buffer.get(i + 1) mustBe array(i)
        }

        val tmp1 = buffer.get(1)
        val tmp2 = buffer.get(2)
        val tmp3 = buffer.get(4)
        buffer.set(1, tmp2)
        buffer.set(2, tmp3)
        buffer.set(4, tmp1)

        array must contain theSameElementsInOrderAs after

        an[ArrayIndexOutOfBoundsException] mustBe thrownBy { buffer.get(0) }
        an[ArrayIndexOutOfBoundsException] mustBe thrownBy {
          buffer.get(array.length + 1)
        }
      }
    }
  }
}
