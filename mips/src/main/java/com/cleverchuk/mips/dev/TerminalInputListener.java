package com.cleverchuk.mips.dev;

public interface TerminalInputListener {
    default void onIntInput(int data) {
    }

    default void onDoubleInput(double data) {
    }

    default void onFloatInput(float data) {
    }

    default void onStringInput(String data) {
    }

    default void onCharInput(char data) {
    }
}
