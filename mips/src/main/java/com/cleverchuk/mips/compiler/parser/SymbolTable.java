package com.cleverchuk.mips.compiler.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SymbolTable {
    private static final Map<Object, Integer> table = new HashMap<>();

    public static void insert(Object id, int index) {
        table.put(id, index);
    }

    public static int lookup(Object id) {
        return Objects.requireNonNull(table.getOrDefault(id, -1));
    }

    public static void clear() {
        table.clear();
    }

    public static Map<Object, Integer> getTable(){
        return new HashMap<>(table);
    }
}
