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
#ifndef ITEMS_HANDLER_HPP
#define ITEMS_HANDLER_HPP

#include <algorithm>
#include <cstdint>

#include "utilities.hpp"

#define ITEMS_HANDLER_START                        5346324
#define ITEMS_HANDLER_AGENT_COMPARING              ITEMS_HANDLER_START + 1
#define ITEMS_HANDLER_AGENT_RECORDING_COMPARING    ITEMS_HANDLER_START + 2
#define ITEMS_HANDLER_RAW                          ITEMS_HANDLER_START + 3
#define ITEMS_HANDLER_RAW_REFERENCE                ITEMS_HANDLER_START + 4
#define ITEMS_HANDLER_SAB_CACHED                   ITEMS_HANDLER_START + 5

static_assert(
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING ||
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING ||
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW ||
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW_REFERENCE ||
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED ||
    false, "Valid items handler type must be selected");

#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
#include "comparing_array_items_agent.hpp"
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
#include "buffered_io.hpp"
#include "number_codec.hpp"
#include "recording_comparing_items_agent.hpp"
#endif

#include "mwc64x.hpp"


template<typename item_t>
struct items_handler_t {
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING
    tarsa::ComparingArrayItemsAgent<item_t> * agent;
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
    tarsa::BufferedWriter * writer;
    tarsa::NumberEncoder * numberEncoder;
    tarsa::ComparingArrayItemsAgent<int32_t> * basicAgent;
    tarsa::RecordingComparingItemsAgentSetup<tarsa::ComparingArrayItemsAgent>::
        Result<int32_t> * agent;
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW_REFERENCE || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    item_t * input;
    size_t size;
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    int8_t * scratchpad;
#endif
};

template<typename item_t>
items_handler_t<item_t> sortItemsHandlerPrepare(item_t * const input,
        size_t const size) {
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
    std::string recordingFilePath;
    std::cin >> recordingFilePath;
#endif
    items_handler_t<item_t> itemsHandler;
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING
    itemsHandler.agent = checkNonNull(
        new tarsa::ComparingArrayItemsAgent<item_t>(input, size));
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
    itemsHandler.writer = checkNonNull(new tarsa::BufferedFileWriter(
        recordingFilePath, 1 << 20, true));
    itemsHandler.numberEncoder = checkNonNull(
        new tarsa::NumberEncoder(itemsHandler.writer));
    itemsHandler.basicAgent = checkNonNull(
        new tarsa::ComparingArrayItemsAgent<item_t>(input, size));
    itemsHandler.agent = checkNonNull(new tarsa::
        RecordingComparingItemsAgentSetup<tarsa::ComparingArrayItemsAgent>::
        Result<int32_t>(itemsHandler.numberEncoder, *itemsHandler.basicAgent));
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW_REFERENCE || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    itemsHandler.input = input;
    itemsHandler.size = size;
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    checkZero(posix_memalign((void**) &itemsHandler.scratchpad, 128,
        sizeof (int8_t) * size));
#endif
    return itemsHandler;
}

template<typename item_t>
void sortItemsHandlerReleasePreValidation(
        items_handler_t<item_t> &itemsHandler) {
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
    safeDelete(itemsHandler.agent);
    itemsHandler.numberEncoder->flush();
    safeDelete(itemsHandler.numberEncoder);
    safeDelete(itemsHandler.writer);
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    safeFree(itemsHandler.scratchpad);
#endif
}

template<typename item_t>
void sortItemsHandlerReleasePostValidation(
        items_handler_t<item_t> &itemsHandler) {
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING
    safeDelete(itemsHandler.agent);
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
    safeDelete(itemsHandler.basicAgent);
#endif
}

bool sortValidate(items_handler_t<int32_t> const &itemsHandler) {
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING
#define compareItemsLt(index1, index2) \
    itemsHandler.agent->compareLt0(index1, index2)
#define getItem(index) itemsHandler.agent->get0(index)
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
#define compareItemsLt(index1, index2) \
    itemsHandler.basicAgent->compareLt0(index1, index2)
#define getItem(index) itemsHandler.basicAgent->get0(index)
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW_REFERENCE || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    int32_t const * const work = itemsHandler.input;
#define compareItemsLt(index1, index2) (work[index1] < work[index2])
#define getItem(index) work[index]
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_COMPARING
    size_t const size = itemsHandler.agent->size0();
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING_COMPARING
    size_t const size = itemsHandler.basicAgent->size0();
#endif
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_RAW_REFERENCE || \
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_SAB_CACHED
    size_t const size = itemsHandler.size;
#endif
#if ITEMS_HANDLER_TYPE != ITEMS_HANDLER_RAW_REFERENCE
    int32_t * reference;
    checkZero(posix_memalign((void**) &reference, 128,
            sizeof (int32_t) * size));
    mwc64xFill(reference, size);
    std::sort(reference, reference + size);
#endif
    bool valid = true;
    for (size_t i = 0; valid && (i < size); i++) {
        valid &= (i == 0) || !compareItemsLt(i, i - 1);
#if ITEMS_HANDLER_TYPE != ITEMS_HANDLER_RAW_REFERENCE
        valid &= getItem(i) == reference[i];
#endif
    }
    return valid;
#undef compareItems
#undef getItem
}

#endif // ITEMS_HANDLER_HPP
