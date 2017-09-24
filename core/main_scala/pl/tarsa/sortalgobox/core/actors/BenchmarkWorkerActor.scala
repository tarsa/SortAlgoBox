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
package pl.tarsa.sortalgobox.core.actors

import akka.actor.{Actor, Props}
import pl.tarsa.sortalgobox.core.actors.BenchmarkWorkerActor.{
  BenchmarkFailed,
  BenchmarkRequest,
  BenchmarkResult,
  BenchmarkSucceeded
}

import scala.concurrent.duration.FiniteDuration

class BenchmarkWorkerActor extends Actor {
  var bufferOpt: Option[Array[Int]] = None

  override def receive: Receive = {
    case BenchmarkRequest(id, problemSize, benchmarkBody) =>
      var result: BenchmarkResult = BenchmarkFailed(id, problemSize)
      val currentBuffer =
        bufferOpt match {
          case Some(buffer) if buffer.length == problemSize =>
            buffer
          case _ =>
            val buffer = Array.ofDim[Int](problemSize)
            bufferOpt = Some(buffer)
            buffer
        }
      try {
        val totalTime = benchmarkBody(currentBuffer)
        result = BenchmarkSucceeded(id, problemSize, totalTime)
      } finally {
        sender() ! result
      }
  }
}

object BenchmarkWorkerActor {
  val props: Props = Props(new BenchmarkWorkerActor())

  case class BenchmarkRequest(id: Int,
                              problemSize: Int,
                              benchmarkBody: Array[Int] => FiniteDuration)

  sealed trait BenchmarkResult {
    def id: Int
    def problemSize: Int
  }

  case class BenchmarkSucceeded(id: Int,
                                problemSize: Int,
                                timeTaken: FiniteDuration)
      extends BenchmarkResult

  case class BenchmarkFailed(id: Int, problemSize: Int) extends BenchmarkResult
}
