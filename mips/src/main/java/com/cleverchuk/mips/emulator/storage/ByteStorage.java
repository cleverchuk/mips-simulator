package com.cleverchuk.mips.emulator.storage;

@Deprecated
public class ByteStorage extends Storage {
    private final byte[] bytes;

    public ByteStorage(Object data, StorageType storageType) {
        super(data, storageType);
        bytes = (byte[]) data;
    }

    @Override
    public void store(Object data, int pos) {
        bytes[pos] = (byte) data;
    }

    @Override
    public Object read(int pos) {
        return bytes[pos];
    }
}
