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
#ifndef SABMAIN_HPP
#define SABMAIN_HPP

#if defined(SORT_CACHED) && defined(SORT_SIMD)
#error Unsupported combination
#endif

#include xstr(SORT_HEADER)

using namespace tarsa;

// #define COUNT_COMPARISONS

#ifdef COUNT_COMPARISONS
int64_t counter;

template<typename ItemType>
bool countingComparisonOperator(ItemType leftOp, ComparisonType opType,
        ItemType rightOp) {
    counter++;
    return genericComparisonOperator(leftOp, opType, rightOp);
}

#define ComparisonOperator countingComparisonOperator
#else
#define ComparisonOperator genericComparisonOperator
#endif


struct auxiliary_space_t {
#ifdef SORT_CACHED
    int8_t * scratchpad;
#endif
};

auxiliary_space_t sortInitAuxiliary(ssize_t const size) {
    auxiliary_space_t auxiliary;
#ifdef SORT_CACHED
    checkZero(posix_memalign((void**) &auxiliary.scratchpad, 128,
            sizeof (int32_t) * size));
#endif
    return auxiliary;
}

void sortPerform(int32_t * const work, ssize_t const size,
        auxiliary_space_t const auxiliary) {
#if defined(SORT_SIMD)
    tarsa::SORT_ALGO<int32_t, true>(work, size);
#elif defined(SORT_CACHED)
    tarsa::SORT_ALGO<int32_t, ComparisonOperator>(work, size,
        auxiliary.scratchpad);
#else
    tarsa::SORT_ALGO<int32_t, ComparisonOperator>(work, size);
#endif
}

#endif /* SABMAIN_HPP */
