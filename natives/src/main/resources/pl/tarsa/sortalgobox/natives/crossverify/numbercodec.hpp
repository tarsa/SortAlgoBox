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
#ifndef NUMBERCODEC_HPP
#define	NUMBERCODEC_HPP

#include <cstdint>

#include "buffered_io.hpp"

namespace tarsa {

    class NumberCodec {
    public:
        enum error_t {
            OK, NegativeValue, ValueOverflow, BufferedIoError
        };

    protected:
        error_t error;

    public:
        NumberCodec() {
            error = OK;
        }

        error_t getError() const {
            return error;
        }

        char const * showError() const {
            switch (error) {
                case OK: return "OK";
                case NegativeValue: return "NegativeValue";
                case ValueOverflow: return "ValueOverflow";
                case BufferedIoError: return "BufferedIoError";
                default: return nullptr;
            }
        }
    };

    class NumberEncoder : public NumberCodec {
        BufferedWriter * const writer;

    public:
        NumberEncoder(BufferedWriter * const writer): writer(writer) {
            error = (writer == nullptr) ? BufferedIoError : OK;
        }

        void flush() {
            if (!writer->flush(true)) {
                error = BufferedIoError;
            }
        }

        void serializeInt(int32_t const value) {
            if (error == OK) {
                if (value < 0) {
                    error = NegativeValue;
                } else if (value <= INT8_MAX) {
                    if (!writer->write(value)) {
                        error = BufferedIoError;
                    }
                } else {
                    if (!writer->write((value & 127) - 128)) {
                        error = BufferedIoError;
                    }
                    serializeInt(value >> 7);
                }
            }
        }

        void serializeLong(int64_t const value) {
            if (error == OK) {
                if (value < 0) {
                    error = NegativeValue;
                } else if (value <= INT8_MAX) {
                    if (!writer->write(value)) {
                        error = BufferedIoError;
                    }
                } else {
                    if (!writer->write((value & 127) - 128)) {
                        error = BufferedIoError;
                    }
                    serializeLong(value >> 7);
                }
            }
        }
    };

    class NumberDecoder : public NumberCodec {
        BufferedReader * const reader;

    public:
        NumberDecoder(BufferedReader * const reader): reader(reader) {
            error = (reader == nullptr) ? BufferedIoError : OK;
        }

    private:
        int32_t deserializeInt0(int32_t const current, int32_t const shift) {
            if (error == OK) {
                int32_t const input = reader->read();
                if (input == -1) {
                    error = BufferedIoError;
                    return -1;
                } else {
                    if (input == 0) {
                        return current;
                    } else {
                        int32_t const chunk = input & 127;
                        if ((chunk != 0) && ((shift > 31)
                                || (chunk > (INT32_MAX >> shift)))) {
                            error = ValueOverflow;
                            return -1;
                        } else {
                            int32_t const next = (chunk << shift) + current;
                            if (input <= 127) {
                                return next;
                            } else {
                                return deserializeInt0(next, shift + 7);
                            }
                        }
                    }
                }
            }
        }

        int64_t deserializeLong0(int64_t const current, int32_t const shift) {
            if (error == OK) {
                int32_t const input = reader->read();
                if (input == -1) {
                    error = BufferedIoError;
                    return -1;
                } else {
                    if (input == 0) {
                        return current;
                    } else {
                        int64_t const chunk = input & 127;
                        if ((chunk != 0) && ((shift > 63)
                                || (chunk > (INT64_MAX >> shift)))) {
                            error = ValueOverflow;
                            return -1;
                        } else {
                            int64_t const next = (chunk << shift) + current;
                            if (input <= 127) {
                                return next;
                            } else {
                                return deserializeLong0(next, shift + 7);
                            }
                        }
                    }
                }
            }
        }

    public:
        int32_t deserializeInt() {
            return deserializeInt0(0, 0);
        }

        int64_t deserializeLong() {
            return deserializeLong0(0L, 0);
        }
    };
}

#endif	/* NUMBERCODEC_HPP */
