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

    template<template<typename, size_t> class ItemsAgent, typename item_t>
    class privateOneBasedBinaryHeapSortVariantA {
        ItemsAgent<item_t, 1> agent;

    public:
        privateOneBasedBinaryHeapSortVariantA(
            ItemsAgent<item_t, 1> agent): agent(agent) {
        }

        void heapsort() {
            heapify();
            drainHeap();
        }

        void heapify() {
            size_t const count = agent.size0();
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
                    if (agent.compareLt0(root, left)) {
                        if (agent.compareLt0(left, right)) {
                            agent.swap0(root, right);
                            root = right;
                        } else {
                            agent.swap0(root, left);
                            root = left;
                        }
                    } else {
                        if (agent.compareLt0(root, right)) {
                            agent.swap0(root, right);
                            root = right;
                        } else {
                            return;
                        }
                    }
                } else {
                    if (left == end && agent.compareLt0(root, left)) {
                        agent.swap0(root, left);
                    }
                    return;
                }
            }
        }

        void drainHeap() {
            size_t const count = agent.size0();
            for (ssize_t next = count; next > 1; next--) {
                agent.swap0(next, 1);
                siftDown(1, next - 1);
            }
        }
    };

    template<template<typename, size_t> class ItemsAgent, typename item_t>
    void OneBasedBinaryHeapSortVariantA(ItemsAgent<item_t, 1> agent) {
        privateOneBasedBinaryHeapSortVariantA<ItemsAgent, item_t>(agent)
            .heapsort();
    }

    template<template<typename, size_t> class ItemsAgent, typename item_t>
    void OneBasedBinaryHeapSortVariantA(ItemsAgent<item_t, 0> agent) {
        OneBasedBinaryHeapSortVariantA(agent.withBase1());
    }
}

#endif	/* SORTHEAPBINARYONEBASEDVARIANTA_HPP */
