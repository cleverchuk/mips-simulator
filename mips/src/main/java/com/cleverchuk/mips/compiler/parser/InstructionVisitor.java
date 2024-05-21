package com.cleverchuk.mips.compiler.parser;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;

import static com.cleverchuk.mips.compiler.parser.NodeType.TERMINAL;


public class InstructionVisitor implements NodeVisitor {
    Set<String> instructions = new LinkedHashSet<>();

    int counter = 0;

    @Inject
    public InstructionVisitor() {
    }

    @Override
    public void visit(Node node) {
        if (node.getConstruct() == Construct.INSTRUCTION) {
            StringBuilder builder = new StringBuilder();
            counter = 0;
            buildInstruction(node, builder);
            instructions.add(builder.toString().trim());
        }
    }

    private void buildInstruction(Node node, StringBuilder builder) {
        if (node.getNodeType() == TERMINAL) {
            if (counter > 1 && builder.charAt(builder.length() - 2 /* 2 because of space on line 50*/) != '(') {
                builder.append(", ");
            }
            String  value = node.getValue().toString();
            if (value.equals("\n"))
                return;
            if (MipsLexer.isRegister(value)) {
                builder
                        .append("$")
                        .append(value);
            } else {
                builder.append(value);
            }

            builder.append(" ");
            counter++;
        } else {
            if (node.getConstruct() == Construct.PARENREG) {
                builder.append("( ");
            }

            for (Node n : node.getChildren()) {
                buildInstruction(n, builder);
            }

            if (node.getConstruct() == Construct.PARENREG) {
                builder.append(")");
            }
        }
    }

    public Set<String> getInstructions() {
        return instructions;
    }
}
