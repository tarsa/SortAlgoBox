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
#include <algorithm>
#include <cstdint>

#include "buffered_io.hpp"
#include "comparing_array_items_agent.hpp"
#include "mwc64x.hpp"
#include "numbercodec.hpp"
#include "recording_comparing_items_agent.hpp"

template<template<typename> class ItemsAgent, typename ItemType>
void sort(ItemsAgent<ItemType> agent) {
    for (size_t i = agent.size0() - 1; i >= 1; i--) {
        for (size_t j = 0; j < i; j++) {
            if (agent.compare0(j, j + 1) == tarsa::CompareAbove) {
                agent.swap0(j, j + 1);
            }
        }
    }
}

int main(int argc, char** argv) {
    size_t const ArraySize = 1234;

    int32_t * arrayToSort = new int32_t[ArraySize];
    mwc64xFill(arrayToSort, ArraySize);

    tarsa::BufferedWriter * const writer = new tarsa::BufferedFileWriter(stdout,
        1 << 20, "stdout");
    tarsa::NumberEncoder numberEncoder(writer);

    tarsa::ComparingArrayItemsAgent<int32_t>
        comparingArrayItemsAgent(arrayToSort, ArraySize);
    tarsa::RecordingComparingItemsAgentSetup<tarsa::ComparingArrayItemsAgent>::
        Result<int32_t> recordingItemsAgent(
        numberEncoder, comparingArrayItemsAgent);

    sort(recordingItemsAgent);

    delete [] arrayToSort;
    arrayToSort = nullptr;

    numberEncoder.flush();
    return EXIT_SUCCESS;
}
