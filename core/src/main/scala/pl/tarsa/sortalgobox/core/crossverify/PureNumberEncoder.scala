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

import java.io.OutputStream

import scala.annotation.tailrec

class PureNumberEncoder(output: OutputStream) {
  def serializeInt(value: Int): Unit = {
    @tailrec
    def serialize(value: Int): Unit = {
      if (value < 0) {
        throw new IllegalArgumentException
      } else if (value <= Byte.MaxValue) {
        output.write(value)
      } else {
        output.write((value & 127) - 128)
        serialize(value >> 7)
      }
    }
    serialize(value)
  }

  def serializeLong(value: Long): Unit = {
    @tailrec
    def serialize(value: Long): Unit = {
      if (value < 0) {
        throw new IllegalArgumentException
      } else if (value <= Byte.MaxValue) {
        output.write(value.toByte)
      } else {
        output.write(((value & 127) - 128).toByte)
        serialize(value >> 7)
      }
    }
    serialize(value)
  }
}
