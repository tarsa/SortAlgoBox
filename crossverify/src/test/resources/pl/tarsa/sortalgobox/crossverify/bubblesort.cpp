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

#include "action_codes.hpp"
#include "buffered_io.hpp"
#include "mwc64x.hpp"
#include "numbercodec.hpp"

tarsa::BufferedWriter * const writer = new tarsa::BufferedFileWriter(stdout,
    1 << 20, "stdout");
tarsa::NumberEncoder numberEncoder(writer);

int32_t compare0(int32_t const * const, ssize_t const, ssize_t const);
void swap0(int32_t * const, ssize_t const, ssize_t const);

void sort(int32_t * const work, ssize_t const size) {
    for (ssize_t i = size - 1; i >= 1; i--) {
        for (ssize_t j = 0; j < i; j++) {
            if (compare0(work, j, j + 1) > 0) {
                swap0(work, j, j + 1);
            }
        }
    }
}

int32_t compare0(int32_t const * const work, ssize_t const i, ssize_t const j) {
    numberEncoder.serializeLong(CodeCompare0);
    numberEncoder.serializeLong(i);
    numberEncoder.serializeLong(j);
    int32_t const a = work[i];
    int32_t const b = work[j];
    if (a > b) {
        return 1;
    } else if (a < b) {
        return -1;
    } else {
        return 0;
    }
}

void swap0(int32_t * const work, ssize_t const i, ssize_t const j) {
    numberEncoder.serializeLong(CodeSwap0);
    numberEncoder.serializeLong(i);
    numberEncoder.serializeLong(j);
    int32_t const tmp = work[i];
    work[i] = work[j];
    work[j] = tmp;
}

int main(int argc, char** argv) {
    ssize_t const ArraySize = 1234;

    int32_t * arrayToSort = new int32_t[ArraySize];
    mwc64xFill(arrayToSort, ArraySize);

    numberEncoder.serializeLong(CodeSize0);
    sort(arrayToSort, ArraySize);

    delete [] arrayToSort;
    arrayToSort = nullptr;

    numberEncoder.flush();
    return EXIT_SUCCESS;
}
