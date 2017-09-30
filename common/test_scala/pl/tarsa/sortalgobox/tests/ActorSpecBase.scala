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

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.MustMatchers._
import pl.tarsa.sortalgobox.tests.ActorSpecBase.config

import scala.concurrent.Await
import scala.concurrent.duration._

abstract class ActorSpecBase extends CommonUnitSpecBase with BeforeAndAfterAll {
  implicit var actorSystem: ActorSystem = _

  override protected def beforeAll(): Unit = {
    ActorSpecBase.warmUpAkka
    val safeName = getClass.getSimpleName
    actorSystem = ActorSystem(safeName, ConfigFactory.parseString(config))
  }

  override protected def afterAll(): Unit =
    actorSystem.terminate()
}

object ActorSpecBase {
  lazy val warmUpAkka: Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("WarmUpActorSystem")
    val probe = TestProbe()
    probe.msgAvailable mustBe false
    probe.expectNoMessage(1.millisecond)
    val termination = actorSystem.terminate()
    Await.ready(termination, 10.seconds)
  }

  val config: String =
    """akka {
      |  # actor.debug.fsm = true
      |  # loglevel = "DEBUG"
      |}
    """.stripMargin
}
