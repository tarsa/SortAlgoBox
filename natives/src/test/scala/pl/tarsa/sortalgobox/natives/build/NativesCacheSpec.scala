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
package pl.tarsa.sortalgobox.natives.build

import java.nio.file.Files
import java.util.Scanner
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import pl.tarsa.sortalgobox.common.SortAlgoBoxConfiguration._
import pl.tarsa.sortalgobox.natives.build.NativesCacheSpec._
import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class NativesCacheSpec extends NativesUnitSpecBase {
  typeBehavior[NativesCache]

  it should "compile file without defines" in {
    val buildConfig = NativeBuildConfig(components, "source.cpp")
    val process = testNativesCache.runCachedProgram(buildConfig)
    val pipeFrom = new Scanner(process.getInputStream)
    assertResult("Hello.")(pipeFrom.nextLine())
  }

  it should "compile file with header" in {
    val buildConfig = NativeBuildConfig(components, "source.cpp",
      CompilerOptions(options = CompilerOptions.defaultOptions ++
        Seq("-include", "source.hpp")))
    val process = testNativesCache.runCachedProgram(buildConfig)
    val pipeFrom = new Scanner(process.getInputStream)
    assertResult("Hello from main.hpp")(pipeFrom.nextLine())
  }

  it should "compile file with define" in {
    val buildConfig = NativeBuildConfig(components, "source.cpp",
      CompilerOptions(defines = CompilerOptions.defaultDefines ++
        Seq(CompilerDefine("SOURCE", Some("test")))))
    val process = testNativesCache.runCachedProgram(buildConfig)
    val pipeFrom = new Scanner(process.getInputStream)
    assertResult("Hello from test!")(pipeFrom.nextLine())
  }

  it should "compile only once when build is successful" in {
    val buildConfig = NativeBuildConfig(components, "source.cpp")
    val counter = new AtomicInteger(0)
    val nativesCache = new NativesCache {
      override protected def buildProgram(buildConfig: NativeBuildConfig) = {
        counter.incrementAndGet()
        Right(Files.createTempDirectory(rootTempDir, "native").toFile)
      }

    }
    val futures = (1 to 2).map(_ => Future(
      nativesCache.buildCachedProgram(buildConfig)))
    val results@Seq(result1, result2) =
      Await.result(Future.sequence(futures), 5.seconds)
    assert(result1 == result2)
    assert(results.forall(_.isInstanceOf[NativeBuildSucceeded]))
    assertResult(1)(counter.get)
  }

  it should "compile only once when build failed" in {
    val buildConfig = NativeBuildConfig(Seq.empty, "filename.bad")
    val counter = new AtomicInteger(0)
    val nativesCache = new NativesCache {
      override protected def buildProgram(buildConfig: NativeBuildConfig) = {
          counter.incrementAndGet()
          Left("Cannot find file: filename.bad")
        }
    }
    val futures = (1 to 2).map(_ => Future(
      nativesCache.buildCachedProgram(buildConfig)))
    val results@Seq(result1, result2) =
      Await.result(Future.sequence(futures), 5.seconds)
    assert(result1 == result2)
    assert(results.forall(_.isInstanceOf[NativeBuildFailed]))
    assertResult(1)(counter.get)
  }

  it should "allow for multiple different builds happening in parallel" in {
    val currentCount = new AtomicInteger(0)
    val wasMoreThanOne = new AtomicBoolean(false)
    val latch = new CountDownLatch(2)
    val nativesCache = new NativesCache {
      override protected def buildProgram(
        buildConfig: NativeBuildConfig) = {
        if (currentCount.incrementAndGet() > 1) {
          wasMoreThanOne.set(true)
        }
        latch.countDown()
        latch.await()
        currentCount.decrementAndGet()
        Left("")
      }
    }
    val futures = Seq("source1.cpp", "source2.cpp").map(fileName => Future(
      nativesCache.buildCachedProgram(NativeBuildConfig(Seq.empty, fileName))))
    val results@Seq(result1, result2) =
      Await.result(Future.sequence(futures), 5.seconds)
    assert(result1 == result2)
    assert(results.forall(_.isInstanceOf[NativeBuildFailed]))
    assert(wasMoreThanOne.get)
  }
}

object NativesCacheSpec extends NativeComponentsSupport {
  val components = makeComponents(
    ("/pl/tarsa/sortalgobox/natives/", "macros.hpp"),
    ("/pl/tarsa/sortalgobox/natives/", "source.cpp"),
    ("/pl/tarsa/sortalgobox/natives/", "source.hpp"))
}
