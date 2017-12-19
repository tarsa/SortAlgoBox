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
package pl.tarsa.sortalgobox.common.cache

import java.util.concurrent.CountDownLatch

import pl.tarsa.sortalgobox.tests.CommonUnitSpecBase

import scala.concurrent.Future

class BuildCacheSpec extends CommonUnitSpecBase {
  typeBehavior[BuildCache]

  it must "build only once on success" in {
    var counter = 0

    val cache = makeCache { _ =>
      counter += 1
      Right(5.0)
    }

    cache.cachedBuild('a') mustBe cache.BuildSucceeded(5.0)
    cache.cachedBuild('a') mustBe cache.BuildSucceeded(5.0)
    counter mustBe 1
  }

  it must "build only once on failure" in {
    var counter = 0

    val cache = makeCache { _ =>
      counter += 1
      Left("oops")
    }

    cache.cachedBuild('x') mustBe cache.BuildFailed("oops")
    cache.cachedBuild('x') mustBe cache.BuildFailed("oops")
    counter mustBe 1
  }

  it must "block if another thread is doing the desired build" in {
    val phaseGuard = new CountDownLatch(1)
    var result = 2.0

    val cache = makeCache { _ =>
      phaseGuard.countDown()
      Thread.sleep(123)
      result += 1.0
      Right(result)
    }

    withFixedExecutor(2) { implicit execCtx =>
      Future(cache.cachedBuild('x'))
      phaseGuard.await()
      val startTime = System.currentTimeMillis()
      val status = cache.cachedBuild('x')
      val waitTime = System.currentTimeMillis() - startTime
      waitTime must be > 100L
      status mustBe cache.BuildSucceeded(3.0)
    }
  }

  it must "allow for different builds going in parallel" in {
    val phaseGuard1 = new CountDownLatch(4)
    val phaseGuard2 = new CountDownLatch(1)

    val cache = makeCache { key =>
      phaseGuard1.countDown()
      phaseGuard2.await()
      Right(key.toDouble)
    }

    withFixedExecutor(4) { implicit execCtx =>
      val statusesFut = Future.sequence(('a' to 'd').toList.map { c =>
        Future(cache.cachedBuild(c))
      })
      phaseGuard1.await()
      phaseGuard2.countDown()
      statusesFut.resultNow() mustBe
        ('a' to 'd').toList.map(c => cache.BuildSucceeded(c.toDouble))
    }
  }

  it must "clean whole cache on request" in {
    val cache = makeCache(key => Right(key.toDouble))

    val keys = ('a' to 'd').toList
    var toRemove = keys.map(_.toDouble).toSet

    keys.foreach(cache.cachedBuild)

    cache.cleanUpCache {
      case cache.BuildSucceeded(result) =>
        toRemove must contain(result)
        toRemove -= result
      case _ =>
        throw new IllegalArgumentException()
    }
    toRemove mustBe empty
  }

  private def makeCache(build: Char => Either[String, Double]): TestCache =
    build(_)

  abstract class TestCache extends BuildCache {
    override type BuildKey = Char
    override type BuildValue = Double
    override type BuildError = String

    override def cachedBuild(key: Char): BuildStatus =
      super.cachedBuild(key)

    override def cleanUpCache(itemCleaner: BuildStatus => Unit): Unit =
      super.cleanUpCache(itemCleaner)
  }
}
