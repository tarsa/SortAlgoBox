/* 
 * sortheapsimddwordcascadingvariantc.hpp -- sorting algorithms benchmark
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
#ifndef SORTHEAPSIMDDWORDCASCADINGVARIANTC_HPP
#define	SORTHEAPSIMDDWORDCASCADINGVARIANTC_HPP

#include "sortalgocommon.hpp"

#include <x86intrin.h>

namespace tarsa {

    ssize_t constexpr Arity = 8;

    ssize_t constexpr QueueSize = 64;

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
    __m128i verticalLeaderSelect(__m128i const a, __m128i const b) {
    }

    template<>
    __m128i verticalLeaderSelect<true, true>(
            const __m128i a, const __m128i b) {
        return _mm_max_epi32(a, b);
    }

    template<>
    __m128i verticalLeaderSelect<false, true>(
            const __m128i a, const __m128i b) {
        return _mm_max_epu32(a, b);
    }

    template<>
    __m128i verticalLeaderSelect<true, false>(
            const __m128i a, const __m128i b) {
        return _mm_min_epi32(a, b);
    }

    template<>
    __m128i verticalLeaderSelect<false, false>(
            const __m128i a, const __m128i b) {
        return _mm_min_epu32(a, b);
    }

    template<typename ItemType, bool Signed, bool Ascending, bool Payload>
    class TheSorter {
        ItemType * const a;
        ssize_t const count;
        ssize_t queueLength;
        ssize_t queueStoreIndex;
        ssize_t queue[QueueSize];

        static_assert(Payload == false, "payload not implemented");
        static_assert(std::is_same<ItemType, int32_t>::value ||
                      std::is_same<ItemType, uint32_t>::value,
                      "parameters invalid or specialization missing");

        /*
         * based on: http://stackoverflow.com/a/23593786/492749
         */
        ssize_t leaderIndex(ItemType const * const a) {
            __m256i vec = _mm256_load_si256((__m256i *) a);
            __m128i lo = _mm256_castsi256_si128(vec);
            __m128i hi = _mm256_extractf128_si256(vec, 1);
            __m128i tmp = verticalLeaderSelect<Signed, Ascending>(lo, hi);
            tmp = verticalLeaderSelect<Signed, Ascending>(tmp,
                    _mm_shuffle_epi32(tmp, _MM_SHUFFLE(1, 0, 3, 2)));
            tmp = verticalLeaderSelect<Signed, Ascending>(tmp,
                    // using lo_epi16 for speed here
                    _mm_shufflelo_epi16(tmp, _MM_SHUFFLE(1, 0, 3, 2))); 
            ItemType const leader = _mm_cvtsi128_si32(tmp);
            tmp = _mm_packs_epi16(_mm_packs_epi32(
                    _mm_cmpeq_epi32(_mm_set1_epi32(leader), lo),
                    _mm_cmpeq_epi32(_mm_set1_epi32(leader), hi)),
                    _mm_setzero_si128());
            return __builtin_ctz(_mm_movemask_epi8(tmp));
        }

        void siftDownSingleStep(ssize_t const count, ssize_t const root,
                ssize_t const child1) {
            if (child1 + Arity - 1 < count) {
                ssize_t const leader = child1 + leaderIndex(a + child1);
                if (ordered<ItemType, Ascending>(a[root], a[leader])) {
                    std::swap(a[root], a[leader]);
                    queue[queueStoreIndex] = leader;
                    queueStoreIndex++;
                    prefetch(a + std::min((leader + 1) * Arity,
                            count - 1));
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
            }
        }

        void siftDownCascaded(ssize_t const count) {
            for (ssize_t index = 0; index < queueLength; index++) {
                siftDownSingleStep(count, queue[index],
                    (queue[index] + 1) * Arity);
            }
            siftDownSingleStep(count, count - 1, 0);
        }

        void siftDown(ssize_t root, ssize_t child1) {
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
                siftDown(item, (item + 1) * Arity);
            }
        }

        void drainHeap() {
            queueLength = 0;
            for (ssize_t next = count; next > 0; next--) {
                queueStoreIndex = 0;
                siftDownCascaded(next);
                queueLength = queueStoreIndex;
            }
        }

    public:
        TheSorter(ItemType * const a, ssize_t const count): a(a), count(count) {
        }

        void sort() {
            heapify();
            drainHeap();
        }
    };

    template<typename item_t>
    using Sorter = TheSorter<item_t, true, true, false>;

    template<typename ItemType>
    Sorter<ItemType> * makeSorter(ItemType * const a, ssize_t const count) {
        return new TheSorter<ItemType, std::is_same<ItemType, int32_t>::value,
            true, false>(a, count);
    }
}

#endif	/* SORTHEAPSIMDDWORDCASCADINGVARIANTB_HPP */
