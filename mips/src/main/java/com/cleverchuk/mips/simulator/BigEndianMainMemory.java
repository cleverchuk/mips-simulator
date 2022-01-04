package com.cleverchuk.mips.simulator;

public class BigEndianMainMemory implements Memory {
    private byte[] backingStore;

    private final static int factor = 2;

    public BigEndianMainMemory(int capacity) {
        this.backingStore = new byte[capacity];
    }

    @Override
    public int read(int offset) {
        offset = offset % backingStore.length;
        return backingStore[offset];
    }

    @Override
    public int readHalf(int offset) {
        offset = offset % backingStore.length;
        short out = 0x0;
        out |= backingStore[offset];
        out <<= 0x8;
        out |= backingStore[offset + 1];
        return out;
    }

    @Override
    public int readWord(int offset) {
        offset = offset % backingStore.length;
        int out = readHalf(offset);
        out = (out << 0x10) | readHalf(offset + 2);
        return out;
    }

    @Override
    public long readDWord(int offset) {
        offset = offset % backingStore.length;
        long out = readWord(offset);
        out = (out << 0x20) | readHalf(offset + 4);
        return out;
    }

    @Override
    public void store(byte bite, int offset) {
        ensureCap(offset);
        backingStore[offset] = bite;
    }

    @Override
    public void storeHalf(short half, int offset) {
        ensureCap(offset + 2);
        backingStore[offset] = (byte) ((half >> 0x8) & 0xff);
        backingStore[offset + 1] = (byte) (half & 0xff);
    }

    @Override
    public void storeWord(int word, int offset) {
        ensureCap(offset + 4);
        backingStore[offset] = (byte) ((word >> 0x18) & 0xff);
        backingStore[offset + 1] = (byte) ((word >> 0x10) & 0xff);
        backingStore[offset + 2] = (byte) ((word >> 0x8) & 0xff);
        backingStore[offset + 3] = (byte) (word & 0xff);
    }

    @Override
    public void storeDword(long Dword, int offset) {
        ensureCap(offset + 8);
        backingStore[offset] = (byte) ((Dword >> 0x38) & 0xff);
        backingStore[offset + 1] = (byte) ((Dword >> 0x30) & 0xff);
        backingStore[offset + 2] = (byte) ((Dword >> 0x28) & 0xff);
        backingStore[offset + 3] = (byte) ((Dword >> 0x22) & 0xff);
        backingStore[offset + 4] = (byte) ((Dword >> 0x18) & 0xff);
        backingStore[offset + 5] = (byte) ((Dword >> 0x10) & 0xff);
        backingStore[offset + 6] = (byte) ((Dword >> 0x8) & 0xff);
        backingStore[offset + 7] = (byte) (Dword & 0xff);
    }

    @Override
    public int resize(int size) {
        byte[] temp = new byte[size];
        System.arraycopy(backingStore, 0, temp, 0, backingStore.length);
        backingStore = temp;
        return size;
    }

    private void ensureCap(int offset) {
        if (offset >= backingStore.length) {
            resize(factor * backingStore.length);
        }
    }
}
