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
package pl.tarsa.sortalgobox.sorts.scala.shell

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class HibbardGapSequenceSpec extends CommonUnitSpecBase {
  typeBehavior[HibbardGapSequence.type]

  it must "generate full sequence" in {
    val expected = Array(1073741823, 536870911, 268435455,
      134217727, 67108863, 33554431, 16777215, 8388607, 4194303, 2097151,
      1048575, 524287, 262143, 131071, 65535, 32767, 16383, 8191, 4095, 2047,
      1023, 511, 255, 127, 63, 31, 15, 7, 3, 1)
    val actual = HibbardGapSequence.forSize(Int.MaxValue)

    assertResult(expected)(actual)
  }

  it must "generate small subsequence" in {
    val expected = Array(127, 63, 31, 15, 7, 3, 1)
    val actual = HibbardGapSequence.forSize(128)

    assertResult(expected)(actual)
  }
}
