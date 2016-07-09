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
#ifndef STANDARD_ITEMS_HANDLER_HPP
#define STANDARD_ITEMS_HANDLER_HPP

#include <cstddef>

#include "utilities.hpp"

template<typename item_t>
struct items_handler_t {
    item_t * input;
    size_t size;
};

template<typename item_t>
items_handler_t<item_t> sortItemsHandlerPrepare(item_t * const input,
        size_t const size) {
    items_handler_t<item_t> itemsHandler;
    itemsHandler.input = input;
    itemsHandler.size = size;
    return itemsHandler;
}

template<typename item_t>
void sortItemsHandlerReleasePreValidation(
        items_handler_t<item_t> &itemsHandler) {
}

template<typename item_t>
void sortItemsHandlerReleasePostValidation(
        items_handler_t<item_t> &itemsHandler) {
}

#endif // STANDARD_ITEMS_HANDLER_HPP
