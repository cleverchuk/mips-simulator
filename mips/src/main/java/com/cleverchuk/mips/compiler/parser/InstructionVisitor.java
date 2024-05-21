/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cleverchuk.mips.compiler.parser;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.cleverchuk.mips.compiler.parser.NodeType.TERMINAL;


@Singleton
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
