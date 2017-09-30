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
package pl.tarsa.sortalgobox.main.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import pl.tarsa.sortalgobox.core.actors.BenchmarkSuiteActor
import pl.tarsa.sortalgobox.main.BenchmarksConfigurations
import pl.tarsa.sortalgobox.main.app.server.BenchmarkSuiteController
import pl.tarsa.sortalgobox.natives.build.NativesCache

import scala.concurrent.Promise

object WebServerBenchmarkSuiteApp {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("web-benchmark-suite")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    import actorSystem.dispatcher
    val benchmarks = BenchmarksConfigurations.benchmarks
    val benchmarkSuiteActor = actorSystem.actorOf(BenchmarkSuiteActor.props)
    val bindingPromise = Promise[Http.ServerBinding]()
    val shutdownAction = () => {
      bindingPromise.future
        .flatMap(_.unbind())
        .flatMap(_ => actorSystem.terminate())
        .map(_ => ())
    }
    val server =
      new BenchmarkSuiteController(benchmarks,
                                   benchmarkSuiteActor,
                                   shutdownAction)
    val bindingFut = Http().bindAndHandle(server.route, "localhost", 8080)
    bindingPromise.completeWith(bindingFut)
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      NativesCache.cleanup()
      println("Cleanup complete!")
    }))
  }
}
