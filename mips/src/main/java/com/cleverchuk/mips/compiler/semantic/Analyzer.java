package com.cleverchuk.mips.compiler.semantic;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.simulator.Opcode;
import com.cleverchuk.mips.simulator.cpu.CpuOpcode;
import com.cleverchuk.mips.simulator.fpu.FpuOpcode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public interface Analyzer {
    boolean analyze(Node node);

    default Optional<Node> findNode(Node node, Construct construct) {
        Deque<Node> nodeDeque = new ArrayDeque<>();
        nodeDeque.push(node);

        do {
            int size = nodeDeque.size();
            for (int i = 0; i < size; i++) {
                Node root = nodeDeque.remove();
                if (root.getConstruct() == construct) {
                    return Optional.of(root);
                }

                for (Node child : root.getChildren()) {
                    nodeDeque.addLast(child);
                }
            }

        } while (!nodeDeque.isEmpty());

        return Optional.empty();
    }

    default Opcode parse(String value) {
        if (CpuOpcode.CPU_OPCODES.contains(value)) {
            return CpuOpcode.parse(value);
        }

        if (FpuOpcode.FPU_OPCODES.contains(value)) {
            return FpuOpcode.parse(value);
        }

        return null;
    }
}
