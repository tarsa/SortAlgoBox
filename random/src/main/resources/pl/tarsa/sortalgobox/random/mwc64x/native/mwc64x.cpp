/**
 * Copyright (C) 2015 Piotr Tarsa ( http://github.com/tarsa )
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
 *
 */

#include <cstdint>
#include <cstdlib>
#include <iomanip>
#include <iostream>

uint32_t mwc64xGet(uint64_t const state) {
    return (uint32_t)((state >> 32) ^ (state & UINT32_MAX));
}

void mwc64xAdvance(uint64_t * const state) {
    uint32_t const A = 4294883355U;
    uint32_t c = *state >> 32;
    uint32_t x = *state & UINT32_MAX;
    *state = (uint64_t) A * x + c;
}

int main(int argc, char** argv) {
    uint64_t const initialState = 1745055044494293084UL;

    int n;
    std::cin >> n;
    std::cout << std::setfill('0');
    uint64_t state = initialState;
    for (int i = 0; i < n; i++) {
        std::cout << std::setw(2) << std::hex << mwc64xGet(state) << std::endl;
        mwc64xAdvance(&state);
    }
    return EXIT_SUCCESS;
}
