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
package pl.tarsa.sortalgobox.natives.agents

import pl.tarsa.sortalgobox.natives.build.NativeComponentsSupport

object ItemsAgentsBuildComponents extends NativeComponentsSupport {
  val standard = makeResourceComponents(
    ("/pl/tarsa/sortalgobox/natives/agents/", "items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/", "comparing_items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/implementations/",
      "comparing_array_items_agent.hpp"))

  val recording = standard ++ makeResourceComponents(
    ("/pl/tarsa/sortalgobox/natives/", "buffered_io.hpp"),
    ("/pl/tarsa/sortalgobox/natives/agents/implementations/",
      "recording_comparing_items_agent.hpp"),
    ("/pl/tarsa/sortalgobox/natives/crossverify/", "numbercodec.hpp"))
}
