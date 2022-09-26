package com.cleverchuk.mips.compiler.semantic.instruction;

import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.semantic.Analyzer;
import com.cleverchuk.mips.simulator.cpu.CpuOpcode;
import java.util.List;
import javax.inject.Inject;

public class ZeroOpAnalyzer implements Analyzer {

    @Inject
    public ZeroOpAnalyzer() {
    }

    @Override
    public boolean analyze(Node opcodeKind) {
        /*listing of zero operand opcode mnemonics
         * nop
         * syscall
         * */
        List<Node> children = opcodeKind.getChildren();
        Node opcode = children.get(0);
        return children.size() == 1 && (CpuOpcode.NOP.same((String) opcode.getValue()) || CpuOpcode.SYSCALL.same((String) opcode.getValue()));
    }
}
