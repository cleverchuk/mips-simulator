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
 
package com.cleverchuk.mips.simulator;

public enum SystemService {
    PRINT_INT(1),
    PRINT_FLOAT(2),
    PRINT_DOUBLE(3),
    PRINT_STRING(4),
    PRINT_CHAR(11),
    READ_INT(5),
    READ_FLOAT(6),
    READ_DOUBLE(7),
    READ_STRING(8),
    READ_CHAR(12),
    HALT(10),
    DEBUG(100),
    PAUSE(101),
    ;

    final int code;

    SystemService(int code) {
        this.code = code;
    }

    public static SystemService parse(int code) {
        switch (code) {
            case 1:
                return PRINT_INT;
            case 2:
                return PRINT_FLOAT;
            case 3:
                return PRINT_DOUBLE;
            case 4:
                return PRINT_STRING;
            case 5:
                return READ_INT;
            case 6:
                return READ_FLOAT;
            case 7:
                return READ_DOUBLE;
            case 8:
                return READ_STRING;
            case 10:
                return HALT;
            case 11:
                return PRINT_CHAR;
            case 12:
                return READ_CHAR;
            case 100:
                return DEBUG;
            case 101:
                return PAUSE;
            default:
                throw new RuntimeException();
        }
    }
}