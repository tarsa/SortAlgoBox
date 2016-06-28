/*
 * Copyright (C) 2015, 2016 Piotr Tarsa ( http://github.com/tarsa )
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

package pl.tarsa.sortalgobox.core.crossverify

import java.io.InputStream

import scala.annotation.tailrec

class PureNumberDecoder(input: InputStream) {
  def deserializeInt(): Int = {
    @tailrec
    def deserialize(current: Int, shift: Int): Int = {
      val value = input.read()
      if (value == -1) {
        throw new PrematureEndOfInputException
      } else if (value == 0) {
        current
      } else {
        val chunk = value & 127
        if (chunk != 0 && (shift > 31 || chunk > (Int.MaxValue >> shift))) {
          throw new ValueOverflowException
        }
        if (value <= 127) {
          (chunk << shift) + current
        } else {
          deserialize((chunk << shift) + current, shift + 7)
        }
      }
    }
    deserialize(0, 0)
  }

  def deserializeLong(): Long = {
    @tailrec
    def deserialize(current: Long, shift: Int): Long = {
      val value = input.read()
      if (value == -1) {
        throw new PrematureEndOfInputException
      } else if (value == 0) {
        current
      } else {
        val chunk = value & 127L
        if (chunk != 0 && (shift > 63 || chunk > (Long.MaxValue >> shift))) {
          throw new ValueOverflowException
        }
        if (value <= 127) {
          (chunk << shift) + current
        } else {
          deserialize((chunk << shift) + current, shift + 7)
        }
      }
    }
    deserialize(0, 0)
  }
}
