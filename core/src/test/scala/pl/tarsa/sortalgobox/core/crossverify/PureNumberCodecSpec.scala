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
package pl.tarsa.sortalgobox.core.crossverify

import java.nio.{BufferUnderflowException, BufferOverflowException, ByteBuffer}
import java.nio.ByteBuffer._

import pl.tarsa.sortalgobox.core.crossverify.PureNumberCodec._
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class PureNumberCodecSpec extends CommonUnitSpecBase {
  typeBehavior[PureNumberCodec.type]

  def showBuffer(buffer: ByteBuffer): Unit = {
    println(buffer.array().deep)
  }

  it should "fail on serializing negative int" in {
    val buffer = allocate(0)
    a [IllegalArgumentException] should be thrownBy {
      serializeInt(buffer, -1)
    }
    assert(buffer.position() == 0)
  }

  it should "serialize int zero" in {
    val buffer = allocate(1)
    serializeInt(buffer, 0)
    assert(buffer.position() == 1)
    assert(buffer.flip() == wrap(Array[Byte](0)))
  }

  it should "serialize positive int" in {
    val buffer = allocate(9)
    serializeInt(buffer, 1234567)
    assert(buffer.position() == 3)
    assert(buffer.flip() == wrap(Array[Byte](-121, -83, 75)))
  }

  it should "serialize max int" in {
    val buffer = allocate(9)
    serializeInt(buffer, Int.MaxValue)
    assert(buffer.position() == 5)
    assert(buffer.flip() == wrap(Array[Byte](-1, -1, -1, -1, 7)))
  }

  it should "fail on int serialization when not enough remaining space" in {
    val buffer = allocate(2)
    a [BufferOverflowException] should be thrownBy {
      serializeInt(buffer, Int.MaxValue)
    }
    assert(buffer.position() == 2)
  }

  it should "fail on deserializing empty buffer for int" in {
    val buffer = allocate(0)
    a [BufferUnderflowException] should be thrownBy {
      deserializeInt(buffer)
    }
    assert(buffer.position() == 0)
  }

  it should "fail on deserializing unfinished negative sequence for int" in {
    val buffer = wrap(Array[Byte](-1, -2, -3))
    a [BufferUnderflowException] should be thrownBy {
      deserializeInt(buffer)
    }
    assert(buffer.remaining() == 0)
  }

  it should "deserialize int zero" in {
    val buffer = wrap(Array[Byte](0))
    assert(deserializeInt(buffer) == 0)
    assert(buffer.position() == 1)
  }

  it should "fully deserialize weirdly encoded int zero" in {
    val buffer = wrap(Array[Byte](-128, -128, -128, -128, -128, -128, -128, 0))
    assert(deserializeInt(buffer) == 0)
    assert(buffer.position() == 8)
  }

  it should "deserialize positive int" in {
    val buffer = wrap(Array[Byte](-121, -83, 75))
    assert(deserializeInt(buffer) == 1234567)
    assert(buffer.position() == 3)
  }

  it should "deserialize max int" in {
    val buffer = wrap(Array[Byte](-1, -1, -1, -1, 7))
    assert(deserializeInt(buffer) == Int.MaxValue)
    assert(buffer.position() == 5)
  }

  it should "fail on deserialization of slightly too big number for int" in {
    val buffer = wrap(Array[Byte](-1, -1, -1, -1, 8))
    a [NumberCodecException] should be thrownBy {
      deserializeInt(buffer)
    }
    assert(buffer.position() == 5)
  }

  it should "fail on deserialization of way too big number for int" in {
    val buffer = wrap(Array[Byte](-1, -1, -1, -1, -1, 1))
    a [NumberCodecException] should be thrownBy {
      deserializeInt(buffer)
    }
    assert(buffer.position() == 5)
  }

  it should "fail on serializing negative long" in {
    val buffer = allocate(0)
    a [IllegalArgumentException] should be thrownBy {
      serializeLong(buffer, -1L)
    }
    assert(buffer.position() == 0)
  }

  it should "serialize long zero" in {
    val buffer = allocate(1)
    serializeLong(buffer, 0L)
    assert(buffer.position() == 1)
    assert(buffer.flip() == wrap(Array[Byte](0)))
  }

  it should "serialize positive long" in {
    val buffer = allocate(9)
    serializeLong(buffer, 123456789000L)
    assert(buffer.flip() == wrap(Array[Byte](-120, -76, -28, -12, -53, 3)))
  }

  it should "serialize max long" in {
    val buffer = allocate(9)
    serializeLong(buffer, Long.MaxValue)
    assert(buffer.position() == 9)
    assert(buffer.flip() ==
      wrap(Array[Byte](-1, -1, -1, -1, -1, -1, -1, -1, 127)))
  }

  it should "fail on long serialization when not enough remaining space" in {
    val buffer = allocate(2)
    a [BufferOverflowException] should be thrownBy {
      serializeLong(buffer, Long.MaxValue)
    }
    assert(buffer.position() == 2)
  }

  it should "fail on deserializing empty buffer for long" in {
    val buffer = allocate(0)
    a [BufferUnderflowException] should be thrownBy {
      deserializeLong(buffer)
    }
    assert(buffer.position() == 0)
  }

  it should "fail on deserializing unfinished negative sequence for long" in {
    val buffer = wrap(Array[Byte](-1, -2, -3))
    a [BufferUnderflowException] should be thrownBy {
      deserializeLong(buffer)
    }
    assert(buffer.remaining() == 0)
  }

  it should "deserialize long zero" in {
    val buffer = wrap(Array[Byte](0))
    assert(deserializeLong(buffer) == 0L)
    assert(buffer.position() == 1)
  }

  it should "fully deserialize weirdly encoded long zero" in {
    val buffer = wrap(Array[Byte](-128, -128, -128, -128, -128, -128, -128,
      -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, 0))
    assert(deserializeLong(buffer) == 0L)
    assert(buffer.position() == 19)
  }

  it should "deserialize positive long" in {
    val buffer = wrap(Array[Byte](-120, -76, -28, -12, -53, 3))
    assert(deserializeLong(buffer) == 123456789000L)
    assert(buffer.position() == 6)
  }

  it should "deserialize max long" in {
    val buffer = wrap(Array[Byte](-1, -1, -1, -1, -1, -1, -1, -1, 127))
    assert(deserializeLong(buffer) == Long.MaxValue)
    assert(buffer.position() == 9)
  }

  it should "fail on deserialization of slightly too big number for long" in {
    val buffer = wrap(Array[Byte](-1, -1, -1, -1, -1, -1, -1, -1, -1, 1))
    a [NumberCodecException] should be thrownBy {
      deserializeLong(buffer)
    }
    assert(buffer.position() == 10)
  }

  it should "fail on deserialization of way too big number for long" in {
    val buffer = wrap(Array[Byte](-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0))
    a [NumberCodecException] should be thrownBy {
      deserializeLong(buffer)
    }
    assert(buffer.position() == 10)
  }
}
