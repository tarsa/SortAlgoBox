/**
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
package pl.tarsa.sortalgobox.random

import org.apache.commons.math3.random._

case class Mwc64x(var state: Long = Mwc64x.initialState)
  extends BitsStreamGenerator {

  override def next(bits: Int): Int = {
    val c = state >>> 32
    val x = state & 0xFFFFFFFFL
    state = Mwc64x.aLong * x + c
    (x ^ c).toInt
  }

  override def setSeed(seed: Int): Unit = setSeed(Array(seed))

  override def setSeed(seed: Array[Int]): Unit = setSeed(
    RandomGeneratorFactory.convertToLong(seed))

  override def setSeed(seed: Long): Unit = {
    state = seed
  }

  def skip(distance: Long): Unit = {
    state = Mwc64x.skip(state, distance)
  }
}

object Mwc64x {
  val initialState = 1745055044494293084L
  val aBig = BigInt("4294883355")
  val aLong = aBig.toLong
  val mBig = BigInt("18446383549859758079")

  def skip(state: Long, distance: Long): Long = {
    val c = state >>> 32
    val x = state & 0xFFFFFFFFL
    val m = aBig.modPow(distance, mBig)
    val v1 = aBig * x + c
    val v2 = v1 * m % mBig
    val (x1, c1) = v2 /% aBig
    (c1.toLong << 32) + x1.toLong
  }
}
