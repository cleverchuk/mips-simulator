package com.cleverchuk.mips.compiler.parser;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Locale;
import lombok.Builder;
import lombok.Data;

public class ErrorRecorder {
    private static final ArrayList<Error> ERRORS = new ArrayList<>();

    public static void recordError(Error error) {
        ERRORS.add(error);
      }

    public static String printErrors() {
        StringBuilder builder = new StringBuilder("Errors:\n");
        for (Error error : ERRORS) {
            builder.append(error).append("\n");
        }
        return builder.toString();
    }

    public static void clear() {
        ERRORS.clear();
    }

    public static boolean hasErrors() {
        return !ERRORS.isEmpty();
    }

    @Data
    @Builder
    public static class Error {
        private int line;

        private String msg;

        @Override
        @NonNull
        public String toString() {
            return String.format(Locale.getDefault(), "[%d] %s", line, msg);
        }
    }
}
