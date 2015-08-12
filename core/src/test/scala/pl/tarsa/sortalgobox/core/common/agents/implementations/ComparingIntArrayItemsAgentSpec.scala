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
package pl.tarsa.sortalgobox.core.common.agents.implementations

class ComparingIntArrayItemsAgentSpec extends BaseIntArrayItemsAgentSpec(
  new ComparingIntArrayItemsAgent(_)) {
  typeBehavior[ComparingIntArrayItemsAgent]

  it should "return correct size for empty array" in {
    readTest()(a => assert(a.size0 == 0))
  }

  it should "return correct size for non-empty array" in {
    readTest(1, 2, 3)(a => assert(a.size0 == 3))
  }

  it should "get proper values" in {
    readTest(5, 3, 2, 8)(
      a => assert(a.get0(0) == 5),
      a => assert(a.get0(1) == 3),
      a => assert(a.get0(2) == 2),
      a => assert(a.get0(3) == 8))
  }

  it should "set proper cells" in {
    writeTest(5, 3, 2, 8)(
      _.set0(3, 1),
      _.set0(1, 2),
      _.set0(0, 9),
      _.set0(3, 7)
    )(9, 2, 2, 7)
  }

  it should "copy proper cells" in {
    writeTest(5, 3, 2, 8)(
      _.copy0(1, 0, 2)
    )(3, 2, 2, 8)
  }

  it should "swap proper cells" in {
    writeTest(5, 3, 2, 8)(
      _.swap0(3, 0)
    )(8, 3, 2, 5)
  }

  it should "compare values properly" in {
    pureTest(
      a => assert(a.compare(1, 2) == -1),
      a => assert(a.compare(2, 1) == 1),
      a => assert(a.compare(1, 1) == 0)
    )
  }

  it should "compare cells properly" in {
    readTest(5, 3, 2, 8, 5)(
      a => assert(a.compare0(0, 1) == 1),
      a => assert(a.compare0(2, 3) == -1),
      a => assert(a.compare0(0, 4) == 0)
    )
  }
}
