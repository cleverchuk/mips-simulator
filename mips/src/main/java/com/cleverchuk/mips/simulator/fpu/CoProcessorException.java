package com.cleverchuk.mips.simulator.fpu;

import androidx.annotation.NonNull;
import java.util.Locale;

public class CoProcessorException extends Throwable {
    private final int code;

    public CoProcessorException(String message, int code) {
        super(message);
        this.code = code;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "{message: %s, code: %d}", getMessage(), code);
    }
}
