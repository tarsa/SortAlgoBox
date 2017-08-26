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
#ifndef COMPARING_ITEMS_AGENT_HPP
#define COMPARING_ITEMS_AGENT_HPP

#include <cstdint>

#include "items_agent.hpp"

namespace tarsa {

    enum compare_t {
        CompareBelow, CompareEqual, CompareAbove
    };

    template<typename item_t>
    class ComparingItemsAgent : public ItemsAgent<item_t> {
    public:
        compare_t compare(item_t const a, item_t const b) const {}

        bool compareLt(item_t const a, item_t const b) const {}

        compare_t compare0(size_t const i, size_t const j) const {}

        bool compareLt0(size_t const i, size_t const j) const {}
    };
}

#endif /* COMPARING_ITEMS_AGENT_HPP */
