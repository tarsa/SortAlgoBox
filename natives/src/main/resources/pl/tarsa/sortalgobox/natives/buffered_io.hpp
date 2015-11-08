/**
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
#ifndef BUFFERED_IO_HPP
#define	BUFFERED_IO_HPP

#include <cstdint>
#include <cstdio>
#include <cstdlib>

namespace tarsa {

    class BufferedReader {
        FILE * const sourceFile;
        size_t const bufferSize;
        uint8_t * buffer;
        int32_t bufferPosition;
        int32_t bufferLimit;
        bool readEnded;

    public:

        BufferedReader(FILE * const sourceFile, size_t const bufferSize) :
                sourceFile(sourceFile), bufferSize(bufferSize) {
            bufferPosition = 0;
            bufferLimit = 0;
            readEnded = false;
            buffer = new uint8_t[bufferSize];
        }

        ~BufferedReader() {
            delete [] buffer;
            buffer = nullptr;
        }

        int32_t read() {
            if (bufferPosition < bufferLimit) {
                return buffer[bufferPosition++];
            } else if (readEnded) {
                return -1;
            } else {
                bufferPosition = 0;
                bufferLimit = fread(buffer, 1, bufferSize, sourceFile);
                if (bufferLimit == 0) {
                    readEnded = true;
                }
                return read();
            }
        }
    };

    class BufferedWriter {
        FILE * targetFile;
        size_t const bufferSize;
        uint8_t * buffer;
        int32_t bufferPosition;
        int32_t bufferLimit;
        bool fileOpened;
        std::string const targetFilename;

    public:

        BufferedWriter(std::string const targetFilename,
                size_t const bufferSize) : targetFilename(targetFilename),
                bufferSize(bufferSize) {
            bufferPosition = 0;
            bufferLimit = bufferSize;
            fileOpened = false;
            buffer = new uint8_t[bufferSize];
        }

        BufferedWriter(FILE * const targetFile,
                size_t const bufferSize, std::string const targetFilename) :
                targetFile(targetFile), targetFilename(targetFilename),
                bufferSize(bufferSize) {
            bufferPosition = 0;
            bufferLimit = bufferSize;
            fileOpened = true;
            buffer = new uint8_t[bufferSize];
        }

        ~BufferedWriter() {
            delete [] buffer;
            buffer = nullptr;
        }

        void flush(bool const flushFile = false) {
            if (bufferPosition > 0) {
                if (!fileOpened) {
                    targetFile = fopen(targetFilename.c_str(), "wb");
                    if (targetFile == NULL) {
                        fputs("Can't open output file.\n", stderr);
                        exit(EXIT_FAILURE);
                    }
                    fileOpened = true;
                }
                if (fwrite(buffer, 1, bufferPosition, targetFile)
                        != bufferPosition) {
                    fputs("Error while writing to output.\n", stderr);
                    exit(EXIT_FAILURE);
                }
                if (flushFile) {
                    fflush(targetFile);
                }
            }
            bufferPosition = 0;
        }

        void write(int32_t const byte) {
            if (bufferPosition < bufferLimit) {
                buffer[bufferPosition++] = byte;
            } else {
                flush();
                write(byte);
            }
        }
    };
}

#endif /* BUFFERED_IO_HPP */
