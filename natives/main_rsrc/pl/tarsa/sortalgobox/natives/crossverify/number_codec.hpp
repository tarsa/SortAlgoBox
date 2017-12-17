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
#ifndef NUMBER_CODEC_HPP
#define	NUMBER_CODEC_HPP

#include <cstdint>

#include "buffered_io.hpp"

namespace tarsa {

    class NumberCodec {
    public:
        enum error_t {
            NoError, ValueOverflow, BufferedIoError
        };

    protected:
        error_t error;

    public:
        NumberCodec() {
            error = NoError;
        }

        error_t getError() const {
            return error;
        }

        char const * showError() const {
            switch (error) {
                case NoError: return "NoError";
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
            error = (writer == nullptr) ? BufferedIoError : NoError;
        }

        void flush() {
            if (error == NoError && !writer->flush(true)) {
                error = BufferedIoError;
            }
        }

        void serializeBit(bool const value) {
            if (error == NoError) {
                int8_t const byte = value ? 1 : 0;
                if (!writer->write(byte)) {
                    error = BufferedIoError;
                }
            }
          }

        void serializeByte(int8_t const value) {
            if (error == NoError) {
                if (!writer->write(value)) {
                    error = BufferedIoError;
                }
            }
        }

        void serializeInt(int32_t const original) {
            uint32_t const abs = original < 0 ? -original : original;
            uint32_t const value = (abs << 1) - (original < 0);
            serializeInt0(value);
        }

        void serializeLong(int64_t const original) {
            uint64_t const abs = original < 0 ? -original : original;
            uint64_t const value = (abs << 1) - (original < 0);
            serializeLong0(value);
        }

    private:
        void serializeInt0(uint32_t const value) {
            if (error == NoError) {
                if ((value >> 7) == 0) {
                    if (!writer->write(value)) {
                        error = BufferedIoError;
                    }
                } else {
                    if (!writer->write((value & 127) - 128)) {
                        error = BufferedIoError;
                    }
                    serializeInt0(value >> 7);
                }
            }
        }

        void serializeLong0(uint64_t const value) {
            if (error == NoError) {
                if ((value >> 7) == 0) {
                    if (!writer->write(value)) {
                        error = BufferedIoError;
                    }
                } else {
                    if (!writer->write((value & 127) - 128)) {
                        error = BufferedIoError;
                    }
                    serializeLong0(value >> 7);
                }
            }
        }
    };

    class NumberDecoder : public NumberCodec {
        BufferedReader * const reader;

    public:
        NumberDecoder(BufferedReader * const reader): reader(reader) {
            error = (reader == nullptr) ? BufferedIoError : NoError;
        }

        bool deserializeBit() {
            if (error == NoError) {
                int32_t const input = reader->read();
                if (input == -1) {
                    error = BufferedIoError;
                    return false;
                } else if (input > 1) {
                    error = ValueOverflow;
                    return false;
                } else {
                    return input == 1;
                }
            }
        }

        int8_t deserializeByte() {
            if (error == NoError) {
                int32_t const input = reader->read();
                if (input == -1) {
                    error = BufferedIoError;
                    return -1;
                } else {
                    return input;
                }
            }
        }

        int32_t deserializeInt() {
            uint32_t const shifted = deserializeInt0(0u, 0);
            bool const signFlag = shifted & 1;
            int32_t const absValue = shifted >> 1;
            return signFlag ? ~absValue : absValue;
        }

        int64_t deserializeLong() {
            uint64_t const shifted = deserializeLong0(0uL, 0);
            bool const signFlag = shifted & 1;
            int64_t const absValue = shifted >> 1;
            return signFlag ? ~absValue : absValue;
        }

    private:
        uint32_t deserializeInt0(uint32_t const current, int32_t const shift) {
            if (error == NoError) {
                int32_t const input = reader->read();
                if (input == -1) {
                    error = BufferedIoError;
                    return 1;
                } else {
                    uint32_t const chunk = input & 127;
                    if (shift > 31 ||
                            (shift > 0 && chunk >= (1u << 32 - shift))) {
                        error = ValueOverflow;
                        return 1;
                    }
                    uint32_t const next = (chunk << shift) + current;
                    if (input <= 127) {
                        return next;
                    } else {
                        return deserializeInt0(next, shift + 7);
                    }
                }
            }
        }

        uint64_t deserializeLong0(uint64_t const current, int32_t const shift) {
            if (error == NoError) {
                int32_t const input = reader->read();
                if (input == -1) {
                    error = BufferedIoError;
                    return 1;
                } else {
                    uint64_t const chunk = input & 127;
                    if (shift > 63 ||
                            (shift > 0 && chunk >= (1uL << 64 - shift))) {
                        error = ValueOverflow;
                        return 1;
                    }
                    uint64_t const next = (chunk << shift) + current;
                    if (input <= 127) {
                        return next;
                    } else {
                        return deserializeLong0(next, shift + 7);
                    }
                }
            }
        }
    };
}

#endif	/* NUMBER_CODEC_HPP */
