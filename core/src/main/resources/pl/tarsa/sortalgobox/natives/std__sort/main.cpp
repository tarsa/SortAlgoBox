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

struct mwc64x_t {
    uint64_t state;
    uint64_t const A = 4294883355L;
    uint64_t const M = (A << 32) - 1;

    mwc64x_t(uint64_t const initialState = 1745055044494293084UL)
    : state(initialState) {
    }

    uint32_t operator()() {
        uint32_t result = (uint32_t) ((state >> 32) ^ (state & UINT32_MAX));
        uint32_t c = state >> 32;
        uint32_t x = state & UINT32_MAX;
        state = A * x + c;
        return result;
    }

    void skip(uint64_t distance) {
        uint64_t c = state >> 32;
        uint64_t x = state & UINT32_MAX;
        uint64_t m = modpow(A, distance, M);
        uint64_t v = mulmod(A * x + c, m, M);
        uint64_t x1 = v / A;
        uint64_t c1 = v % A;
        state = (c1 << 32) + x1;
    }

    uint64_t modpow(uint64_t base, uint64_t exp, uint64_t modulus) {
        base %= modulus;
        uint64_t result = 1;
        while (exp > 0) {
            if (exp & 1) {
                result = mulmod(result, base, modulus);
            }
            base = mulmod(base, base, modulus);
            exp >>= 1;
        }
        return result;
    }

    uint64_t mulmod(uint64_t a, uint64_t b, uint64_t m) {
        uint64_t res = 0;
        uint64_t temp_b;

        if (b >= m) {
            if (m > UINT64_MAX / 2u)
                b -= m;
            else
                b %= m;
        }

        while (a != 0) {
            if (a & 1) {
                /* Add b to res, modulo m, without overflow */
                /* Equiv to if (res + b >= m), without overflow */
                if (b >= m - res)
                    res -= m;
                res += b;
            }
            a >>= 1;

            /* Double b, modulo m */
            temp_b = b;
            /* Equiv to if (2 * b >= m), without overflow */
            if (b >= m - b)
                temp_b -= m;
            b += temp_b;
        }
        return res;
    }
};

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
