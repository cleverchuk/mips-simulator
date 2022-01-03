package com.cleverchuk.mips.emulator.storage;
@Deprecated
public class SpaceStorage extends Storage {
    private final byte[] space;

    public SpaceStorage(Object data, StorageType storageType) {
        super(data, storageType);
        space = (byte[]) data;
    }

    @Override
    public void store(Object data, int pos) {
        space[pos] = (byte) data;
    }

    public void storeInt(int data, int pos) {
        if (pos + 3 >= space.length) {
            throw new IndexOutOfBoundsException();
        }
        space[pos + 3] = (byte) (data & 0xff);
        space[pos + 2] = (byte) ((data >> 8) & 0xff);
        space[pos + 1] = (byte) ((data >> 8) & 0xff);
        space[pos] = (byte) ((data >> 8) & 0xff);
    }

    @Override
    public Object read(int pos) {
        return space[pos];
    }

    public int readInt(int pos) {
        if (pos + 3 >= space.length) {
            throw new IndexOutOfBoundsException();
        }
        int value = space[pos];
        value = (value << 8) | space[++pos];
        value = (value << 8) | space[++pos];
        value = (value << 8) | space[++pos];
        return value;
    }
}
