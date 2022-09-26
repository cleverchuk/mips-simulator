package com.cleverchuk.mips.simulator.cpu;

public interface RegisterFile {
    int readWord(String file);
    void writeWord(String file, int value);
    void accAdd(long value);
    void accSub(long value);
    void accSet(long value);
    void accSetHI(int value);
    void accSetLO(int value);
    int accLO();
    int accHI();
    String dump();
}
