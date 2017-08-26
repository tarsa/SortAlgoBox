/*
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
package pl.tarsa.sortalgobox.tests

import java.util.concurrent.{Executors, TimeUnit}

import org.scalatest.{FlatSpec, MustMatchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{
  Await,
  ExecutionContext,
  ExecutionContextExecutorService,
  Future
}
import scala.reflect.ClassTag

abstract class CommonUnitSpecBase extends FlatSpec with MustMatchers {

  System.setProperty("uniqueLibraryNames", "true")

  val `have full code coverage` = "have full code coverage"

  def typeBehavior[T](implicit classTag: ClassTag[T]): Unit =
    behavior of classTag.runtimeClass.getSimpleName

  implicit class InstantFuture[T](future: Future[T]) {
    def readyNow(): Future[T] = Await.ready(future, Duration.Inf)

    def resultNow(): T = Await.result(future, Duration.Inf)
  }

  def guardedOpenCLTest(body: => Unit): Unit = {
    val skip = System.getProperty("skip_opencl_tests") != null
    if (skip) {
      pending
    } else {
      body
    }
  }

  def withFixedExecutor(threadsNumber: Int)(
      body: ExecutionContextExecutorService => Unit): Unit = {
    val execCtx = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(threadsNumber))
    try {
      body(execCtx)
    } finally {
      execCtx.shutdown()
      execCtx.awaitTermination(Int.MaxValue, TimeUnit.DAYS)
    }
  }
}
