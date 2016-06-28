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

import java.io.{ByteArrayOutputStream, OutputStream}
import java.util.{Arrays => jArrays}

import org.apache.commons.io.output.ThresholdingOutputStream
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class PureNumberEncoderSpec extends CommonUnitSpecBase {
  typeBehavior[PureNumberEncoder]

  class OverflowException extends Exception

  it must "fail on serializing negative int" in {
    havingSize(0) { codec =>
      an[IllegalArgumentException] mustBe thrownBy {
        codec.serializeInt(-1)
      }
    }()
  }

  it must "serialize int zero" in {
    havingSize(1) { codec =>
      codec.serializeInt(0)
    }(contents = 0)
  }

  it must "serialize positive int" in {
    havingSize(9) { codec =>
      codec.serializeInt(1234567)
    }(contents = -121, -83, 75)
  }

  it must "serialize max int" in {
    havingSize(9) { codec =>
      codec.serializeInt(Int.MaxValue)
    }(contents = -1, -1, -1, -1, 7)
  }

  it must "fail on int serialization when not enough remaining space" in {
    havingSize(2) { codec =>
      a[OverflowException] mustBe thrownBy {
        codec.serializeInt(Int.MaxValue)
      }
    }(contents = -1, -1)
  }

  it must "fail on serializing negative long" in {
    havingSize(0) { codec =>
      an[IllegalArgumentException] mustBe thrownBy {
        codec.serializeLong(-1L)
      }
    }()
  }

  it must "serialize long zero" in {
    havingSize(1) { codec =>
      codec.serializeLong(0L)
    }(contents = 0)
  }

  it must "serialize positive long" in {
    havingSize(9) { codec =>
      codec.serializeLong(123456789000L)
    }(contents = -120, -76, -28, -12, -53, 3)
  }

  it must "serialize max long" in {
    havingSize(9) { codec =>
      codec.serializeLong(Long.MaxValue)
    }(contents = -1, -1, -1, -1, -1, -1, -1, -1, 127)
  }

  it must "fail on long serialization when not enough remaining space" in {
    havingSize(2) { codec =>
      a[OverflowException] mustBe thrownBy {
        codec.serializeLong(Long.MaxValue)
      }
    }(-1, -1)
  }

  def havingSize(size: Int)(function: PureNumberEncoder => Unit)
    (contents: Byte*): Unit = {
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
      assert(stream.getByteCount == contents.length)
      assert(jArrays.equals(baos.toByteArray, contents.toArray))
    }
  }
}
