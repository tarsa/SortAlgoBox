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

import java.io.{ByteArrayOutputStream, OutputStream}

import org.apache.commons.io.output.ThresholdingOutputStream
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class PureNumberEncoderSpec extends CommonUnitSpecBase {
  typeBehavior[PureNumberEncoder]

  class OverflowException extends Exception

  it must "serialize false value" in {
    havingSize() { codec =>
      codec.serializeBit(false)
    }(contents = 0)
  }

  it must "serialize true value" in {
    havingSize() { codec =>
      codec.serializeBit(true)
    }(contents = 1)
  }

  it must "serialize positive byte" in {
    havingSize() { codec =>
      codec.serializeByte(100)
    }(contents = 100)
  }

  it must "serialize negative byte" in {
    havingSize() { codec =>
      codec.serializeByte(-100)
    }(contents = -100)
  }

  it must "serialize int zero" in {
    havingSize() { codec =>
      codec.serializeInt(0)
    }(contents = 0)
  }

  it must "serialize positive int" in {
    havingSize() { codec =>
      codec.serializeInt(1234567)
    }(contents = -114, -38, -106, 1)
  }

  it must "serialize negative int" in {
    havingSize() { codec =>
      codec.serializeInt(-1234567)
    }(contents = -115, -38, -106, 1)
  }

  it must "serialize Int.MaxValue" in {
    havingSize() { codec =>
      codec.serializeInt(Int.MaxValue)
    }(contents = -2, -1, -1, -1, 15)
  }

  it must "serialize Int.MinValue" in {
    havingSize() { codec =>
      codec.serializeInt(Int.MinValue)
    }(contents = -1, -1, -1, -1, 15)
  }

  it must "fail on int serialization when not enough remaining space" in {
    havingSize(2) { codec =>
      a[OverflowException] mustBe thrownBy {
        codec.serializeInt(Int.MaxValue)
      }
    }(contents = -2, -1)
  }

  it must "serialize long zero" in {
    havingSize() { codec =>
      codec.serializeLong(0L)
    }(contents = 0)
  }

  it must "serialize positive long" in {
    havingSize() { codec =>
      codec.serializeLong(123456789000L)
    }(contents = -112, -24, -56, -23, -105, 7)
  }

  it must "serialize negative long" in {
    havingSize() { codec =>
      codec.serializeLong(-123456789000L)
    }(contents = -113, -24, -56, -23, -105, 7)
  }

  it must "serialize Long.MaxValue" in {
    havingSize() { codec =>
      codec.serializeLong(Long.MaxValue)
    }(contents = -2, -1, -1, -1, -1, -1, -1, -1, -1, 1)
  }

  it must "serialize Long.MinValue" in {
    havingSize() { codec =>
      codec.serializeLong(Long.MinValue)
    }(contents = -1, -1, -1, -1, -1, -1, -1, -1, -1, 1)
  }

  it must "fail on long serialization when not enough remaining space" in {
    havingSize(2) { codec =>
      a[OverflowException] mustBe thrownBy {
        codec.serializeLong(Long.MaxValue)
      }
    }(-2, -1)
  }

  def havingSize(size: Int = 10)(function: PureNumberEncoder => Unit)(
      contents: Byte*): Unit = {
    val baos = new ByteArrayOutputStream()
    val stream = new ThresholdingOutputStream(size) {
      override def getStream: OutputStream =
        baos

      override def thresholdReached(): Unit =
        throw new OverflowException
    }
    val codec = new PureNumberEncoder(stream)
    try {
      function(codec)
    } finally {
      stream.getByteCount mustBe contents.length
      baos.toByteArray mustBe contents.toArray
    }
  }
}
