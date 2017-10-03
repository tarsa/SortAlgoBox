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
package pl.tarsa.sortalgobox.frontend.views

import diode.react.ModelProxy
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import pl.tarsa.sortalgobox.frontend.state.MainAction.{
  QueryStatus,
  ShutdownServer,
  StartBenchmarking
}
import pl.tarsa.sortalgobox.frontend.state.MainModel

object MainView {
  case class Props(proxy: ModelProxy[MainModel])

  private def render(p: Props) = {
    val data = p.proxy()

    <.div(
      <.div(^.color := "darkgreen", "Welcome in Sorting Algorithms Toolbox!"),
      <.input(^.tpe := "button",
              ^.value := "Start",
              ^.onClick --> p.proxy.dispatchCB(StartBenchmarking)),
      <.input(^.tpe := "button",
              ^.value := "Status",
              ^.onClick --> p.proxy.dispatchCB(QueryStatus)),
      <.input(^.tpe := "button",
              ^.value := "Shutdown",
              ^.onClick --> p.proxy.dispatchCB(ShutdownServer)),
      <.br(),
      <.textarea(^.width := "100%", ^.height := "80%", ^.value := data.message)
    )
  }

  private val component =
    ScalaComponent
      .builder[Props]("MainView")
      .stateless
      .render_P(render)
      .build

  def apply(proxy: ModelProxy[MainModel]): Unmounted[Props, Unit, Unit] =
    component(Props(proxy))
}
