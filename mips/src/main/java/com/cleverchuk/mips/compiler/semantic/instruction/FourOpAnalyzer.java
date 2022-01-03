package com.cleverchuk.mips.compiler.semantic.instruction;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.semantic.Analyzer;
import com.cleverchuk.mips.emulator.Opcode;
import java.util.List;
import javax.inject.Inject;

public class FourOpAnalyzer implements Analyzer {

    @Inject
    public FourOpAnalyzer() {
    }

    @Override
    public boolean analyze(Node opcodeKind) {
        List<Node> children = opcodeKind.getChildren();
        Node opcode = children.get(0);
        return children.size() == 5 &&
                (Opcode.EXT.same((String)opcode.getValue()) || Opcode.INS.same((String)opcode.getValue())) &&
                Construct.REGISTER == children.get(1).getConstruct() &&
                Construct.REGISTER == children.get(2).getConstruct() &&
                Construct.CONSTANT == children.get(3).getConstruct()&&
                Construct.CONSTANT == children.get(4).getConstruct();
    }
}
