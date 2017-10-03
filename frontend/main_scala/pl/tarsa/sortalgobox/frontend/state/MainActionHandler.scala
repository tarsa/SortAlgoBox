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
package pl.tarsa.sortalgobox.frontend.state

import diode.{ActionHandler, ActionResult, Effect, ModelRW}
import org.scalajs.dom
import pl.tarsa.sortalgobox.frontend.state.MainAction.{
  NewStatus,
  QueryStatus,
  ShutdownServer,
  StartBenchmarking
}
import pl.tarsa.sortalgobox.frontend.state.MainActionHandler.{
  queryStatus,
  shutdownServer,
  startBenchmarking
}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util.control.NonFatal

class MainActionHandler[M](modelRW: ModelRW[M, MainModel])
    extends ActionHandler(modelRW) {
  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case NewStatus(message) =>
      updated(MainModel(message))
    case StartBenchmarking =>
      effectOnly(startBenchmarking())
    case QueryStatus =>
      effectOnly(queryStatus())
    case ShutdownServer =>
      effectOnly(shutdownServer())
  }
}

object MainActionHandler {
  def startBenchmarking(): Effect =
    queryAndUpdateStatus("/start")

  def queryStatus(): Effect =
    queryAndUpdateStatus("/status")

  def shutdownServer(): Effect =
    queryAndUpdateStatus("/shutdown")

  private def queryAndUpdateStatus(path: String): Effect = {
    lazy val messageFut =
      dom.ext.Ajax
        .get(path)
        .map(req => NewStatus(req.responseText))
        .recover { case NonFatal(_) => NewStatus("query failed") }
    Effect(messageFut)
  }
}
