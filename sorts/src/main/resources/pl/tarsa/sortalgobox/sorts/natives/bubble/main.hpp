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

#include "comparing_array_items_agent.hpp"

EMPTY_AUXILIARY_SPACE

template<template<typename> class ItemsAgent, typename item_t>
void sort(ItemsAgent<item_t> agent) {
    for (size_t i = agent.size0() - 1; i >= 1; i--) {
        for (size_t j = 0; j < i; j++) {
            if (agent.compare0(j, j + 1) == tarsa::CompareAbove) {
                agent.swap0(j, j + 1);
            }
        }
    }
}

void sortPerform(int32_t * const work, size_t const size,
        auxiliary_space_t& auxiliary) {
    tarsa::ComparingArrayItemsAgent<int32_t> agent(work, size);
    sort(agent);
}

#endif /* MAIN_HPP */
