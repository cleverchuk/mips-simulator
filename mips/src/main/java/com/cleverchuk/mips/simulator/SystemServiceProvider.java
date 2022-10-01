package com.cleverchuk.mips.simulator;

public interface SystemServiceProvider {
    void requestService(int which) throws Exception;
}
