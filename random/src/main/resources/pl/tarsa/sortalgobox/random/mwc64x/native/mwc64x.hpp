#ifndef MWC64X_HPP
#define	MWC64X_HPP

#include <cstdint>

struct mwc64x_t {
    uint64_t state;
    uint64_t const A = 4294883355L;
    uint64_t const M = (A << 32) - 1;

    mwc64x_t(uint64_t const initialState = 1745055044494293084UL)
    : state(initialState) {
    }

    uint32_t operator()() {
        uint32_t result = (uint32_t) ((state >> 32) ^ (state & UINT32_MAX));
        uint32_t c = state >> 32;
        uint32_t x = state & UINT32_MAX;
        state = A * x + c;
        return result;
    }

    void skip(uint64_t distance) {
        uint64_t c = state >> 32;
        uint64_t x = state & UINT32_MAX;
        uint64_t m = modpow(A, distance, M);
        uint64_t v = mulmod(A * x + c, m, M);
        uint64_t x1 = v / A;
        uint64_t c1 = v % A;
        state = (c1 << 32) + x1;
    }

    uint64_t modpow(uint64_t base, uint64_t exp, uint64_t modulus) {
        base %= modulus;
        uint64_t result = 1;
        while (exp > 0) {
            if (exp & 1) {
                result = mulmod(result, base, modulus);
            }
            base = mulmod(base, base, modulus);
            exp >>= 1;
        }
        return result;
    }

    uint64_t mulmod(uint64_t a, uint64_t b, uint64_t m) {
        uint64_t res = 0;
        uint64_t temp_b;

        if (b >= m) {
            if (m > UINT64_MAX / 2u)
                b -= m;
            else
                b %= m;
        }

        while (a != 0) {
            if (a & 1) {
                /* Add b to res, modulo m, without overflow */
                /* Equiv to if (res + b >= m), without overflow */
                if (b >= m - res)
                    res -= m;
                res += b;
            }
            a >>= 1;

            /* Double b, modulo m */
            temp_b = b;
            /* Equiv to if (2 * b >= m), without overflow */
            if (b >= m - b)
                temp_b -= m;
            b += temp_b;
        }
        return res;
    }
};

#endif	/* MWC64X_HPP */
