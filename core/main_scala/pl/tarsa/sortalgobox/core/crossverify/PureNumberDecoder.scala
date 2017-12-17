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
package pl.tarsa.sortalgobox.core.crossverify

import java.io.InputStream

import scala.annotation.tailrec

class PureNumberDecoder(input: InputStream) {
  def deserializeBit(): Boolean = {
    val value = input.read()
    if (value == -1) {
      throw new PrematureEndOfInputException
    }
    if (value > 1) {
      throw new ValueOverflowException
    }
    value == 1
  }

  def deserializeByte(): Byte = {
    val value = input.read()
    if (value == -1) {
      throw new PrematureEndOfInputException
    }
    value.toByte
  }

  def deserializeInt(): Int = {
    @tailrec
    def deserialize(current: Int, shift: Int): Int = {
      val value = input.read()
      if (value == -1) {
        throw new PrematureEndOfInputException
      } else {
        val chunk = value & 127
        if (shift > 31 || (shift > 0 && chunk >= (1 << (32 - shift)))) {
          throw new ValueOverflowException
        }
        if (value <= 127) {
          (chunk << shift) + current
        } else {
          deserialize((chunk << shift) + current, shift + 7)
        }
      }
    }
    val shifted = deserialize(0, 0)
    val signFlag = (shifted & 1) == 1
    val absValue = shifted >>> 1
    if (signFlag) ~absValue else absValue
  }

  def deserializeLong(): Long = {
    @tailrec
    def deserialize(current: Long, shift: Int): Long = {
      val value = input.read()
      if (value == -1) {
        throw new PrematureEndOfInputException
      } else {
        val chunk = value & 127L
        if (shift > 63 || (shift > 0 && chunk >= (1L << (64 - shift)))) {
          throw new ValueOverflowException
        }
        if (value <= 127) {
          (chunk << shift) + current
        } else {
          deserialize((chunk << shift) + current, shift + 7)
        }
      }
    }
    val shifted = deserialize(0, 0)
    val signFlag = (shifted & 1) == 1
    val absValue = shifted >>> 1
    if (signFlag) ~absValue else absValue
  }
}
