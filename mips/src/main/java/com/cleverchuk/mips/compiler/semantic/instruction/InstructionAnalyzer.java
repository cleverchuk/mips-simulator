package com.cleverchuk.mips.compiler.semantic.instruction;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.semantic.Analyzer;
import com.cleverchuk.mips.simulator.Opcode;
import com.cleverchuk.mips.simulator.fpu.FpuOpcode;
import javax.inject.Inject;


public class InstructionAnalyzer implements Analyzer {
    private final ZeroOpAnalyzer zeroOpAnalyzer;

    private final OneOpAnalyzer oneOpAnalyzer;

    private final TwoOpAnalyzer twoOpAnalyzer;

    private final ThreeOpAnalyzer threeOpAnalyzer;

    private final FourOpAnalyzer fourOpAnalyzer;

    @Inject
    public InstructionAnalyzer(ZeroOpAnalyzer zeroOpAnalyzer, OneOpAnalyzer oneOpAnalyzer,
            TwoOpAnalyzer twoOpAnalyzer, ThreeOpAnalyzer threeOpAnalyzer, FourOpAnalyzer fourOpAnalyzer) {
        this.zeroOpAnalyzer = zeroOpAnalyzer;
        this.oneOpAnalyzer = oneOpAnalyzer;
        this.twoOpAnalyzer = twoOpAnalyzer;
        this.threeOpAnalyzer = threeOpAnalyzer;
        this.fourOpAnalyzer = fourOpAnalyzer;
    }

    @Override
    public boolean analyze(Node instructionNode) {
        Node opcodeKind = instructionNode.getChildren().get(0);
        Construct construct = opcodeKind.getConstruct();

        Opcode parse = parse((String) opcodeKind.getChildren().get(0).getValue());
        if (parse instanceof FpuOpcode) {
            return true;
        }

        switch (construct) {
            case ZEROOP:
                return zeroOpAnalyzer.analyze(opcodeKind);
            case ONEOP:
                return oneOpAnalyzer.analyze(opcodeKind);
            case TWOOP:
                return twoOpAnalyzer.analyze(opcodeKind);
            case THREEOP:
                return threeOpAnalyzer.analyze(opcodeKind);
            case FOUROP:
                return fourOpAnalyzer.analyze(opcodeKind);
            default:
                return false;

        }
    }
}
