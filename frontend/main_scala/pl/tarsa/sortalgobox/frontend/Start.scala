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
import pl.tarsa.sortalgobox.frontend.state.{MainModel, MainStateHolder}
import pl.tarsa.sortalgobox.frontend.utils.BrowserRevolver
import pl.tarsa.sortalgobox.frontend.utils.DiodeTypes.DiodeWrapperU
import pl.tarsa.sortalgobox.frontend.views.MainView
import pl.tarsa.sortalgobox.shared.TinyLocator

object Start {
  def main(args: Array[String]): Unit = {
    setupRefresh()
    plugInReact()
  }

  private def setupRefresh(): Unit = {
    val token = java.util.UUID.randomUUID().toString
    println(s"token = $token")
    new BrowserRevolver(token).refreshIfNeeded()
  }

  private def plugInReact(): Unit = {
    val mainDivId = TinyLocator.theOnlyElementIdWeNeed
    val diodeCircuit = new MainStateHolder()
    val mainViewProxyConstructor = diodeCircuit.connect(identity[MainModel] _)
    val mainView: DiodeWrapperU[MainModel] =
      mainViewProxyConstructor { modelProxy =>
        MainView(modelProxy).vdomElement
      }
    mainView.renderIntoDOM(dom.document.getElementById(mainDivId))
  }
}
