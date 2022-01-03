package com.cleverchuk.mips.compiler.semantic.instruction;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.semantic.Analyzer;
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
