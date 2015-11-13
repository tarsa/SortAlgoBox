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
package pl.tarsa.sortalgobox.core.crossverify

import java.nio.ByteBuffer._
import java.nio.{BufferOverflowException, BufferUnderflowException}

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class PureNumberCodecSpec extends CommonUnitSpecBase {
  typeBehavior[PureNumberCodec]

  def havingSize(size: Int)(function: PureNumberCodec => Unit)
    (contents: Byte*): Unit = {
    val buffer = allocate(size)
    val codec = new PureNumberCodec(buffer)
    function(codec)
    assert(buffer.position() == contents.length)
    assert(buffer.flip() == wrap(contents.toArray))
  }

  def havingContents(contents: Byte*)(function: PureNumberCodec => Unit)
    (position: Int): Unit = {
    val buffer = wrap(contents.toArray)
    val codec = new PureNumberCodec(buffer)
    function(codec)
    assert(buffer.position() == position)
  }

  it should "fail on serializing negative int" in {
    havingSize(0) { codec =>
      an[IllegalArgumentException] should be thrownBy {
        codec.serializeInt(-1)
      }
    }()
  }

  it should "serialize int zero" in {
    havingSize(1) { codec =>
      codec.serializeInt(0)
    }(contents = 0)
  }

  it should "serialize positive int" in {
    havingSize(9) { codec =>
      codec.serializeInt(1234567)
    }(contents = -121, -83, 75)
  }

  it should "serialize max int" in {
    havingSize(9) { codec =>
      codec.serializeInt(Int.MaxValue)
    }(contents = -1, -1, -1, -1, 7)
  }

  it should "fail on int serialization when not enough remaining space" in {
    havingSize(2) { codec =>
      a[BufferOverflowException] should be thrownBy {
        codec.serializeInt(Int.MaxValue)
      }
    }(contents = -1, -1)
  }

  it should "fail on deserialization of empty buffer for int" in {
    havingContents() { codec =>
      a[BufferUnderflowException] should be thrownBy {
        codec.deserializeInt()
      }
    }(position = 0)
  }

  it should "fail on deserialization of unfinished negative sequence for " +
    "int" in {
    havingContents(-1, -2, -3) { codec =>
      a[BufferUnderflowException] should be thrownBy {
        codec.deserializeInt()
      }
    }(position = 3)
  }

  it should "deserialize int zero" in {
    havingContents(0) { codec =>
      assert(codec.deserializeInt() == 0)
    }(position = 1)
  }

  it should "fully deserialize weirdly encoded int zero" in {
    havingContents(-128, -128, -128, -128, -128, -128, -128, 0) { codec =>
      assert(codec.deserializeInt() == 0)
    }(position = 8)
  }

  it should "deserialize positive int" in {
    havingContents(-121, -83, 75) { codec =>
      assert(codec.deserializeInt() == 1234567)
    }(position = 3)
  }

  it should "deserialize max int" in {
    havingContents(-1, -1, -1, -1, 7) { codec =>
      assert(codec.deserializeInt() == Int.MaxValue)
    }(position = 5)
  }

  it should "fail on deserialization of slightly too big number for int" in {
    havingContents(-1, -1, -1, -1, 8) { codec =>
      a[NumberCodecException] should be thrownBy {
        codec.deserializeInt()
      }
    }(position = 5)
  }

  it should "fail on deserialization of way too big number for int" in {
    havingContents(-1, -1, -1, -1, -1, 1) { codec =>
      a[NumberCodecException] should be thrownBy {
        codec.deserializeInt()
      }
    }(position = 5)
  }

  it should "fail on serializing negative long" in {
    havingSize(0) { codec =>
      an[IllegalArgumentException] should be thrownBy {
        codec.serializeLong(-1L)
      }
    }()
  }

  it should "serialize long zero" in {
    havingSize(1) { codec =>
      codec.serializeLong(0L)
    }(contents = 0)
  }

  it should "serialize positive long" in {
    havingSize(9) { codec =>
      codec.serializeLong(123456789000L)
    }(contents = -120, -76, -28, -12, -53, 3)
  }

  it should "serialize max long" in {
    havingSize(9) { codec =>
      codec.serializeLong(Long.MaxValue)
    }(contents = -1, -1, -1, -1, -1, -1, -1, -1, 127)
  }

  it should "fail on long serialization when not enough remaining space" in {
    havingSize(2) { codec =>
      a[BufferOverflowException] should be thrownBy {
        codec.serializeLong(Long.MaxValue)
      }
    }(-1, -1)
  }

  it should "fail on deserialization of empty buffer for long" in {
    havingContents() { codec =>
      a[BufferUnderflowException] should be thrownBy {
        codec.deserializeLong()
      }
    }(position = 0)
  }

  it should "fail on deserialization of unfinished negative sequence for " +
    "long" in {
    havingContents(-1, -2, -3) { codec =>
      a[BufferUnderflowException] should be thrownBy {
        codec.deserializeLong()
      }
    }(position = 3)
  }

  it should "deserialize long zero" in {
    havingContents(0) { codec =>
      assert(codec.deserializeLong() == 0L)
    }(position = 1)
  }

  it should "fully deserialize weirdly encoded long zero" in {
    havingContents(-128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
      -128, -128, -128, -128, -128, -128, -128, -128, 0) { codec =>
      assert(codec.deserializeLong() == 0L)
    }(position = 19)
  }

  it should "deserialize positive long" in {
    havingContents(-120, -76, -28, -12, -53, 3) { codec =>
      assert(codec.deserializeLong() == 123456789000L)
    }(position = 6)
  }

  it should "deserialize max long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, 127) { codec =>
      assert(codec.deserializeLong() == Long.MaxValue)
    }(position = 9)
  }

  it should "fail on deserialization of slightly too big number for long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, 1) { codec =>
      a[NumberCodecException] should be thrownBy {
        codec.deserializeLong()
      }
    }(position = 10)
  }

  it should "fail on deserialization of way too big number for long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0) { codec =>
      a[NumberCodecException] should be thrownBy {
        codec.deserializeLong()
      }
    }(position = 10)
  }
}
