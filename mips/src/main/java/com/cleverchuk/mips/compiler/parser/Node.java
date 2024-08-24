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

import com.cleverchuk.mips.simulator.cpu.CpuInstruction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Node {
    private final Construct construct;

    private final NodeType nodeType;

    private Object value;

    private int line;


    protected final List<Node> children = new LinkedList<>();


    protected final List<CpuInstruction> cpuInstructions = new ArrayList<>(100);

    public Node(Construct construct, NodeType nodeType, Object value, int line) {
        this.construct = construct;
        this.nodeType = nodeType;
        this.value = value;
        this.line = line;
    }

    public Node addChild(Node node) {
        children.add(node);
        return this;
    }

    public void removeChild(Node node) {
        children.remove(node);
    }

    public Construct getConstruct() {
        return construct;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public List<Node> getChildren() {
        return children;
    }

    public List<CpuInstruction> getInstructions() {
        return cpuInstructions;
    }

    public static NodeBuilder builder() {
        return new NodeBuilder();
    }

    public static class NodeBuilder {
        private Construct construct;

        private NodeType nodeType;

        private Object value;

        private int line;

        public NodeBuilder construct(Construct construct) {
            this.construct = construct;
            return this;
        }

        public NodeBuilder nodeType(NodeType nodeType) {
            this.nodeType = nodeType;
            return this;
        }

        public NodeBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public NodeBuilder line(int line) {
            this.line = line;
            return this;
        }

        public Node build() {
            return new Node(construct, nodeType, value, line);
        }
    }
}
