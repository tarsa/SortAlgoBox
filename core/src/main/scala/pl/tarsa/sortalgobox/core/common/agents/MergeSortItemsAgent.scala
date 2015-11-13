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
package pl.tarsa.sortalgobox.core.common.agents

abstract class MergeSortItemsAgent[ItemType]
  extends ComparingItemsAgent[ItemType] {

  def size1: Int

  def get1(i: Int): ItemType

  def set1(i: Int, v: ItemType): Unit

  def swap00(i: Int, j: Int): Unit = {
    val temp = get0(i)
    set0(i, get0(j))
    set0(j, temp)
  }

  def swap01(i: Int, j: Int): Unit = {
    val temp = get0(i)
    set0(i, get1(j))
    set1(j, temp)
  }

  def swap10(i: Int, j: Int): Unit = {
    val temp = get1(i)
    set1(i, get0(j))
    set0(j, temp)
  }

  def swap11(i: Int, j: Int): Unit = {
    val temp = get1(i)
    set1(i, get1(j))
    set1(j, temp)
  }

  def compare00(i: Int, j: Int): Int = compare(get0(i), get0(j))

  def compare01(i: Int, j: Int): Int = compare(get0(i), get1(j))

  def compare10(i: Int, j: Int): Int = compare(get1(i), get0(j))

  def compare11(i: Int, j: Int): Int = compare(get1(i), get1(j))

  def copy00(i: Int, j: Int, n: Int): Unit

  def copy01(i: Int, j: Int, n: Int): Unit

  def copy10(i: Int, j: Int, n: Int): Unit

  def copy11(i: Int, j: Int, n: Int): Unit
}
