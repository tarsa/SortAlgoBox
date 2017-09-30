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
package pl.tarsa.sortalgobox.tests

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.{
  RouteTest,
  ScalatestRouteTest,
  TestFrameworkInterface
}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.MustMatchers._

abstract class HttpRouteSpecBase
    extends CommonUnitSpecBase
    with ScalatestRouteTest
    with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    HttpRouteSpecBase.warmUpAkkaHttp
    super.beforeAll()
  }
}

object HttpRouteSpecBase {
  lazy val warmUpAkkaHttp: Unit = {
    ActorSpecBase.warmUpAkka
    val testRoute = path("test")(get(complete("ok")))
    val tester =
      new (() => Unit) with RouteTest with TestFrameworkInterface {
        def apply(): Unit = {
          Get("/test") ~> testRoute ~> {
            check(responseAs[String] mustBe "ok")
          }
        }

        override def failTest(msg: String): Nothing =
          fail(msg)
      }
    tester()
    tester.cleanUp()
  }
}
