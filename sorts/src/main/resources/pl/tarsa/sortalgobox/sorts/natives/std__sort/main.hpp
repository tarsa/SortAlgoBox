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
#ifndef MAIN_HPP
#define MAIN_HPP

#include <algorithm>
#include <cstdint>

#include "standard_items_handler.hpp"

#define VALIDATE_FUNCTION

bool sortValidate(items_handler_t<int32_t> &itemsHandler) {
    int32_t const * const work = itemsHandler.input;
    bool sorted = true;
    for (size_t i = 1; sorted && (i < itemsHandler.size); i++) {
        sorted &= work[i - 1] <= work[i];
    }
    return sorted;
}

void sortPerform(items_handler_t<int32_t> &itemsHandler) {
    int32_t * const work = itemsHandler.input;
    std::sort(work, work + itemsHandler.size);
}

#endif /* MAIN_HPP */
