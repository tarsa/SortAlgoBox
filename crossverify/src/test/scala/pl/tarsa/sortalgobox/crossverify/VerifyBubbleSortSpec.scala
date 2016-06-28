/*
 * Copyright (C) 2015, 2016 Piotr Tarsa ( http://github.com/tarsa )
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

package pl.tarsa.sortalgobox.crossverify

import org.apache.commons.io.IOUtils
import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes
import pl.tarsa.sortalgobox.core.common.agents.implementations.{ComparingIntArrayItemsAgent, VerifyingComparingIntArrayItemsAgent}
import pl.tarsa.sortalgobox.core.crossverify.PureNumberDecoder
import pl.tarsa.sortalgobox.natives.build.{NativeBuildComponentFromString, NativeBuildConfig, NativeComponentsSupport, NativesCache}
import pl.tarsa.sortalgobox.natives.generators.NativeEnumGenerator
import pl.tarsa.sortalgobox.random.{Mwc64x, NativeMwc64x}
import pl.tarsa.sortalgobox.sorts.scala.bubble.BubbleSort
import pl.tarsa.sortalgobox.tests.NativesUnitSpecBase

class VerifyBubbleSortSpec extends NativesUnitSpecBase {

  "Native Bubble Sort" must "take steps identical to Scala Bubble Sort" in {
    val replayer = new NativeRecordingBubbleSort(testNativesCache)
      .sortAndCollectData()
    new CheckingBubbleSort().run(replayer, assert(_))
  }
}

class NativeRecordingBubbleSort(nativesCache: NativesCache = NativesCache) {

  val buildConfig =
    NativeBuildConfig(NativeRecordingBubbleSort.components, "bubblesort.cpp")

  def sortAndCollectData(): PureNumberDecoder = {
    val generatorProcess = nativesCache.runCachedProgram(buildConfig)
    val stream = IOUtils.buffer(generatorProcess.getInputStream)
    new PureNumberDecoder(stream)
  }
}

object NativeRecordingBubbleSort extends NativeComponentsSupport {

  import ActionTypes._

  val trackingEnum = Seq(NativeBuildComponentFromString(
    NativeEnumGenerator("tracking_codes_t", "Code", ActionTypes)
    (Size0 -> "Size0", Get0 -> "Get0", Set0 -> "Set0", Copy0 -> "Copy0",
      Swap0 -> "Swap0", Compare -> "Compare", Compare0 -> "Compare0"),
    "action_codes.hpp"))

  val components = NativeMwc64x.header ++ makeResourceComponents(
    ("/pl/tarsa/sortalgobox/natives/", "buffered_io.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/", "comparing_items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/", "items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/implementations/",
      "comparing_array_items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/implementations/",
      "recording_comparing_items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/crossverify/", "numbercodec.hpp"),
    ("/pl/tarsa/sortalgobox/crossverify/", "bubblesort.cpp")) ++
    trackingEnum
}

class CheckingBubbleSort {
  def run(replayer: PureNumberDecoder, verify: Boolean => Unit) = {
    val generator = Mwc64x()
    val array = Array.fill[Int](1234)(generator.nextInt())
    val itemsAgent = new VerifyingComparingIntArrayItemsAgent(replayer,
      new ComparingIntArrayItemsAgent(array), verify)
    new BubbleSort().sort(itemsAgent)
  }
}
