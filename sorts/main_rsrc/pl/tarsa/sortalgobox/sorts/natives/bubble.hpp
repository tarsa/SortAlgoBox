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
#ifndef BUBBLE_HPP
#define BUBBLE_HPP

#include <cstddef>

#include "items_handler.hpp"

namespace tarsa {

    template<typename item_t>
    class Sorter {
        ItemsAgent a;
        Buffer<item_t> buf;

    public:
        Sorter(ItemsAgent const agent, item_t * const array,
                size_t const length): a(agent),
                buf(Buffer<item_t>(array, length, 0)) {}

        void sort() {
            for (size_t i = a.size(buf) - 1; i >= 1; i--) {
                for (size_t j = 0; j < i; j++) {
                    if (a.compareGtI(buf, j, j + 1)) {
                        a.swap(buf, j, j + 1);
                    }
                }
            }
        }
    };

    template<typename item_t>
    Sorter<item_t> * makeSorter(items_handler_t<item_t> &handler) {
        return new Sorter<item_t>(*handler.agent, handler.input, handler.size);
    }
}

#endif /* BUBBLE_HPP */
