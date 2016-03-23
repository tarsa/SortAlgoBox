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
package pl.tarsa.sortalgobox.common.cache

import java.util.concurrent.CountDownLatch

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.concurrent.Future

class BuildCacheSpec extends CommonUnitSpecBase {
  typeBehavior[BuildCache]

  it should "build only once on success" in new TestCache {
    var counter = 0

    override def build(key: Char): Either[String, Double] = {
      counter += 1
      Right(5.0)
    }

    cachedBuild('a') shouldEqual BuildSucceeded(5.0)
    cachedBuild('a') shouldEqual BuildSucceeded(5.0)
    counter shouldEqual 1
  }

  it should "build only once on failure" in new TestCache {
    var counter = 0

    override def build(key: Char): Either[String, Double] = {
      counter += 1
      Left("oops")
    }

    cachedBuild('x') shouldEqual BuildFailed("oops")
    cachedBuild('x') shouldEqual BuildFailed("oops")
    counter shouldEqual 1
  }

  it should "block if another thread is doing the desired build" in
    new TestCache {
      val phaseGuard = new CountDownLatch(1)
      var result = 2.0

      override def build(key: Char): Either[String, Double] = {
        phaseGuard.countDown()
        Thread.sleep(123)
        result += 1.0
        Right(result)
      }

      withFixedExecutor(2) { implicit execCtx =>
        Future(cachedBuild('x'))
        phaseGuard.await()
        val startTime = System.currentTimeMillis()
        val status = cachedBuild('x')
        val waitTime = System.currentTimeMillis() - startTime
        assert(waitTime > 100)
        assert(status == BuildSucceeded(3.0))
      }
    }

  it should "allow for different builds going in parallel" in new TestCache {
    val phaseGuard1 = new CountDownLatch(4)
    val phaseGuard2 = new CountDownLatch(1)

    override def build(key: Char): Either[String, Double] = {
      phaseGuard1.countDown()
      phaseGuard2.await()
      Right(key.toDouble)
    }

    withFixedExecutor(4) { implicit execCtx =>
      val statusesFut = Future.sequence(
        ('a' to 'd').toList.map(c => Future(cachedBuild(c))))
      phaseGuard1.await()
      phaseGuard2.countDown()
      assert(statusesFut.resultNow() ==
        ('a' to 'd').toList.map(c => BuildSucceeded(c.toDouble)))
    }
  }

  it should "clean whole cache on request" in new TestCache {
    override def build(key: Char): Either[String, Double] =
      Right(key.toDouble)

    val keys = ('a' to 'd').toList
    var toRemove = keys.map(_.toDouble).toSet

    keys.foreach(cachedBuild)

    cleanUpCache {
      case BuildSucceeded(result) =>
        assert(toRemove.contains(result))
        toRemove -= result
      case _ =>
        throw new IllegalArgumentException()
    }
    assert(toRemove.isEmpty)
  }

  abstract class TestCache extends BuildCache {
    override type BuildKey = Char
    override type BuildValue = Double
    override type BuildError = String
  }

}
