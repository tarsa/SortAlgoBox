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
package pl.tarsa.sortalgobox.sorts.scala.shell

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class TokudaGapSequenceSpec extends CommonUnitSpecBase {
  typeBehavior[TokudaGapSequence.type]

  it should "generate full sequence" in {
    val expected = Array(1147718699, 510097199, 226709865, 100759939, 44782195,
      19903197, 8845865, 3931495, 1747330, 776590, 345151, 153400, 68177, 30300,
      13466, 5984, 2659, 1181, 524, 232, 102, 45, 19, 8, 3, 1)

    val actual = TokudaGapSequence.forSize(Int.MaxValue)

    assertResult(expected)(actual)
  }

  it should "generate small subsequence" in {
    val expected = Array(102, 45, 19, 8, 3, 1)
    val actual = TokudaGapSequence.forSize(128)

    assertResult(expected)(actual)
  }
}
