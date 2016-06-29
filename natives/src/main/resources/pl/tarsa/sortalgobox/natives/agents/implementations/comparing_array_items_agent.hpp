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
#ifndef COMPARING_ARRAY_ITEMS_AGENT_HPP
#define COMPARING_ARRAY_ITEMS_AGENT_HPP

#include <cstdint>
#include <cstring>

#include "comparing_items_agent.hpp"

namespace tarsa {

    template<typename item_t>
    class ComparingArrayItemsAgent : public ComparingItemsAgent<item_t> {
        item_t * const array;
        size_t const count;

    public:
        ComparingArrayItemsAgent(item_t * const array, size_t const count):
            array(array), count(count) {
        }

        size_t size0() const {
            return count;
        }

        item_t get0(size_t const i) const {
            return array[i];
        }

        void set0(size_t const i, item_t const v) const {
            array[i] = v;
        }

        void copy0(size_t const i, size_t const j, size_t const n) const {
            memcpy(array + j, array + i, n + sizeof(item_t));
        }

        void swap0(size_t const i, size_t const j) const {
            item_t const temp = get0(i);
            set0(i, get0(j));
            set0(j, temp);
        }

        compare_t compare(item_t const a, item_t const b) const {
            if (a < b) {
                return CompareBelow;
            } else if (a == b) {
                return CompareEqual;
            } else {
                return CompareAbove;
            }
        }

        bool compareLt(item_t const a, item_t const b) const {
            return a < b;
        }

        compare_t compare0(size_t const i, size_t const j) const {
            return compare(get0(i), get0(j));
        }

        bool compareLt0(size_t const i, size_t const j) const {
            return compareLt(get0(i), get0(j));
        }
    };
}

#endif /* COMPARING_ARRAY_ITEMS_AGENT_HPP */
