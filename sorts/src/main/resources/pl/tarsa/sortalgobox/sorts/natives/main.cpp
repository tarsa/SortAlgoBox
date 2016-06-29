/*
 * Copyright (C) 2015 Piotr Tarsa ( http://github.com/tarsa )
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
 *
 */
#define NDEBUG

#include <algorithm>
#include <cassert>
#include <chrono>
#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <utility>

#include "macros.hpp"
#include "utilities.hpp"

#include "mwc64x.hpp"

#define EMPTY_AUXILIARY_SPACE struct auxiliary_space_t { \
}; \
\
auxiliary_space_t sortInitAuxiliary(size_t const size) { \
    auxiliary_space_t auxiliary; \
    return auxiliary; \
}


#include xstr(SORT_MECHANICS)

#ifndef VALIDATE_FUNCTION
#define VALIDATE_FUNCTION

bool sortValidate(int32_t const * const work, size_t const size,
        auxiliary_space_t const * const auxiliary) {
    int32_t * reference;
    checkZero(posix_memalign((void**) &reference, 128,
            sizeof (int32_t) * size));
    mwc64xFill(reference, size);
    std::sort(reference, reference + size);
    bool valid = true;
    for (size_t i = 0; valid && (i < size); i++) {
        valid &= work[i] == reference[i];
    }
    return valid;
}

#endif // VALIDATE_FUNCTION

int main(int argc, char** argv) {
    bool shouldValidate;
    std::cin >> shouldValidate;
    size_t size;
    std::cin >> size;

    int32_t * work;
    checkZero(posix_memalign((void**) &work, 128, sizeof (int32_t) * size));
    auxiliary_space_t auxiliary = sortInitAuxiliary(size);

    mwc64xFill(work, size);

    auto startingChrono = std::chrono::system_clock::now();
    sortPerform(work, size, &auxiliary);
    auto elapsedChrono = std::chrono::system_clock::now() - startingChrono;
    uint64_t elapsedChronoNanoseconds = std::chrono::duration_cast<std::chrono
        ::nanoseconds>(elapsedChrono).count();
    printf("%lx\n", elapsedChronoNanoseconds);

    if (shouldValidate) {
        bool const valid = sortValidate(work, size, &auxiliary);
        std::cout << (valid ? "pass" : "fail") << std::endl;
    }

    return EXIT_SUCCESS;
}
