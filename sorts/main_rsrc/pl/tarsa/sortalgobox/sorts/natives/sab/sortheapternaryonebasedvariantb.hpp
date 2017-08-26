/* 
 * sortheapternaryonebasedvariantb.hpp -- sorting algorithms benchmark
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
#ifndef SORTHEAPTERNARYONEBASEDVARIANTB_HPP
#define	SORTHEAPTERNARYONEBASEDVARIANTB_HPP

#include "sortalgocommon.hpp"

namespace tarsa {

    namespace privateOneBasedTernaryHeapSortVariantB {

        template<typename ItemType, ComparisonOperator<ItemType> compOp>
        void siftDown(ItemType * const a, ssize_t const start,
                ssize_t const end) {
            ssize_t root = start;
            while (root * 3 + 1 <= end) {
                ssize_t const first = root * 3 - 1;
                ssize_t const middle = first + 1;
                ssize_t const last = middle + 1;

                if (compOp(a[root], Below, a[first])) {
                    if (compOp(a[first], Below, a[middle])) {
                        if (compOp(a[middle], Below, a[last])) {
                            std::swap(a[root], a[last]);
                            root = last;
                        } else {
                            std::swap(a[root], a[middle]);
                            root = middle;
                        }
                    } else {
                        if (compOp(a[first], Below, a[last])) {
                            std::swap(a[root], a[last]);
                            root = last;
                        } else {
                            std::swap(a[root], a[first]);
                            root = first;
                        }
                    }
                } else {
                    if (compOp(a[root], Below, a[middle])) {
                        if (compOp(a[middle], Below, a[last])) {
                            std::swap(a[root], a[last]);
                            root = last;
                        } else {
                            std::swap(a[root], a[middle]);
                            root = middle;
                        }
                    } else {
                        if (compOp(a[root], Below, a[last])) {
                            std::swap(a[root], a[last]);
                            root = last;
                        } else {
                            return;
                        }
                    }
                }
            }
            {
                ssize_t const first = root * 3 - 1;
                ssize_t const middle = first + 1;
                ssize_t biggest = root;

                if (first <= end && compOp(a[biggest], Below, a[first])) {
                    biggest = first;
                }
                if (middle <= end && compOp(a[biggest], Below, a[middle])) {
                    biggest = middle;
                }
                std::swap(a[root], a[biggest]);
            }
        }

        template<typename ItemType, ComparisonOperator<ItemType> compOp>
        void heapify(ItemType * const a, ssize_t const count) {
            for (ssize_t item = (count + 1) / 3; item >= 1; item--) {
                siftDown<ItemType, compOp>(a, item, count);
            }
        }

        template<typename ItemType, ComparisonOperator<ItemType> compOp>
        void drainHeap(ItemType * const a, ssize_t const count) {
            for (ssize_t next = count; next > 1; next--) {
                std::swap(a[next], a[1]);
                siftDown<ItemType, compOp>(a, 1, next - 1);
            }
        }

        template<typename ItemType, ComparisonOperator<ItemType> compOp>
        void heapsort(ItemType * const a, ssize_t const count) {
            heapify<ItemType, compOp>(a, count);
            drainHeap<ItemType, compOp>(a, count);
        }
    }

    template<typename ItemType, ComparisonOperator<ItemType> compOp>
    void OneBasedTernaryHeapSortVariantB(ItemType * const a,
            ssize_t const count) {
        privateOneBasedTernaryHeapSortVariantB::heapsort<ItemType, compOp>(
                a - 1, count);
    }

    template<typename ItemType>
    void OneBasedTernaryHeapSortVariantB(ItemType * const a,
            ssize_t const count) {
        OneBasedTernaryHeapSortVariantB<ItemType, genericComparisonOperator>(
                a, count);
    }
}

#endif	/* SORTHEAPTERNARYONEBASEDVARIANTB_HPP */
