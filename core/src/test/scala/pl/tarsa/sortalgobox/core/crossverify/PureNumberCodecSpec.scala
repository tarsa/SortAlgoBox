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

  def havingSize(size: Int)(function: ByteBuffer => Unit)
    (contents: Byte*): Unit = {
    val buffer = allocate(size)
    function(buffer)
    assert(buffer.position() == contents.length)
    assert(buffer.flip() == wrap(contents.toArray))
  }

  def havingContents(contents: Byte*)(function: ByteBuffer => Unit)
    (position: Int): Unit = {
    val buffer = wrap(contents.toArray)
    function(buffer)
    assert(buffer.position() == position)
  }

  it should "fail on serializing negative int" in {
    havingSize(0) { buffer =>
      a[IllegalArgumentException] should be thrownBy {
        serializeInt(buffer, -1)
      }
    }()
  }

  it should "serialize int zero" in {
    havingSize(1) { buffer =>
      serializeInt(buffer, 0)
    }(contents = 0)
  }

  it should "serialize positive int" in {
    havingSize(9) { buffer =>
      serializeInt(buffer, 1234567)
    }(contents = -121, -83, 75)
  }

  it should "serialize max int" in {
    havingSize(9) { buffer =>
      serializeInt(buffer, Int.MaxValue)
    }(contents = -1, -1, -1, -1, 7)
  }

  it should "fail on int serialization when not enough remaining space" in {
    havingSize(2) { buffer =>
      a[BufferOverflowException] should be thrownBy {
        serializeInt(buffer, Int.MaxValue)
      }
    }(contents = -1, -1)
  }

  it should "fail on deserializing empty buffer for int" in {
    havingContents() { buffer =>
      a[BufferUnderflowException] should be thrownBy {
        deserializeInt(buffer)
      }
    }(position = 0)
  }

  it should "fail on deserializing unfinished negative sequence for int" in {
    havingContents(-1, -2, -3) { buffer =>
      a[BufferUnderflowException] should be thrownBy {
        deserializeInt(buffer)
      }
    }(position = 3)
  }

  it should "deserialize int zero" in {
    havingContents(0) { buffer =>
      assert(deserializeInt(buffer) == 0)
    }(position = 1)
  }

  it should "fully deserialize weirdly encoded int zero" in {
    havingContents(-128, -128, -128, -128, -128, -128, -128, 0) { buffer =>
      assert(deserializeInt(buffer) == 0)
    }(position = 8)
  }

  it should "deserialize positive int" in {
    havingContents(-121, -83, 75) { buffer =>
      assert(deserializeInt(buffer) == 1234567)
    }(position = 3)
  }

  it should "deserialize max int" in {
    havingContents(-1, -1, -1, -1, 7) { buffer =>
      assert(deserializeInt(buffer) == Int.MaxValue)
    }(position = 5)
  }

  it should "fail on deserialization of slightly too big number for int" in {
    havingContents(-1, -1, -1, -1, 8) { buffer =>
      a[NumberCodecException] should be thrownBy {
        deserializeInt(buffer)
      }
    }(position = 5)
  }

  it should "fail on deserialization of way too big number for int" in {
    havingContents(-1, -1, -1, -1, -1, 1) { buffer =>
      a[NumberCodecException] should be thrownBy {
        deserializeInt(buffer)
      }
    }(position = 5)
  }

  it should "fail on serializing negative long" in {
    havingSize(0) { buffer =>
      a[IllegalArgumentException] should be thrownBy {
        serializeLong(buffer, -1L)
      }
    }()
  }

  it should "serialize long zero" in {
    havingSize(1) { buffer =>
      serializeLong(buffer, 0L)
    }(contents = 0)
  }

  it should "serialize positive long" in {
    havingSize(9) { buffer =>
      serializeLong(buffer, 123456789000L)
    }(contents = -120, -76, -28, -12, -53, 3)
  }

  it should "serialize max long" in {
    havingSize(9) { buffer =>
      serializeLong(buffer, Long.MaxValue)
    }(contents = -1, -1, -1, -1, -1, -1, -1, -1, 127)
  }

  it should "fail on long serialization when not enough remaining space" in {
    havingSize(2) { buffer =>
      a[BufferOverflowException] should be thrownBy {
        serializeLong(buffer, Long.MaxValue)
      }
    }(-1, -1)
  }

  it should "fail on deserializing empty buffer for long" in {
    havingContents() { buffer =>
      a[BufferUnderflowException] should be thrownBy {
        deserializeLong(buffer)
      }
    }(position = 0)
  }

  it should "fail on deserializing unfinished negative sequence for long" in {
    havingContents(-1, -2, -3) { buffer =>
      a[BufferUnderflowException] should be thrownBy {
        deserializeLong(buffer)
      }
    }(position = 3)
  }

  it should "deserialize long zero" in {
    havingContents(0) { buffer =>
      assert(deserializeLong(buffer) == 0L)
    }(position = 1)
  }

  it should "fully deserialize weirdly encoded long zero" in {
    havingContents(-128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
      -128, -128, -128, -128, -128, -128, -128, -128, 0) { buffer =>
      assert(deserializeLong(buffer) == 0L)
    }(position = 19)
  }

  it should "deserialize positive long" in {
    havingContents(-120, -76, -28, -12, -53, 3) { buffer =>
      assert(deserializeLong(buffer) == 123456789000L)
    }(position = 6)
  }

  it should "deserialize max long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, 127) { buffer =>
      assert(deserializeLong(buffer) == Long.MaxValue)
    }(position = 9)
  }

  it should "fail on deserialization of slightly too big number for long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, 1) { buffer =>
      a[NumberCodecException] should be thrownBy {
        deserializeLong(buffer)
      }
    }(position = 10)
  }

  it should "fail on deserialization of way too big number for long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0) { buffer =>
      a[NumberCodecException] should be thrownBy {
        deserializeLong(buffer)
      }
    }(position = 10)
  }
}
