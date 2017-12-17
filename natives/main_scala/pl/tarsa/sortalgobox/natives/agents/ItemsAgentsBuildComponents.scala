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
package pl.tarsa.sortalgobox.natives.agents

import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes
import pl.tarsa.sortalgobox.common.crossverify.TrackingEnums.ActionTypes._
import pl.tarsa.sortalgobox.natives.build.{
  NativeBuildComponent,
  NativeBuildComponentFromString,
  NativeComponentsSupport
}
import pl.tarsa.sortalgobox.natives.generators.NativeEnumGenerator

object ItemsAgentsBuildComponents extends NativeComponentsSupport {
  val trackingEnum = Seq(
    NativeBuildComponentFromString(
      NativeEnumGenerator("tracking_codes_t", "Code", ActionTypes)(
        Size -> "Size",
        Get -> "Get",
        Set -> "Set",
        Swap -> "Swap",
        ItemBitsSize -> "ItemBitsSize",
        AsLong -> "AsLong",
        GetSlice -> "GetSlice",
        CompareEq -> "CompareEq",
        CompareGt -> "CompareGt",
        CompareGte -> "CompareGte",
        CompareLt -> "CompareLt",
        CompareLte -> "CompareLte"
      ),
      "action_codes.hpp"
    ))

  val standard: Seq[NativeBuildComponent] = {
    makeResourceComponents(
      ("/pl/tarsa/sortalgobox/natives/agents/", "items_agent.hpp")
    )
  }

  val recording: Seq[NativeBuildComponent] = {
    standard ++ makeResourceComponents(
      ("/pl/tarsa/sortalgobox/natives/", "buffered_io.hpp"),
      ("/pl/tarsa/sortalgobox/natives/crossverify/", "number_codec.hpp")
    ) ++ trackingEnum
  }
}
