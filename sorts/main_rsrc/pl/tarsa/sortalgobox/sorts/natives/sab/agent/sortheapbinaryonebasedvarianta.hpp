/*
 * sortheapbinaryonebasedvarianta.hpp -- sorting algorithms benchmark
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
#ifndef SORTHEAPBINARYONEBASEDVARIANTA_HPP
#define	SORTHEAPBINARYONEBASEDVARIANTA_HPP

#include "sortalgocommon.hpp"

namespace tarsa {

    template<typename item_t>
    class Sorter {
        ItemsAgent a;
        Buffer<item_t> buf;

    public:
        Sorter(ItemsAgent const agent, item_t * const array,
                size_t const length): a(agent),
                buf(Buffer<item_t>(array - 1, length, 0)) {}

        void sort() {
            heapify();
            drainHeap();
        }

        void heapify() {
            size_t const count = a.size(buf);
            for (ssize_t item = count / 2; item >= 1; item--) {
                siftDown(item, count);
            }
        }

        void siftDown(ssize_t const start, ssize_t const end) {
            ssize_t root = start;
            while (true) {
                ssize_t const left = root * 2;
                ssize_t const right = left + 1;

                if (right <= end) {
                    if (a.compareLtI(buf, root, left)) {
                        if (a.compareLtI(buf, left, right)) {
                            a.swap(buf, root, right);
                            root = right;
                        } else {
                            a.swap(buf, root, left);
                            root = left;
                        }
                    } else {
                        if (a.compareLtI(buf, root, right)) {
                            a.swap(buf, root, right);
                            root = right;
                        } else {
                            return;
                        }
                    }
                } else {
                    if (left == end && a.compareLtI(buf, root, left)) {
                        a.swap(buf, root, left);
                    }
                    return;
                }
            }
        }

        void drainHeap() {
            size_t const count = a.size(buf);
            for (ssize_t next = count; next > 1; next--) {
                a.swap(buf, next, 1);
                siftDown(1, next - 1);
            }
        }
    };

    template<typename item_t>
    Sorter<item_t> * makeSorter(items_handler_t<item_t> &handler) {
        return new Sorter<item_t>(*handler.agent, handler.input, handler.size);
    }
}

#endif	/* SORTHEAPBINARYONEBASEDVARIANTA_HPP */
