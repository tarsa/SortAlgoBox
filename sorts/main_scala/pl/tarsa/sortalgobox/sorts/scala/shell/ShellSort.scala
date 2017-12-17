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
package pl.tarsa.sortalgobox.sorts.scala.shell

import pl.tarsa.sortalgobox.core.common.Specialization.Group
import pl.tarsa.sortalgobox.sorts.scala.ComparisonSortBase

class ShellSort(gapSequence: GapSequence) extends ComparisonSortBase {
  override def sort[@specialized(Group) Item: Setup, _: Agent](): Unit = {
    val buf = buf1[Item]

    for (gap <- gapSequence.forSize(a.size(buf));
         i <- gap until a.size(buf)) {
      var j = i
      while (j >= gap && a.compareGtI(buf, j - gap, j)) {
        a.swap(buf, j - gap, j)
        j -= gap
      }
    }
  }
}
