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
package pl.tarsa.sortalgobox.natives.generators

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class NativeEnumGeneratorSpec extends CommonUnitSpecBase {
  typeBehavior[NativeEnumGenerator.type]

  it must "generate proper enum" in {
    import SomeEnums._
    val mappings = Seq(A -> "A", B -> "B", C -> "C")
    val generated =
      NativeEnumGenerator("my_enum_t", "k", SomeEnums)(mappings: _*)

    generated mustBe
      s"""enum my_enum_t {
         |  kA = 0, kB = 1, kC = 2
         |};""".stripMargin
  }

  it must "fail when provided non-matching enumeration and mapping" in {
    import OtherEnums._
    a[AssertionError] mustBe thrownBy {
      NativeEnumGenerator(null, null, SomeEnums)(X -> "X", Y -> "Y", Z -> "Z")
    }
  }

  object SomeEnums extends Enumeration {
    val A, B, C = Value
  }

  object OtherEnums extends Enumeration {
    val X, Y, Z = Value
  }
}
