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

import java.io.ByteArrayInputStream

import org.apache.commons.io.input.CountingInputStream
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class PureNumberDecoderSpec extends CommonUnitSpecBase {
  typeBehavior[PureNumberDecoder]

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
      assert(codec.deserializeInt() == 0)
    }(position = 1)
  }

  it must "fully deserialize weirdly encoded int zero" in {
    havingContents(-128, -128, -128, -128, -128, -128, -128, 0) { codec =>
      assert(codec.deserializeInt() == 0)
    }(position = 8)
  }

  it must "deserialize positive int" in {
    havingContents(-121, -83, 75) { codec =>
      assert(codec.deserializeInt() == 1234567)
    }(position = 3)
  }

  it must "deserialize max int" in {
    havingContents(-1, -1, -1, -1, 7) { codec =>
      assert(codec.deserializeInt() == Int.MaxValue)
    }(position = 5)
  }

  it must "fail on deserialization of slightly too big number for int" in {
    havingContents(-1, -1, -1, -1, 8) { codec =>
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
      assert(codec.deserializeLong() == 0L)
    }(position = 1)
  }

  it must "fully deserialize weirdly encoded long zero" in {
    havingContents(-128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
      -128, -128, -128, -128, -128, -128, -128, -128, 0) { codec =>
      assert(codec.deserializeLong() == 0L)
    }(position = 19)
  }

  it must "deserialize positive long" in {
    havingContents(-120, -76, -28, -12, -53, 3) { codec =>
      assert(codec.deserializeLong() == 123456789000L)
    }(position = 6)
  }

  it must "deserialize max long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, 127) { codec =>
      assert(codec.deserializeLong() == Long.MaxValue)
    }(position = 9)
  }

  it must "fail on deserialization of slightly too big number for long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, 1) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeLong()
      }
    }(position = 10)
  }

  it must "fail on deserialization of way too big number for long" in {
    havingContents(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0) { codec =>
      a[ValueOverflowException] mustBe thrownBy {
        codec.deserializeLong()
      }
    }(position = 10)
  }

  def havingContents(contents: Byte*)(function: PureNumberDecoder => Unit)
    (position: Int): Unit = {
    val stream = new CountingInputStream(new ByteArrayInputStream(
      contents.toArray))
    val codec = new PureNumberDecoder(stream)
    try {
      function(codec)
    } finally {
      assert(stream.getCount == position)
    }
  }
}
