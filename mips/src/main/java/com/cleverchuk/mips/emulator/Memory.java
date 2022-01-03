package com.cleverchuk.mips.emulator;

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
     * @param size size of backing store
     * @return size reserved
     */
    int resize(int size);
}
