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
package pl.tarsa.sortalgobox.random

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class Mwc64xTest extends CommonUnitSpecBase {
  typeBehavior[Mwc64x]

  it should "have hashCode and equals working" in {
    val rng1 = new Mwc64x
    val rng2 = new Mwc64x
    assert(rng1 == rng2)
    assert(rng1.hashCode() == rng2.hashCode())
    rng1.nextInt()
    assert(rng1 != rng2)
    assert(rng1.hashCode() != rng2.hashCode())
    rng2.skip(1)
    assert(rng1 == rng2)
    assert(rng1.hashCode() == rng2.hashCode())
  }

  it should "generate the same numbers as in test vector" in {
    val source = io.Source.fromInputStream(getClass.getResourceAsStream(
      "/pl/tarsa/sortalgobox/random/mwc64x/test_vector.txt"))
    val testVector = source.getLines().map(Integer.parseUnsignedInt(_, 16))
    val rng = new Mwc64x
    var i = 0
    for (x <- testVector) {
      assertResult(x)(rng.nextInt())
      i += 1
    }
    assertResult(12345)(i)
  }

  it should "have working skipping" in {
    val rng1 = new Mwc64x
    val rng2 = new Mwc64x
    rng1.skip(0)
    assertResult(rng2)(rng1)
    val distance = 100
    rng1.skip(distance)
    for (_ <- 0 until distance) {
      rng2.nextInt()
    }
    assertResult(rng2)(rng1)
  }

  it should "have reliable skipping" in {
    val segmentLength = 8192L
    for (i <- 0L until 123L) {
      val rngStep = new Mwc64x
      rngStep.skip(segmentLength * i)
      for (_ <- 0L until segmentLength) {
        rngStep.nextInt()
      }
      val rngSkip = new Mwc64x
      rngSkip.skip(segmentLength * (i + 1))
      assertResult(rngSkip)(rngStep)
    }
  }

  it should "have additive skipping" in {
    val segmentLength = 8192L
    for (i <- 1 until 20) {
      for (j <- 0 until i) {
        val rng1 = new Mwc64x
        val rng2 = new Mwc64x
        rng1.skip(segmentLength * j)
        rng1.skip(segmentLength * (i - j))
        rng2.skip(segmentLength * i)
        assertResult(rng2)(rng1)
      }
    }
  }
}