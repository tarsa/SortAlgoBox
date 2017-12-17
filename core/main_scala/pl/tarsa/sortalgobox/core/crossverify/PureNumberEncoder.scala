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

import java.io.OutputStream

import scala.annotation.tailrec

class PureNumberEncoder(output: OutputStream) {
  def serializeBit(value: Boolean): Unit = {
    val byte: Byte = if (value) 1 else 0
    output.write(byte)
  }

  def serializeByte(value: Byte): Unit =
    output.write(value)

  def serializeInt(original: Int): Unit = {
    @tailrec
    def serialize(value: Int): Unit = {
      if ((value >>> 7) == 0) {
        output.write(value.toByte)
      } else {
        output.write((value.toByte & 127) - 128)
        serialize(value >>> 7)
      }
    }
    val value = (original.abs << 1) - (original >>> 31)
    serialize(value)
  }

  def serializeLong(original: Long): Unit = {
    @tailrec
    def serialize(value: Long): Unit = {
      if ((value >>> 7) == 0) {
        output.write(value.toByte)
      } else {
        output.write((value.toByte & 127) - 128)
        serialize(value >>> 7)
      }
    }
    val value = (original.abs << 1) - (original >>> 63)
    serialize(value)
  }
}
