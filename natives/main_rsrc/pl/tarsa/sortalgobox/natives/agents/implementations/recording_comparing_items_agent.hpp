/*
 * Copyright (C) 2015, 2016 Piotr Tarsa ( http://github.com/tarsa )
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
 */
#ifndef RECORDING_COMPARING_ITEMS_AGENT_HPP
#define RECORDING_COMPARING_ITEMS_AGENT_HPP

#include <cstdint>
#include <cstring>

#include "action_codes.hpp"
#include "comparing_items_agent.hpp"

namespace tarsa {

    template<template<typename> class UnderlyingItemsAgent>
    struct RecordingComparingItemsAgentSetup {

        template<typename item_t>
        class RecordingComparingItemsAgent :
                public ComparingItemsAgent<item_t> {
            NumberEncoder * const recorder;
            UnderlyingItemsAgent<item_t> const underlying;

        public:
            RecordingComparingItemsAgent(NumberEncoder * const recorder,
                    UnderlyingItemsAgent<item_t> const underlying):
                recorder(recorder), underlying(underlying) {
            }

            size_t size0() const {
                recorder->serializeInt(CodeSize0);
                return underlying.size0();
            }

            item_t get0(size_t const i) const {
                recorder->serializeInt(CodeGet0);
                recorder->serializeLong(i);
                return underlying.get0(i);
            }

            void set0(size_t const i, item_t const v) const {
                recorder->serializeInt(CodeSet0);
                recorder->serializeLong(i);
                underlying.set0(i, v);
            }

            void copy0(size_t const i, size_t const j, size_t const n) const {
                recorder->serializeInt(CodeCopy0);
                recorder->serializeLong(i);
                recorder->serializeLong(j);
                recorder->serializeLong(n);
                underlying.copy0(i, j, n);
            }

            void swap0(size_t const i, size_t const j) const {
                recorder->serializeInt(CodeSwap0);
                recorder->serializeLong(i);
                recorder->serializeLong(j);
                underlying.swap0(i, j);
            }

            compare_t compare(item_t const a, item_t const b) const {
                recorder->serializeInt(CodeCompare);
                return underlying.compare(a, b);
            }

            bool compareLt(item_t const a, item_t const b) const {
                recorder->serializeInt(CodeCompare);
                return underlying.compareLt(a, b);
            }

            compare_t compare0(size_t const i, size_t const j) const {
                recorder->serializeInt(CodeCompare0);
                recorder->serializeLong(i);
                recorder->serializeLong(j);
                return underlying.compare0(i, j);
            }

            bool compareLt0(size_t const i, size_t const j) const {
                recorder->serializeInt(CodeCompare0);
                recorder->serializeLong(i);
                recorder->serializeLong(j);
                return underlying.compareLt0(i, j);
            }
        };

        template<typename item_t>
        using Result = RecordingComparingItemsAgent<item_t>;
    };
}

#endif /* RECORDING_COMPARING_ITEMS_AGENT_HPP */
