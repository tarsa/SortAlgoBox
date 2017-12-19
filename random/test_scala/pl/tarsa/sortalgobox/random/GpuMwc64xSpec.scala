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
package pl.tarsa.sortalgobox.random

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class GpuMwc64xSpec extends CommonUnitSpecBase {
  typeBehavior[GpuMwc64x.type]

  it must "work for empty arrays" in guardedOpenCLTest {
    val gpuRng = GpuMwc64x
    val array = gpuRng.generate(0, 1)
    array mustBe empty
  }

  it must "fail for non positive work items number" in guardedOpenCLTest {
    val gpuRng = GpuMwc64x
    an[IllegalArgumentException] mustBe thrownBy {
      gpuRng.generate(1 << 16, 0)
    }
    an[IllegalArgumentException] mustBe thrownBy {
      gpuRng.generate(1 << 16, -1)
    }
  }

  it must "fail for invalid vector length" in guardedOpenCLTest {
    val gpuRng = GpuMwc64x
    an[IllegalArgumentException] mustBe thrownBy {
      gpuRng.generate(1 << 16, 1, 7)
    }
  }

  it must "give identical results as Scala version" in guardedOpenCLTest {
    val gpuRng = GpuMwc64x
    forEvery(List(1, 2, 4, 8, 16, 32, 64, 512)) { workItems =>
      forEvery(GpuMwc64x.allowedVectorLengths.toList.sorted) { vectorLength =>
        val array = gpuRng.generate(1 << 16, workItems, vectorLength)
        val scalaRng = new Mwc64x
        array.foreach(_ mustBe scalaRng.nextInt())
      }
    }
  }
}
