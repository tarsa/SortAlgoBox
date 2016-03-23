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
package pl.tarsa.sortalgobox.sorts.jre

import pl.tarsa.sortalgobox.sorts.tests.{SortCheckerLong, SortCheckerInt}
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class ParallelArraysSortSpec extends CommonUnitSpecBase {
  typeBehavior[ParallelArraysSort.type]

  val intChecker = new SortCheckerInt(ParallelArraysSort.intSort)
  val longChecker = new SortCheckerLong(ParallelArraysSort.longSort)

  it must "handle empty array" in {
    intChecker.forEmptyArray()
    longChecker.forEmptyArray()
  }

  it must "handle single element array" in {
    intChecker.forSingleElementArray()
    longChecker.forSingleElementArray()
  }

  it must "sort small array" in {
    intChecker.forFewElementsArray()
    longChecker.forFewElementsArray()
  }

  it must "sort random array" in {
    intChecker.forArrayOfSize(100)
    longChecker.forArrayOfSize(100)
  }
}
