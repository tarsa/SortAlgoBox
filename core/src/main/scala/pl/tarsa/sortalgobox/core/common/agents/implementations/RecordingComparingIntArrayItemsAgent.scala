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
package pl.tarsa.sortalgobox.core.common.agents.implementations

import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes._
import pl.tarsa.sortalgobox.core.common.agents.ComparingItemsAgent
import pl.tarsa.sortalgobox.core.crossverify.PureNumberCodec

class RecordingComparingIntArrayItemsAgent(recorder: PureNumberCodec,
  underlying: ComparingIntArrayItemsAgent) extends ComparingItemsAgent[Int] {

  import recorder._

  override def size0: Int =
    recordedF(Size0, _.size0)

  override def get0(i: Int): Int =
    recordedF(Get0, _.get0(i), i)

  override def set0(i: Int, v: Int): Unit =
    recordedP(Set0, _.set0(i, v), i)

  override def copy0(i: Int, j: Int, n: Int): Unit =
    recordedP(Copy0, _.copy0(i, j, n), i, j, n)

  override def swap0(i: Int, j: Int): Unit =
    recordedP(Swap0, _.swap0(i, j), i, j)

  override def compare(a: Int, b: Int): Int =
    recordedF(Compare, _.compare(a, b))

  override def compare0(i: Int, j: Int): Int =
    recordedF(Compare0, _.compare0(i, j), i, j)

  @inline
  private def recordedP(actionType: ActionType,
    action: ComparingIntArrayItemsAgent => Unit,
    parameters: Int*): Unit = {
    serializeInt(actionType.id)
    parameters.foreach(serializeInt)
    action(underlying)
  }

  @inline
  private def recordedF(actionType: ActionType,
    action: ComparingIntArrayItemsAgent => Int,
    parameters: Int*): Int = {
    serializeInt(actionType.id)
    parameters.foreach(serializeInt)
    action(underlying)
  }
}
