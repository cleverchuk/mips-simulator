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