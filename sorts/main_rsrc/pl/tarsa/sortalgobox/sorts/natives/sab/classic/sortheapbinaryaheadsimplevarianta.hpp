/* 
 * sortheapbinaryaheadsimplevarianta.hpp -- sorting algorithms benchmark
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
#ifndef SORTHEAPBINARYAHEADSIMPLEVARIANTA_HPP
#define	SORTHEAPBINARYAHEADSIMPLEVARIANTA_HPP

#include "sortalgocommon.hpp"

namespace tarsa {

    template<typename ItemType, ComparisonOperator<ItemType> compOp>
    class TheSorter {
        ItemType * const a;
        ssize_t const count;

        void siftDown(ssize_t const start, ssize_t const end) {
            ItemType const item = a[start];
            ssize_t index = start;
            while (index * 2 + 1 <= end) {
                ssize_t const left = index * 2;
                ssize_t const right = left + 1;
                prefetch<1, 0>(a + std::min(left * 8, end));
                ssize_t const newIndex = index * 2
                        + compOp(a[left], Below, a[right]);
                a[index] = a[newIndex];
                index = newIndex;
            }
            if (index * 2 == end) {
                ssize_t const newIndex = index * 2;
                a[index] = a[newIndex];
                index = newIndex;
            }
            while (index / 2 >= start && compOp(a[index / 2], Below, item)) {
                a[index] = a[index / 2];
                index /= 2;
            }
            a[index] = item;
        }

        void heapify() {
            for (ssize_t item = count / 2; item >= 1; item--) {
                siftDown(item, count);
            }
        }

        void drainHeap() {
            for (ssize_t next = count; next > 1; next--) {
                std::swap(a[next], a[1]);
                siftDown(1, next - 1);
            }
        }

    public:
        TheSorter(ItemType * const a, ssize_t const count):
            a(a - 1), count(count) {}

        void sort() {
            heapify();
            drainHeap();
        }
    };

    template<typename item_t>
    using Sorter = TheSorter<item_t, genericComparisonOperator>;

    template<typename ItemType>
    Sorter<ItemType> * makeSorter(ItemType * const a, ssize_t const count) {
        return new TheSorter<ItemType, genericComparisonOperator>(a, count);
    }
}

#endif	/* SORTHEAPBINARYAHEADSIMPLEVARIANTA_HPP */
