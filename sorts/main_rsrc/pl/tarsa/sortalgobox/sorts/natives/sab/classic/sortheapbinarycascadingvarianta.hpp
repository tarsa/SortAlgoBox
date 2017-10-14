/* 
 * sortheapbinarycascadingvarianta.hpp -- sorting algorithms benchmark
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
#ifndef SORTHEAPBINARYCASCADINGVARIANTA_HPP
#define	SORTHEAPBINARYCASCADINGVARIANTA_HPP

#include "sortalgocommon.hpp"

namespace tarsa {

    ssize_t constexpr QueueSize = 64;

    template<typename ItemType, ComparisonOperator<ItemType> compOp>
    class TheSorter {
        ItemType * const a;
        ssize_t const count;
        ssize_t queueLength;
        ssize_t queueStoreIndex;
        ssize_t queue[QueueSize];

        void siftDown(ssize_t const start, ssize_t const end) {
            ssize_t root = start;
            while (true) {
                ssize_t const left = root * 2;
                ssize_t const right = left + 1;

                if (right <= end) {
                    if (compOp(a[root], Below, a[left])) {
                        if (compOp(a[left], Below, a[right])) {
                            std::swap(a[root], a[right]);
                            root = right;
                        } else {
                            std::swap(a[root], a[left]);
                            root = left;
                        }
                    } else {
                        if (compOp(a[root], Below, a[right])) {
                            std::swap(a[root], a[right]);
                            root = right;
                        } else {
                            return;
                        }
                    }
                } else {
                    if (left == end && compOp(a[root], Below, a[left])) {
                        std::swap(a[root], a[left]);
                    }
                    return;
                }
            }
        }

        void heapify() {
            for (ssize_t item = count / 2; item >= 1; item--) {
                siftDown(item, count);
            }
        }

        void siftDownSingleStep(ssize_t const end, ssize_t const root) {
            ssize_t const left = root * 2;
            ssize_t const right = left + 1;

            if (right <= end) {
                ssize_t const maxChild = root * 2
                        + compOp(a[left], Below, a[right]);
                if (compOp(a[root], Below, a[maxChild])) {
                    std::swap(a[root], a[maxChild]);
                    queue[queueStoreIndex] = maxChild;
                    queueStoreIndex++;
                    prefetch(a + std::min(maxChild * 2, end));
                }
            } else {
                if (left == end && compOp(a[root], Below, a[left])) {
                    std::swap(a[root], a[left]);
                }
            }
        }

        void siftDownCascaded(ssize_t const start, ssize_t const end) {
            queue[queueLength] = start;
            queueLength++;
            for (ssize_t queueIndex = 0; queueIndex < queueLength;
                    queueIndex++) {
                siftDownSingleStep(end, queue[queueIndex]);
            }
        }

        void drainHeap() {
            queueLength = 0;
            for (ssize_t next = count; next > 1; next--) {
                std::swap(a[next], a[1]);
                queueStoreIndex = 0;
                siftDownCascaded(1, next - 1);
                queueLength = queueStoreIndex;
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

    template<typename ItemType, ComparisonOperator<ItemType> compOp>
    void BinaryHeapSortCascadingVariantA(ItemType * const a,
            ssize_t const count) {
        TheSorter<ItemType, compOp>(a - 1, count).heapsort();
    }

    template<typename ItemType>
    void BinaryHeapSortCascadingVariantA(ItemType * const a,
            ssize_t const count) {
        BinaryHeapSortCascadingVariantA<ItemType, genericComparisonOperator>(
                a, count);
    }
}

#endif	/* SORTHEAPBINARYCASCADINGVARIANTA_HPP */
