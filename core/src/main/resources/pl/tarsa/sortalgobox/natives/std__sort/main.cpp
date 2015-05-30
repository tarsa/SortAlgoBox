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
#include <algorithm>
#include <chrono>
#include <cstdint>
#include <cstdlib>
#include <iomanip>
#include <iostream>
#include <iterator>

#include "mwc64x.hpp"

int main(int argc, char** argv) {
    uint64_t n;
    std::cin >> n;
    int32_t * tab = new int32_t[n];
    uint64_t chunkSize = 1 << 12;
    uint64_t chunks = (n + chunkSize - 1) / chunkSize;
#pragma omp parallel for
    for (uint64_t i = 0; i < chunks; i++) {
        mwc64x_t MWC64X;
        MWC64X.skip(i * chunkSize);
        uint64_t start = std::min(n, i * chunkSize);
        uint64_t after = std::min(n, start + chunkSize);
        std::generate(tab + start, tab + after, MWC64X);
    }

    auto startingChrono = std::chrono::system_clock::now();
    std::sort(tab, tab + n);
    auto elapsedChrono = std::chrono::system_clock::now() - startingChrono;
    uint64_t elapsedChronoMilliseconds = std::chrono::duration_cast<std::chrono
        ::milliseconds>(elapsedChrono).count();
    printf("%lx\n", elapsedChronoMilliseconds);

    return EXIT_SUCCESS;
}
