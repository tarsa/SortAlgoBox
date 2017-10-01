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
package pl.tarsa.sortalgobox.frontend

import org.scalajs.dom
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import pl.tarsa.sortalgobox.shared.TinyLocator

object Start {
  private val token = java.util.UUID.randomUUID().toString
  private val timeout = 10.minutes.toMillis.toInt

  var savedServerVersion = Option.empty[String]

  def checkRefresh(): Unit = {
    val serverVersionFut = dom.ext.Ajax
      .post("/register", timeout = timeout, data = token)
      .map(_.responseText)
    serverVersionFut.onComplete {
      case util.Success(serverVersion)
          if savedServerVersion.exists(_ != serverVersion) =>
        dom.window.location.reload(true)
      case result =>
        savedServerVersion = savedServerVersion.orElse(result.toOption)
        dom.window.setTimeout(() => checkRefresh(), 1000)
    }
  }

  def main(args: Array[String]): Unit = {
    println(s"token = $token")
    checkRefresh()
    val mainDivId = TinyLocator.theOnlyElementIdWeNeed
    val mainDiv = dom.document.getElementById(mainDivId)
    mainDiv.innerHTML = ""
    mainDiv.appendChild(IndexPage.render())
  }
}
