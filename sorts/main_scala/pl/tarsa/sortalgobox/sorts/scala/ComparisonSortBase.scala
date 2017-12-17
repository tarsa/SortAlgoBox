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
package pl.tarsa.sortalgobox.sorts.scala

import pl.tarsa.sortalgobox.core.common.ComparableItemsAgentSortAlgorithm
import pl.tarsa.sortalgobox.core.common.Specialization.Group
import pl.tarsa.sortalgobox.core.common.items.buffers.ComparableItemsBuffer
import scala.{specialized => spec}

abstract class ComparisonSortBase extends ComparableItemsAgentSortAlgorithm {
  class Setup[@spec(Group) Item: Permit](
      val buffer1: ComparableItemsBuffer[Item])

  override def setupSort[@spec(Group) Item: Ordering](
      items: Array[Item]): Setup[Item] =
    new Setup(ComparableItemsBuffer(0, items, 0))

  protected final def buf1[@spec(Group) Item: Setup]
    : ComparableItemsBuffer[Item] =
    setup[Item].buffer1
}
