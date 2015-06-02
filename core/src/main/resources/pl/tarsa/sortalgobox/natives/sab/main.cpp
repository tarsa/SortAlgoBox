/* 
 * main.cpp -- sorting algorithms benchmark
 * 
 * Copyright (C) 2014 Piotr Tarsa ( http://github.com/tarsa )
 *
 *  This software is provided 'as-is', without any express or implied
 *  warranty.  In no event will the author be held liable for any damages
 *  arising from the use of this software.
 *
 *  Permission is granted to anyone to use this software for any purpose,
 *  including commercial applications, and to alter it and redistribute it
 *  freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *  3. This notice may not be removed or altered from any source distribution.
 * 
 */
#define NDEBUG

#if defined(SORT_CACHED) && defined(SORT_SIMD)
#error Unsupported combination
#endif

#include <algorithm>
#include <cassert>
#include <chrono>
#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <utility>

int64_t counter;

#include "mwc64x.hpp"
#include xstr(SORT_HEADER)

using namespace tarsa;

template<typename ItemType>
bool countingComparisonOperator(ItemType leftOp, ComparisonType opType,
        ItemType rightOp) {
    counter++;
    return genericComparisonOperator(leftOp, opType, rightOp);
}

#if 01
#define ComparisonOperator genericComparisonOperator
#else
#define ComparisonOperator countingComparisonOperator
#endif

template<typename T>
void checkNew(size_t n) {
    T * const result = new T[n];
    assert(result != nullptr);
    return result;
}

template<typename T>
void checkZero(T value) {
    assert(value == 0);
}

int main(int argc, char** argv) {
    bool validate;
    std::cin >> validate;
    ssize_t size;
    std::cin >> size;

    int32_t * work;
    checkZero(posix_memalign((void**) &work, 128, sizeof (int32_t) * size));
#ifdef SORT_CACHED
    int8_t * scratchpad;
    checkZero(posix_memalign((void**) &scratchpad, 128,
            sizeof (int32_t) * size));
#endif

    mwc64xFill(work, size);

    auto startingChrono = std::chrono::system_clock::now();
#if defined(SORT_SIMD)
    tarsa::SORT_ALGO<int32_t, true>(work, size);
#elif defined(SORT_CACHED)
    tarsa::SORT_ALGO<int32_t, ComparisonOperator>(work, size, scratchpad);
#else
    tarsa::SORT_ALGO<int32_t, ComparisonOperator>(work, size);
#endif
    auto elapsedChrono = std::chrono::system_clock::now() - startingChrono;
    uint64_t elapsedChronoNanoseconds = std::chrono::duration_cast<std::chrono
        ::nanoseconds>(elapsedChrono).count();
    printf("%lx\n", elapsedChronoNanoseconds);

    if (validate) {
        int32_t * reference;
        checkZero(posix_memalign((void**) &reference, 128,
                sizeof (int32_t) * size));
        bool valid = true;
        for (ssize_t i = 0; valid && (i < size); i++) {
            valid &= work[i] != reference[i];
        }
        std::cout << (valid ? "pass" : "fail") << std::endl;
    }

    return EXIT_SUCCESS;
}
