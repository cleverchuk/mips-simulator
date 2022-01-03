package com.cleverchuk.mips.compiler.semantic;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.Node;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public interface Analyzer {
    boolean analyze(Node node);

    default Optional<Node> findNode(Node node, Construct construct) {
        Deque<Node> nodeDeque = new ArrayDeque<>();
        nodeDeque.push(node);

        do {
            Node root = nodeDeque.pop();
            if (root.getConstruct() == construct) {
                return Optional.of(root);
            }

            for (Node child : root.getChildren()) {
                nodeDeque.push(child);
            }

        } while (!nodeDeque.isEmpty());

        return Optional.empty();
    }
}
