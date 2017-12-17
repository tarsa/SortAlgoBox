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

import java.io.ByteArrayInputStream

import org.apache.commons.io.input.CountingInputStream
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class PureNumberDecoderSpec extends CommonUnitSpecBase {
  typeBehavior[PureNumberDecoder]

  it must "deserialize false value" in {
    havingContents(0) { codec =>
      codec.deserializeBit() mustBe false
    }(position = 1)
  }

  it must "deserialize true value" in {
    havingContents(1) { codec =>
      codec.deserializeBit() mustBe true
    }(position = 1)
  }

  it must "fail on unexpected value when deserializing boolean" in {
    havingContents(8) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeBit()
      }
    }(position = 1)
  }

  it must "deserialize positive byte" in {
    havingContents(100) { codec =>
      codec.deserializeByte() mustBe 100
    }(position = 1)
  }

  it must "deserialize negative byte" in {
    havingContents(-100) { codec =>
      codec.deserializeByte() mustBe -100
    }(position = 1)
  }

  it must "fail on deserialization of empty buffer for int" in {
    havingContents() { codec =>
      a[PrematureEndOfInputException] mustBe thrownBy {
        codec.deserializeInt()
      }
    }(position = 0)
  }

  it must "fail on deserialization of unfinished negative sequence for " +
    "int" in {
    havingContents(-1, -2, -3) { codec =>
      a[PrematureEndOfInputException] mustBe thrownBy {
        codec.deserializeInt()
      }
    }(position = 3)
  }

  it must "deserialize int zero" in {
    havingContents(0) { codec =>
      codec.deserializeInt() mustBe 0
    }(position = 1)
  }

  it must "deserialize positive int" in {
    havingContents(-114, -38, -106, 1) { codec =>
      codec.deserializeInt() mustBe 1234567
    }(position = 4)
  }

  it must "deserialize negative int" in {
    havingContents(-115, -38, -106, 1) { codec =>
      codec.deserializeInt() mustBe -1234567
    }(position = 4)
  }

  it must "deserialize Int.MaxValue" in {
    havingContents(-2, -1, -1, -1, 15) { codec =>
      codec.deserializeInt() mustBe Int.MaxValue
    }(position = 5)
  }

  it must "deserialize Int.MinValue" in {
    havingContents(-1, -1, -1, -1, 15) { codec =>
      codec.deserializeInt() mustBe Int.MinValue
    }(position = 5)
  }

  it must "fail on deserialization of slightly too big number for int" in {
    havingContents(-2, -1, -1, -1, 16) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeInt()
      }
    }(position = 5)
  }

  it must "fail on deserialization of way too big number for int" in {
    havingContents(-1, -1, -1, -1, -1, 1) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeInt()
      }
    }(position = 5)
  }

  it must "fail on deserialization of empty buffer for long" in {
    havingContents() { codec =>
      a[PrematureEndOfInputException] mustBe thrownBy {
        codec.deserializeLong()
      }
    }(position = 0)
  }

  it must "fail on deserialization of unfinished negative sequence for " +
    "long" in {
    havingContents(-1, -2, -3) { codec =>
      a[PrematureEndOfInputException] mustBe thrownBy {
        codec.deserializeLong()
      }
    }(position = 3)
  }

  it must "deserialize long zero" in {
    havingContents(0) { codec =>
      codec.deserializeLong() mustBe 0L
    }(position = 1)
  }

  it must "deserialize positive long" in {
    havingContents(-112, -24, -56, -23, -105, 7) { codec =>
      codec.deserializeLong() mustBe 123456789000L
    }(position = 6)
  }

  it must "deserialize negative long" in {
    havingContents(-113, -24, -56, -23, -105, 7) { codec =>
      codec.deserializeLong() mustBe -123456789000L
    }(position = 6)
  }

  it must "deserialize Long.MaxValue" in {
    havingContents(-2, -1, -1, -1, -1, -1, -1, -1, -1, 1) { codec =>
      codec.deserializeLong() mustBe Long.MaxValue
    }(position = 10)
  }

  it must "deserialize Long.MinValue" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, 1) { codec =>
      codec.deserializeLong() mustBe Long.MinValue
    }(position = 10)
  }

  it must "fail on deserialization of slightly too big number for long" in {
    havingContents(-2, -1, -1, -1, -1, -1, -1, -1, -1, 3) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeLong()
      }
    }(position = 10)
  }

  it must "fail on deserialization of way too big number for long" in {
    havingContents(-2, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeLong()
      }
    }(position = 10)
  }

  def havingContents(contents: Byte*)(function: PureNumberDecoder => Unit)(
      position: Int): Unit = {
    val stream = new CountingInputStream(
      new ByteArrayInputStream(contents.toArray))
    val codec = new PureNumberDecoder(stream)
    try {
      function(codec)
    } finally {
      stream.getCount mustBe position
    }
  }
}
