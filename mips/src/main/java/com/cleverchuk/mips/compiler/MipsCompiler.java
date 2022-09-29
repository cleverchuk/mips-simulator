package com.cleverchuk.mips.compiler;

import com.cleverchuk.mips.compiler.codegen.CodeGenerator;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.parser.SyntaxError;
import com.cleverchuk.mips.simulator.cpu.CpuInstruction;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

public final class MipsCompiler {
    private final RecursiveDescentParser parser;

    private final CodeGenerator codeGenerator;

    private int sourceHash = -1;

    @Inject
    public MipsCompiler(RecursiveDescentParser parser, CodeGenerator codeGenerator) {
        this.parser = parser;
        this.codeGenerator = codeGenerator;
    }

    public boolean compile(String source) {
        if (sourceHash == source.hashCode()) {
            if (ErrorRecorder.hasErrors()) {
                throw new SyntaxError(ErrorRecorder.printErrors());
            }
            return false;
        }

        sourceHash = source.hashCode();
        codeGenerator.flush();
        Node program = parser.parse(source);

        if (ErrorRecorder.hasErrors()) {
            throw new SyntaxError(ErrorRecorder.printErrors());
        }

        codeGenerator.generate(Objects.requireNonNull(program, "incorrect program"));
        if (ErrorRecorder.hasErrors()) {
            throw new SyntaxError(ErrorRecorder.printErrors());
        }

        return true;
    }

    public Memory getDataSegment() {
        return codeGenerator.getMemory();
    }

    public List<CpuInstruction> getTextSegment() {
        return codeGenerator.getInstructions();
    }

    public int dataSegmentOffset() {
        return codeGenerator.getDataSegmentOffset();
    }

    public int textSegmentOffset() {
        return codeGenerator.getTextSegmentOffset();
    }

    public int memBoundary() {
        return codeGenerator.getMemOffset();
    }
}
