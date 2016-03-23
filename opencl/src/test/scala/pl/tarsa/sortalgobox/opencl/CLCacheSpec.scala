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
package pl.tarsa.sortalgobox.opencl

import org.jocl.CL
import org.scalatest.BeforeAndAfterAll
import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

class CLCacheSpec extends CommonUnitSpecBase with BeforeAndAfterAll {
  typeBehavior[CLCache]

  override protected def beforeAll(): Unit =
    CL.setExceptionsEnabled(true)

  it should "compile fine when input is correct" in {
    val example = getClass.getResource("example.cl")
    val sources = List(io.Source.fromURL(example).mkString)
    val program = CLCache.withCpuContext(ctx =>
      CLCache.getCachedProgram(ctx, sources))
    assert(program ne null)
  }

  it should "fail when compilation is unsuccessful" in guardedOpenCLTest {
    an[Exception] shouldBe thrownBy {
      CLCache.withCpuContext(ctx => CLCache.getCachedProgram(ctx, List("bad")))
    }
  }
}
