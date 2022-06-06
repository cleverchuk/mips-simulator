package com.cleverchuk.mips.compiler.parser;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Locale;

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


    public static class Error {
        private final int line;

        private final String msg;

        public Error(int line, String msg) {
            this.line = line;
            this.msg = msg;
        }

        public int getLine() {
            return line;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        @NonNull
        public String toString() {
            return String.format(Locale.getDefault(), "[%d] %s", line, msg);
        }

        public static ErrorBuilder builder(){
            return new ErrorBuilder();
        }

        public static class ErrorBuilder{
            private int line;

            private String msg;

            public ErrorBuilder line(int line){
                this.line = line;
                return this;
            }

            public ErrorBuilder msg(String msg){
                this.msg = msg;
                return this;
            }

            public Error build(){
                return new Error(line, msg);
            }
        }
    }
}
