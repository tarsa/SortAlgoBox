/* 
 * sortheapsimddwordvariantb.hpp -- sorting algorithms benchmark
 * 
 * Copyright (C) 2014 - 2017 Piotr Tarsa ( http://github.com/tarsa )
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
 */
#ifndef SORTHEAPSIMDDWORDVARIANTB_HPP
#define	SORTHEAPSIMDDWORDVARIANTB_HPP

#include "sortalgocommon.hpp"

#include <x86intrin.h>

namespace tarsa {

    ssize_t constexpr Arity = 8;

    template<typename ItemType, bool Ascending>
    bool ordered(ItemType const &a, ItemType const &b) {
    }

    template<>
    bool ordered<int32_t, true>(int32_t const &a, int32_t const &b) {
        return a < b;
    }

    template<>
    bool ordered<uint32_t, true>(uint32_t const &a, uint32_t const &b) {
        return a < b;
    }

    template<>
    bool ordered<int32_t, false>(int32_t const &a, int32_t const &b) {
        return a > b;
    }

    template<>
    bool ordered<uint32_t, false>(uint32_t const &a, uint32_t const &b) {
        return a > b;
    }

    template<bool Signed, bool Ascending>
    __m256i verticalLeaderSelect(__m256i const a, __m256i const b) {
    }

    template<>
    __m256i verticalLeaderSelect<true, true>(
            const __m256i a, const __m256i b) {
        return _mm256_max_epi32(a, b);
    }

    template<>
    __m256i verticalLeaderSelect<false, true>(
            const __m256i a, const __m256i b) {
        return _mm256_max_epu32(a, b);
    }

    template<>
    __m256i verticalLeaderSelect<true, false>(
            const __m256i a, const __m256i b) {
        return _mm256_min_epi32(a, b);
    }

    template<>
    __m256i verticalLeaderSelect<false, false>(
            const __m256i a, const __m256i b) {
        return _mm256_min_epu32(a, b);
    }

    template<typename ItemType, bool Signed, bool Ascending, bool Payload>
    class TheSorter {
        ItemType * const a;
        ssize_t const count;

        /*
         * based on: http://stackoverflow.com/a/23592221/492749
         */
        ssize_t leaderIndex(ItemType const * const a) {
            __m256i v1 = _mm256_load_si256((__m256i *) a);
            __m256i v2 = v1;
            v2 = verticalLeaderSelect<Signed, Ascending>(v2, 
                    _mm256_alignr_epi8(v2, v2, 4));
            v2 = verticalLeaderSelect<Signed, Ascending>(v2, 
                    _mm256_alignr_epi8(v2, v2, 8));
            v2 = verticalLeaderSelect<Signed, Ascending>(v2,
                    _mm256_permute2x128_si256(v2, v2, 0x01));
            __m256i vcmp = _mm256_cmpeq_epi32(v1, v2);
            uint32_t mask = _mm256_movemask_epi8(vcmp);
            return __builtin_ctz(mask) / 4;
        }

        void siftDown(ssize_t root, ssize_t child1, ssize_t const count) {
            while (child1 < count) {
                if (child1 + Arity - 1 < count) {
                    ssize_t const leader = child1 + leaderIndex(a + child1);
                    if (ordered<ItemType, Ascending>(a[root], a[leader])) {
                        std::swap(a[root], a[leader]);
                        root = leader;
                        child1 = (root + 1) * Arity;
                    } else {
                        return;
                    }
                } else {
                    ssize_t leader = root;
                    ssize_t lastChild = std::min(child1 + Arity - 1, count - 1);
                    for (ssize_t child = child1; child <= lastChild; child++) {
                        if (ordered<ItemType, Ascending>(a[leader], a[child])) {
                            leader = child;
                        }
                    }
                    std::swap(a[root], a[leader]);
                    return;
                }
            }
        }

        void heapify() {
            for (ssize_t item = count / Arity - 1; item >= 0; item--) {
                siftDown(item, (item + 1) * Arity, count);
            }
        }

        void drainHeap() {
            for (ssize_t next = count - 1; next > 0; next--) {
                siftDown(next, 0, next);
            }
        }

    public:
        TheSorter(ItemType * const a, ssize_t const count): a(a), count(count) {
        }

        void heapsort() {
            heapify();
            drainHeap();
        }
    };

    template<typename ItemType, bool Ascending = true, bool Payload = false >
    void SimdDwordHeapSortVariantB(ItemType * const a,
            ssize_t const count) {
        static_assert(Payload == false, "payload not implemented");
        bool constexpr ok = std::is_same<ItemType, int32_t>::value
                || std::is_same<ItemType, uint32_t>::value;
        static_assert(ok, "parameters invalid or specialization missing");
        TheSorter<ItemType, std::is_same<ItemType, int32_t>::value, Ascending,
                Payload>(a, count).heapsort();
    }
}

#endif	/* SORTHEAPSIMDDWORDVARIANTB_HPP */
