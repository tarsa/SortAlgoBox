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
#ifndef ITEMS_HANDLER_HPP
#define ITEMS_HANDLER_HPP

#include <algorithm>
#include <cstdint>

#include "utilities.hpp"

#define ITEMS_HANDLER_START              -8564120874858705559L
#define ITEMS_HANDLER_AGENT_PLAIN        ITEMS_HANDLER_START + 1
#define ITEMS_HANDLER_AGENT_RECORDING    ITEMS_HANDLER_START + 2
#define ITEMS_HANDLER_REFERENCE          ITEMS_HANDLER_START + 3

static_assert(
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_PLAIN ||
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING ||
    ITEMS_HANDLER_TYPE == ITEMS_HANDLER_REFERENCE ||
    false, "Valid items handler type must be selected");

#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING
#define ITEMS_AGENT_TYPE ITEMS_AGENT_TYPE_RECORDING
#else
#define ITEMS_AGENT_TYPE ITEMS_AGENT_TYPE_PLAIN
#endif

#include "items_agent.hpp"
#include "mwc64x.hpp"


template<typename item_t>
struct items_handler_t {
    item_t * input;
    size_t size;
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING
    tarsa::BufferedWriter * writer;
    tarsa::NumberEncoder * numberEncoder;
#endif
    tarsa::ItemsAgent * agent;
};

template<typename item_t>
items_handler_t<item_t> sortItemsHandlerPrepare(size_t const size) {
    int32_t * work;
    checkZero(posix_memalign((void**) &work, 128, sizeof (int32_t) * size));
    mwc64xFill(work, size);
    items_handler_t<item_t> itemsHandler;
    itemsHandler.input = work;
    itemsHandler.size = size;
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING
    std::string recordingFilePath;
    std::cin >> recordingFilePath;
    itemsHandler.writer = checkNonNull(new tarsa::BufferedFileWriter(
        recordingFilePath, 1 << 20, true));
    itemsHandler.numberEncoder = checkNonNull(
        new tarsa::NumberEncoder(itemsHandler.writer));
    itemsHandler.agent = checkNonNull(
        new tarsa::ItemsAgent(itemsHandler.numberEncoder));
#else
    itemsHandler.agent = checkNonNull(new tarsa::ItemsAgent());
#endif
    return itemsHandler;
}

/** @returns true if agent checks passed */
template<typename item_t>
bool sortItemsHandlerFinish(items_handler_t<item_t> &itemsHandler) {
#if ITEMS_HANDLER_TYPE == ITEMS_HANDLER_AGENT_RECORDING
    safeDelete(itemsHandler.agent);
    itemsHandler.numberEncoder->flush();
    bool const checksPassed =
        itemsHandler.numberEncoder->getError() == tarsa::NumberCodec::NoError;
    safeDelete(itemsHandler.numberEncoder);
    safeDelete(itemsHandler.writer);
    return checksPassed;
#else
    safeDelete(itemsHandler.agent);
    return true;
#endif
}

template<typename item_t>
bool sortValidate(items_handler_t<item_t> const &itemsHandler) {
    item_t const * const work = itemsHandler.input;
    size_t const size = itemsHandler.size;
#if ITEMS_HANDLER_TYPE != ITEMS_HANDLER_REFERENCE
    item_t * reference;
    checkZero(posix_memalign((void**) &reference, 128, sizeof (item_t) * size));
    mwc64xFill(reference, size);
    std::sort(reference, reference + size);
#endif
    bool valid = true;
    for (size_t i = 0; valid && (i < size); i++) {
        valid &= (i == 0) || work[i - 1] <= work[i];
#if ITEMS_HANDLER_TYPE != ITEMS_HANDLER_REFERENCE
        valid &= work[i] == reference[i];
#endif
    }
    return valid;
}

#endif // ITEMS_HANDLER_HPP
