/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cleverchuk.mips.simulator.mem;

public interface Memory {

    /**
     * reads a single byte
     * @param offset which byte to read
     * @return byte read
     */
    int read(int offset);

    /**
     * reads a two bytes
     * @param offset where to start reading
     * @return bytes read
     */
    int readHalf(int offset);

    /**
     * reads four bytes
     * @param offset where to start reading
     * @return bytes read
     */
    int readWord(int offset);

    /**
     * reads eight bytes
     * @param offset where to start reading
     * @return bytes read
     */
    long readDWord(int offset);

    /**
     * stores one byte
     * @param bite byte to store
     * @param offset base address
     */
    void store(byte bite, int offset);


    /**
     * stores two bytes
     * @param half bytes to store
     * @param offset base address
     */
    void storeHalf(short half, int offset);

    /**
     * stores four bytes
     * @param word bytes to store
     * @param offset base address
     */
    void storeWord(int word, int offset);

    /**
     * stores eight bytes
     * @param Dword bytes to store
     * @param offset base address
     */
    void storeDword(long Dword, int offset);

    /**
     * increase the underlying backing store to size
     *
     * @param size size of backing store
     */
    void resize(int size);

    int getCapacity();
}
