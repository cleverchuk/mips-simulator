package com.cleverchuk.mips.emulator.storage;
@Deprecated
public class HalfStorage extends Storage {
    private final short[] halfWords;

    public HalfStorage(Object data, StorageType storageType) {
        super(data, storageType);
        halfWords = (short[]) data;
    }

    @Override
    public void store(Object data, int pos) {
        halfWords[pos] = (short) data;
    }

    @Override
    public Object read(int pos) {
        return halfWords[pos];
    }
}
