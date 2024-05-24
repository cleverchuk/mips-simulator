/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

        private final String code;

        public Error(int line, String msg, String code) {
            this.line = line;
            this.msg = msg;
            this.code = code;
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

            private String code;

            public ErrorBuilder line(int line){
                this.line = line;
                return this;
            }

            public ErrorBuilder msg(String msg){
                this.msg = msg;
                return this;
            }

            public ErrorBuilder code(String code){
                this.code = code;
                return this;
            }

            public Error build(){
                return new Error(line, msg, code);
            }
        }
    }
}
