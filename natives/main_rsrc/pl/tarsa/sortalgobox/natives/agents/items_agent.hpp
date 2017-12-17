/*
 * Copyright (C) 2015 - 2017 Piotr Tarsa ( http://github.com/tarsa )
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
#ifndef ITEMS_AGENT_HPP
#define ITEMS_AGENT_HPP

#include <cstdint>
#include <cstring>

#define ITEMS_AGENT_TYPE_START        8191029952343308046L
#define ITEMS_AGENT_TYPE_PLAIN        ITEMS_AGENT_TYPE_START + 1
#define ITEMS_AGENT_TYPE_RECORDING    ITEMS_AGENT_TYPE_START + 2

static_assert(
    ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_PLAIN ||
    ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING ||
    false, "Valid items agent type must be selected");

#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
#include "number_codec.hpp"
#include "action_codes.hpp"
#endif

namespace tarsa {

    template<typename item_t>
    struct Buffer {
        item_t * const array;
        size_t const length;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
        int32_t const id;
#endif

        Buffer(item_t * const array, size_t const length, int32_t const id):
            array(array), length(length)
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            , id(id)
#endif
            {}
    };

    class ItemsAgent {
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
        NumberEncoder * const recorder;
#endif

    public:
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_PLAIN
        ItemsAgent() {}
#else
        ItemsAgent(NumberEncoder * const recorder): recorder(recorder) {}
#endif

        template<typename item_t>
        size_t size(Buffer<item_t> const buffer) {
            size_t const result = buffer.length;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeSize);
            recorder->serializeByte(buffer.id);
            recorder->serializeInt((int32_t) result);
#endif
            return result;
        }

        template<typename item_t>
        item_t get(Buffer<item_t> const buffer, size_t const index) {
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeGet);
            recorder->serializeByte(buffer.id);
            recorder->serializeInt((int32_t) index);
#endif
            return buffer.array[index];
        }

        template<typename item_t>
        void set(Buffer<item_t> const buffer, size_t const index,
                item_t const value) {
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeSet);
            recorder->serializeByte(buffer.id);
            recorder->serializeInt((int32_t) index);
#endif
            buffer.array[index] = value;
        }

        template<typename item_t>
        void swap(Buffer<item_t> const buffer, size_t const index1,
                size_t const index2) const {
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeSwap);
            recorder->serializeByte(buffer.id);
            recorder->serializeInt((int32_t) index1);
            recorder->serializeInt((int32_t) index2);
#endif
            item_t const item = buffer.array[index1];
            buffer.array[index1] = buffer.array[index2];
            buffer.array[index2] = item;
        }

        template<typename item_t>
        int32_t itemBitsSize(Buffer<item_t> const buffer) {
            int32_t result = sizeof(item_t) * 8;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeItemBitsSize);
            recorder->serializeByte(buffer.id);
            recorder->serializeByte(result);
#endif
            return result;
        }

        template<typename item_t>
        int64_t asLong(item_t const value) {
            int64_t const result = (int64_t) result;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeAsLong);
            recorder->serializeLong(result);
#endif
            return result;
        }

        template<typename item_t>
        int64_t asLongI(Buffer<item_t> const buffer, size_t const index) {
            return asLong<item_t>(get(buffer, index));
        }

        template<typename item_t>
        int32_t getSlice(item_t const value, int32_t const lowestBitIndex,
                int32_t const length) {
            int32_t const result = ((int32_t) (value >> lowestBitIndex)) &
                ((1 << length) - 1);
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeGetSlice);
            recorder->serializeByte(lowestBitIndex);
            recorder->serializeByte(length);
            recorder->serializeInt(result);
#endif
            return result;
        }

        template<typename item_t>
        int32_t getSliceI(Buffer<item_t> const buffer, size_t const index,
                int32_t const lowestBitIndex, int32_t const length) {
            return getSlice<item_t>(get(buffer, index), lowestBitIndex, length);
        }
        
        template<typename item_t>
        bool compareEq(item_t const value1, item_t const value2) {
            bool const result = value1 == value2;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeCompareEq);
            recorder->serializeBit(result);
#endif
            return result;
        }
        
        template<typename item_t>
        bool compareEqI(Buffer<item_t> const buffer, size_t const index1,
                size_t const index2) {
            item_t const value1 = get(buffer, index1);
            item_t const value2 = get(buffer, index2);
            return compareEq(value1, value2);
        }
        
        template<typename item_t>
        bool compareEqIV(Buffer<item_t> const buffer, size_t const index1,
                item_t const value2) {
            return compareEq(get(buffer, index1), value2);
        }
        
        template<typename item_t>
        bool compareEqVI(Buffer<item_t> const buffer, item_t const value1,
                size_t const index2) {
            return compareEq(value1, get(buffer, index2));
        }

        template<typename item_t>
        bool compareGt(item_t const value1, item_t const value2) {
            bool const result = value1 > value2;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeCompareGt);
            recorder->serializeBit(result);
#endif
            return result;
        }
        
        template<typename item_t>
        bool compareGtI(Buffer<item_t> const buffer, size_t const index1,
                size_t const index2) {
            item_t const value1 = get(buffer, index1);
            item_t const value2 = get(buffer, index2);
            return compareGt(value1, value2);
        }
        
        template<typename item_t>
        bool compareGtIV(Buffer<item_t> const buffer, size_t const index1,
                item_t const value2) {
            return compareGt(get(buffer, index1), value2);
        }
        
        template<typename item_t>
        bool compareGtVI(Buffer<item_t> const buffer, item_t const value1,
                size_t const index2) {
            return compareGt(value1, get(buffer, index2));
        }


        template<typename item_t>
        bool compareGte(item_t const value1, item_t const value2) {
            bool const result = value1 >= value2;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeCompareGte);
            recorder->serializeBit(result);
#endif
            return result;
        }
        
        template<typename item_t>
        bool compareGteI(Buffer<item_t> const buffer, size_t const index1,
                size_t const index2) {
            item_t const value1 = get(buffer, index1);
            item_t const value2 = get(buffer, index2);
            return compareGte(value1, value2);
        }
        
        template<typename item_t>
        bool compareGteIV(Buffer<item_t> const buffer, size_t const index1,
                item_t const value2) {
            return compareGte(get(buffer, index1), value2);
        }
        
        template<typename item_t>
        bool compareGteVI(Buffer<item_t> const buffer, item_t const value1,
                size_t const index2) {
            return compareGte(value1, get(buffer, index2));
        }

        template<typename item_t>
        bool compareLt(item_t const value1, item_t const value2) {
            bool const result = value1 < value2;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeCompareLt);
            recorder->serializeBit(result);
#endif
            return result;
        }
        
        template<typename item_t>
        bool compareLtI(Buffer<item_t> const buffer, size_t const index1,
                size_t const index2) {
            item_t const value1 = get(buffer, index1);
            item_t const value2 = get(buffer, index2);
            return compareLt(value1, value2);
        }
        
        template<typename item_t>
        bool compareLtIV(Buffer<item_t> const buffer, size_t const index1,
                item_t const value2) {
            return compareLt(get(buffer, index1), value2);
        }
        
        template<typename item_t>
        bool compareLtVI(Buffer<item_t> const buffer, item_t const value1,
                size_t const index2) {
            return compareLt(value1, get(buffer, index2));
        }

        template<typename item_t>
        bool compareLte(item_t const value1, item_t const value2) {
            bool const result = value1 <= value2;
#if ITEMS_AGENT_TYPE == ITEMS_AGENT_TYPE_RECORDING
            recorder->serializeByte(CodeCompareLte);
            recorder->serializeBit(result);
#endif
            return result;
        }
        
        template<typename item_t>
        bool compareLteI(Buffer<item_t> const buffer, size_t const index1,
                size_t const index2) {
            item_t const value1 = get(buffer, index1);
            item_t const value2 = get(buffer, index2);
            return compareLte(value1, value2);
        }
        
        template<typename item_t>
        bool compareLteIV(Buffer<item_t> const buffer, size_t const index1,
                item_t const value2) {
            return compareLte(get(buffer, index1), value2);
        }
        
        template<typename item_t>
        bool compareLteVI(Buffer<item_t> const buffer, item_t const value1,
                size_t const index2) {
            return compareLte(value1, get(buffer, index2));
        }
    };
}

#endif /* ITEMS_AGENT_HPP */
