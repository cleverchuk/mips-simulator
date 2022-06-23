package com.cleverchuk.mips.simulator.storage;


/**
 * Author: CleverChuk
 * Company: CleverCorp Inc
 * Date: 11/26/2019.
 */

@Deprecated
public class Storage {
    public Object data;

    public final StorageType storageType;

    public Storage(Object data, StorageType storageType) {
        this.data = data;
        this.storageType = storageType;
    }

    public void store(Object data, int pos) {
    }

    public Object read(int pos) {
        return null;
    }

}
