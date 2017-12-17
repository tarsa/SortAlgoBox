/* 
 * sortheapbinaryclusteredvarianta.hpp -- sorting algorithms benchmark
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
#ifndef SORTHEAPBINARYCLUSTEREDVARIANTA_HPP
#define	SORTHEAPBINARYCLUSTEREDVARIANTA_HPP

#include "sortalgocommon.hpp"

namespace tarsa {

    using namespace privateClusteredHeapsorts;

    ssize_t constexpr arity = 2;

    template<typename ItemType, ComparisonOperator<ItemType> compOp,
        ssize_t clusterLevels>
    class TheSorter {
        ItemType * const a;
        ssize_t const count;

        void siftDown(ssize_t item, ssize_t const count, ssize_t clusterStart) {
            ssize_t constexpr clusterSize =
                    computeClusterSize<clusterLevels>(arity);
            ssize_t constexpr clusterArity =
                    computeClusterLevelSize<clusterLevels>(arity);
            ssize_t constexpr relativeLastLevelStart =
                    computeClusterSize < clusterLevels - 1 > (arity);

            ssize_t relativeItem = item - clusterStart;

            while (item < count) {
                ssize_t relativeEnd = count - clusterStart;

                ItemType * const cluster = a + clusterStart;
                while (relativeItem < relativeLastLevelStart) {
                    ssize_t const relativeLeft = relativeItem * arity + 1;
                    ssize_t const relativeRight = relativeItem * arity + 2;

                    if (relativeRight < relativeEnd) {
                        if (compOp(cluster[relativeItem], Below,
                                cluster[relativeLeft])) {
                            if (compOp(cluster[relativeLeft], Below,
                                    cluster[relativeRight])) {
                                std::swap(cluster[relativeItem],
                                        cluster[relativeRight]);
                                relativeItem = relativeRight;
                            } else {
                                std::swap(cluster[relativeItem],
                                        cluster[relativeLeft]);
                                relativeItem = relativeLeft;
                            }
                        } else {
                            if (compOp(cluster[relativeItem], Below,
                                    cluster[relativeRight])) {
                                std::swap(cluster[relativeItem],
                                        cluster[relativeRight]);
                                relativeItem = relativeRight;
                            } else {
                                return;
                            }
                        }
                    } else {
                        if (relativeLeft < relativeEnd && compOp(
                                cluster[relativeItem], Below,
                                cluster[relativeLeft])) {
                            std::swap(cluster[relativeItem],
                                    cluster[relativeLeft]);
                        }
                        return;
                    }
                }
                item = clusterStart + relativeItem;
                {
                    ssize_t const left = clusterStart * clusterArity + (1 +
                            (relativeItem - relativeLastLevelStart) * arity)
                            * clusterSize;
                    ssize_t const right = left + clusterSize;

                    ssize_t max = item;

                    if (right < count) {
                        if (compOp(a[max], Below, a[left])) {
                            max = left;
                        }
                        if (compOp(a[max], Below, a[right])) {
                            max = right;
                        }
                    } else if (left < count && compOp(a[max], Below, a[left])) {
                        max = left;
                    } else {
                        return;
                    }
                    if (max != item) {
                        std::swap(a[item], a[max]);
                        item = max;
                    } else {
                        return;
                    }

                    clusterStart = item;
                }
                relativeItem = 0;
            }
        }

        void heapify() {
            ssize_t constexpr clusterSize =
                    computeClusterSize<clusterLevels>(arity);

            for (ssize_t item = count - 1,
                    localClusterStart = item / clusterSize * clusterSize;
                    item >= 0; localClusterStart -= clusterSize) {
                while (item >= localClusterStart) {
                    siftDown(item, count, localClusterStart);
                    item--;
                }
            }
        }

        void drainHeap() {
            for (ssize_t next = count - 1; next > 0; next--) {
                std::swap(a[next], a[0]);
                siftDown(0, next, 0);
            }
        }

    public:
        TheSorter(ItemType * const a, ssize_t const count):
            a(a), count(count) {}

        void sort() {
            heapify();
            drainHeap();
        }
    };

    template<typename item_t>
    using Sorter = TheSorter<item_t, genericComparisonOperator, 4>;

    template<typename ItemType>
    Sorter<ItemType> * makeSorter(ItemType * const a, ssize_t const count) {
        return new TheSorter<ItemType, genericComparisonOperator, 4>(a, count);
    }
}

#endif	/* SORTHEAPBINARYCLUSTEREDVARIANTA_HPP */
