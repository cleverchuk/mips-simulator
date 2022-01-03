package com.cleverchuk.mips.compiler.semantic;

import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import javax.inject.Inject;

public class SemanticAnalyzer implements Analyzer {
    private final InstructionAnalyzer instructionAnalyzer;

    @Inject
    public SemanticAnalyzer(InstructionAnalyzer instructionAnalyzer) {
        this.instructionAnalyzer = instructionAnalyzer;
    }

    @Override
    public boolean analyze(Node node) {
        return instructionAnalyzer.analyze(node);
    }
}
