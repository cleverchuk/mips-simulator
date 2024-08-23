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

import com.cleverchuk.mips.simulator.cpu.CpuOpcode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.cleverchuk.mips.compiler.parser.NodeType.NONTERMINAL;
import static com.cleverchuk.mips.compiler.parser.NodeType.TERMINAL;

public final class PseudoTransformer implements NodeVisitor {
    @Override
    public void visit(Node node) {
        if (node.getConstruct() == Construct.INSTRUCTION) {
            transformLW(node);
        }
    }


    public Node getLeaf(Node root) {
        Deque<Node> nodes = new ArrayDeque<>();
        nodes.add(root);
        Node leaf = null;
        while (!nodes.isEmpty()) {
            root = nodes.removeFirst();
            if (root.getNodeType() == NodeType.TERMINAL) {
                leaf = root;
                break;
            }
            nodes.addAll(root.getChildren());
        }

        return leaf;
    }

    private void transformLW(Node instruction) {
        Node twoOp = instruction.getChildren().get(0);
        if (twoOp.getConstruct() == Construct.TWOOP) {
            List<Node> children = twoOp.getChildren();
            String opcode = (String) children.get(0).getValue();
            Node label = children.get(2).getChildren().get(0);


            if ("lw".equals(opcode) && label.getNodeType() == TERMINAL) {
                instruction.removeChild(twoOp);

                // create la node
                Node la = Node.builder()
                        .construct(Construct.TWOOP)
                        .nodeType(NONTERMINAL)
                        .build();

                la.addChild(Node.builder()
                        .construct(Construct.OPCODE)
                        .nodeType(TERMINAL)
                        .value(CpuOpcode.LA.getName())
                        .build());

                la.addChild(children.get(1));
                la.addChild(children.get(2));

                // create lw node
                Node lw = Node.builder()
                        .construct(Construct.TWOOP)
                        .nodeType(NONTERMINAL)
                        .build();

                lw.addChild(Node.builder()
                        .construct(Construct.OPCODE)
                        .nodeType(TERMINAL)
                        .value(CpuOpcode.LW.getName())
                        .line(instruction.getLine())
                        .build());

                lw.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .value(getLeaf(children.get(1)).getValue())
                        .build());

                Node operand = Node.builder()
                        .construct(Construct.OPERAND)
                        .nodeType(NONTERMINAL)
                        .build();

                operand.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .value(0)
                        .line(twoOp.getLine())
                        .build());

                operand.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .value(getLeaf(children.get(1)).getValue())
                        .build());
                lw.addChild(operand);

                instruction.addChild(la);
                instruction.addChild(lw);
            }
        }
    }
}
