package com.cleverchuk.mips.compiler.parser;

import com.cleverchuk.mips.simulator.cpu.Instruction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Node {
    private final Construct construct;

    private final NodeType nodeType;

    private Object value;

    private int line;


    protected final List<Node> children = new LinkedList<>();


    protected final List<Instruction> instructions = new ArrayList<>(100);

    public Node(Construct construct, NodeType nodeType, Object value, int line) {
        this.construct = construct;
        this.nodeType = nodeType;
        this.value = value;
        this.line = line;
    }

    public void addChild(Node node) {
        children.add(node);
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

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public static NodeBuilder builder() {
        return new NodeBuilder();
    }

    public static class NodeBuilder {
        private Construct construct;

        private NodeType nodeType;

        private Object value;

        private int line;

        public  NodeBuilder construct(Construct construct){
            this.construct = construct;
            return this;
        }

        public  NodeBuilder nodeType(NodeType nodeType){
            this.nodeType = nodeType;
            return this;
        }

        public  NodeBuilder value(Object value){
            this.value = value;
            return this;
        }

        public  NodeBuilder line(int line){
            this.line = line;
            return this;
        }

        public Node build(){
            return new Node(construct, nodeType, value, line);
        }
    }
}
