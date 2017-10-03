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
package pl.tarsa.sortalgobox.frontend.utils

import org.scalajs.dom

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

class BrowserRevolver(token: String) {
  private val timeout = 10.minutes.toMillis.toInt

  private var savedServerVersion = Option.empty[String]

  def refreshIfNeeded(): Unit = {
    val serverVersionFut = dom.ext.Ajax
      .post("/register", timeout = timeout, data = token)
      .map(_.responseText)
    serverVersionFut.onComplete {
      case util.Success(serverVersion)
          if savedServerVersion.exists(_ != serverVersion) =>
        dom.window.location.reload(true)
      case result =>
        savedServerVersion = savedServerVersion.orElse(result.toOption)
        dom.window.setTimeout(() => refreshIfNeeded(), 1000)
    }
  }
}
