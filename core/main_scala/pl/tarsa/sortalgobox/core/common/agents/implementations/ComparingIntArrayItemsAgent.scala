/*
 * Copyright (C) 2015 Piotr Tarsa ( http://github.com/tarsa )
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
 *
 */
package pl.tarsa.sortalgobox.core.common.agents.implementations

import pl.tarsa.sortalgobox.core.common.agents.ComparingItemsAgent

class ComparingIntArrayItemsAgent(items: Array[Int])
  extends ComparingItemsAgent[Int] {

  override def size0: Int = items.length

  override def get0(i: Int): Int = items(i)

  override def set0(i: Int, v: Int): Unit = items(i) = v

  override def copy0(i: Int, j: Int, n: Int): Unit =
    System.arraycopy(items, i, items, j, n)

  override def compare(a: Int, b: Int): Int = Ordering.Int.compare(a, b)
}
