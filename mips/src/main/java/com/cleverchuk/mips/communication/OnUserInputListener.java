package com.cleverchuk.mips.communication;

public interface OnUserInputListener<T> {
    void onInputComplete(T data);
}
