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
#ifndef BUFFERED_IO_HPP
#define	BUFFERED_IO_HPP

#include <cstdint>
#include <cstdio>
#include <cstdlib>

namespace tarsa {

    class BufferedReader {
    protected:
        size_t const bufferSize;
        uint8_t * buffer;
        int32_t bufferPosition;
        int32_t bufferLimit;
        bool readEnded;

    public:
        BufferedReader(size_t const bufferSize): bufferSize(bufferSize) {
            bufferPosition = 0;
            bufferLimit = 0;
            readEnded = false;
            buffer = new uint8_t[bufferSize];
        }

        ~BufferedReader() {
            close();
        }

        void close() {
            if (buffer != nullptr) {
                delete [] buffer;
                buffer = nullptr;
            }
        }

        /** @return byte in range 0-255 or -1 in case of failure */
        int32_t read() {
            if (buffer == nullptr || readEnded) {
                return -1;
            } else if (bufferPosition < bufferLimit) {
                return buffer[bufferPosition++];
            } else {
                bufferPosition = 0;
                readEnded = !refillBuffer();
                return read();
            }
        }

    protected:
        /** @return true on success */
        virtual bool refillBuffer() = 0;
    };

    class BufferedFileReader : public BufferedReader {
        typedef BufferedReader super;

        FILE * const sourceFile;

    public:
        BufferedFileReader(FILE * const sourceFile, size_t const bufferSize) :
                sourceFile(sourceFile), super(bufferSize) {
        }

    private:
        /** @return true on success */
        bool refillBuffer() {
            if (buffer != nullptr) {
                bufferLimit = fread(buffer, 1, bufferSize, sourceFile);
            }
            return buffer != nullptr &&  bufferLimit != 0;
        }
    };

    class BufferedWriter {
    protected:
        size_t const bufferSize;
        uint8_t * buffer;
        int32_t bufferPosition;
        bool writeFailed;

        virtual ~BufferedWriter() = 0;

        /** @return true on success */
        virtual bool flush(bool const underlyingFlush) = 0;

    public:
        BufferedWriter(size_t const bufferSize): bufferSize(bufferSize) {
            buffer = new uint8_t[bufferSize];
            bufferPosition = 0;
            writeFailed = false;
        }

        /** @return true on success */
        bool write(int32_t const byte) {
            if (writeFailed) {
                return false;
            } else if (bufferPosition < bufferSize) {
                buffer[bufferPosition++] = byte;
                return true;
            } else {
                return flush(false) && write(byte);
            }
        }
    };

    BufferedWriter::~BufferedWriter() {}

    class BufferedFileWriter: public BufferedWriter {
        typedef BufferedWriter super;

        FILE * targetFile;
        std::string const targetFilename;
        bool const closeFile;

    protected:
        bool fileOpened;

    public:

        BufferedFileWriter(std::string const targetFilename,
                size_t const bufferSize, bool const closeFile = true):
                targetFilename(targetFilename), closeFile(closeFile),
                super(bufferSize) {
            fileOpened = false;
        }

        BufferedFileWriter(FILE * const targetFile, size_t const bufferSize,
                std::string const targetFilename, bool const closeFile = false):
                targetFile(targetFile), targetFilename(targetFilename),
                closeFile(closeFile), super(bufferSize) {
            fileOpened = true;
        }

        virtual ~BufferedFileWriter() {
            close();
        }

        void close() {
            flush(false);
            if (closeFile && fileOpened) {
                fclose(targetFile);
                fileOpened = false;
            }
            if (buffer != nullptr) {
                delete [] buffer;
                buffer = nullptr;
            }
        }

        /** @return true on success */
        bool flush(bool const flushFile) {
            bool success = true;
            if (buffer == nullptr) {
                success = false;
            } else if (bufferPosition > 0) {
                if (!fileOpened) {
                    targetFile = fopen(targetFilename.c_str(), "wb");
                    if (targetFile == NULL) {
                        fputs("Can't open output file.\n", stderr);
                        success = false;
                    } else {
                        fileOpened = true;
                    }
                }
                if (fileOpened && fwrite(buffer, 1, bufferPosition, targetFile)
                        != bufferPosition) {
                    fputs("Error while writing to output.\n", stderr);
                    success = false;
                }
                if (fileOpened && flushFile) {
                    fflush(targetFile);
                }
            }
            bufferPosition = 0;
            writeFailed |= !success;
            return success;
        }
    };
}

#endif /* BUFFERED_IO_HPP */
