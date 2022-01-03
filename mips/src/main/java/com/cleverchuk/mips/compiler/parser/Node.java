package com.cleverchuk.mips.compiler.parser;

import com.cleverchuk.mips.emulator.Instruction;
import com.cleverchuk.mips.emulator.storage.Storage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Node {
    private Construct construct;

    private NodeType nodeType;

    private Object value;

    private int line;

    @Builder.Default
    protected final List<Node> children = new LinkedList<>();

    @Builder.Default
    protected final List<Instruction> instructions = new ArrayList<>(100);

    @Builder.Default
    protected final List<Storage> data = new ArrayList<>(100);

    public void addChild(Node node){
        children.add(node);
    }
}
