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

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CountDownLatch,
ConcurrentHashMap => jConcurrentHashMap, ConcurrentMap => jConcurrentMap}
import java.util.function.{Function => jFunction}

abstract class BuildCache {
  type BuildKey
  type BuildValue
  type BuildError

  sealed trait BuildStatus

  case class BuildPending(buildId: Long, latch: CountDownLatch)
    extends BuildStatus

  case class BuildSucceeded(value: BuildValue) extends BuildStatus

  case class BuildFailed(failure: BuildError) extends BuildStatus

  private val buildCache: jConcurrentMap[BuildKey, BuildStatus] =
    new jConcurrentHashMap[BuildKey, BuildStatus]()

  private val buildIdGenerator = new AtomicLong

  protected def cachedBuild(key: BuildKey): BuildStatus = {
    val nextBuildId = buildIdGenerator.incrementAndGet()
    val cachedBuildStatus = buildCache.computeIfAbsent(key,
      new jFunction[BuildKey, BuildStatus] {
        override def apply(t: BuildKey): BuildStatus =
          BuildPending(nextBuildId, new CountDownLatch(1))
      })
    cachedBuildStatus match {
      case BuildPending(buildId, latch) if buildId == nextBuildId =>
        val newBuildStatus = build(key).fold[BuildStatus](
          BuildFailed, BuildSucceeded)
        buildCache.put(key, newBuildStatus)
        latch.countDown()
        newBuildStatus
      case BuildPending(_, latch) =>
        latch.await()
        buildCache.get(key)
      case _ =>
        cachedBuildStatus
    }
  }

  protected def build(key: BuildKey): Either[BuildError, BuildValue]

  /** Warning: this method is not synchronized */
  protected def cleanUpCache(itemCleaner: BuildStatus => Unit): Unit = {
    val oldCacheValues = buildCache.values().toArray(Array.empty[BuildStatus])
    buildCache.clear()
    oldCacheValues.foreach(itemCleaner)
  }
}
