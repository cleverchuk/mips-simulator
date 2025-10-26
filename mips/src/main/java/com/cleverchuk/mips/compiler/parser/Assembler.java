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

import static com.cleverchuk.mips.compiler.lexer.MipsLexer.CPU_REG_TO_DECI;
import static com.cleverchuk.mips.compiler.lexer.MipsLexer.FPU_REG_TO_DECI;

import com.cleverchuk.mips.simulator.binary.Opcode;
import com.cleverchuk.mips.simulator.mem.BigEndianMainMemory;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Assembler implements NodeVisitor {
  private int dataOffset = -1;

  private int textOffset = -1;

  private int index = 0;

  private final Memory layout = new BigEndianMainMemory(1024);

  private final Map<String, Integer> symbolTable = new HashMap<>();

  public static Map<String, Opcode> opcodesMap =
      Arrays.stream(Opcode.values())
          .collect(Collectors.toMap(opcode -> opcode.name, Function.identity()));

  public static Map<String, Integer> registers =
      new HashMap<String, Integer>() {
        {
          putAll(FPU_REG_TO_DECI);
          putAll(CPU_REG_TO_DECI);
        }
      };

  private Opcode opcode;

  private int currentOpcode = -1;

  private int currentRs = 0;

  private int currentRt = 0;

  private int currentRd = 0;

  private int currentImme = 0;

  private int currentOffset = 0;

  private int currentShiftAmt = 0;

  private String currentDataMode = "";

  private byte regBitfield = 0; // rt = 001, 1, rs = 010, 2, rd = 100, 4

  @Override
  public void visit(Node node) {}

  @Override
  public void visitTextSegment(Node text) {
    textOffset = index;
    currentDataMode = "";
  }

  @Override
  public void visitLabel(Node node) {
    Node leftLeaf = getLeftLeaf(node);
    String label = leftLeaf.getValue().toString();
    if (symbolTable.containsKey(label)) {
      ErrorRecorder.recordError(
          ErrorRecorder.Error.builder()
              .msg("Reused of label : " + label)
              .line(leftLeaf.getLine())
              .build());

    } else {
      symbolTable.put(label, index);
    }
  }

  @Override
  public void visitOpcode(Node node) {
    if (currentOpcode >= 0) {
      flushEncoding(opcode);
    }

    currentRd = currentImme = currentOffset = currentRs = currentRt = currentShiftAmt = 0;
    String opcodeName = node.getValue().toString();
    opcode = Objects.requireNonNull(opcodesMap.get(opcodeName));
    currentOpcode = opcode.opcode;
  }

  @Override
  public void visitReg(Node reg) {
    String registerName = reg.getValue().toString();
    int regNum = Objects.requireNonNull(registers.get(registerName));
    if (opcode.rd && (regBitfield & 4) == 0) {
      currentRd = regNum;
      regBitfield |= 4;
    } else if (opcode.rs && (regBitfield & 2) == 0) {
      currentRs = regNum;
      regBitfield |= 2;
    } else if (opcode.rt && (regBitfield & 1) == 0) {
      currentRt = regNum;
      regBitfield |= 1;
    }
  }

  @Override
  public void visitBaseRegister(Node register) {
    String registerName = getLeftLeaf(register).getValue().toString();
    if (opcode.rs) {
      currentRs = Objects.requireNonNull(registers.get(registerName));
    } else if (opcode.rd) {
      currentRd = Objects.requireNonNull(registers.get(registerName));
    }
  }

  @Override
  public void visitExpression(Node expr) {
    Stack<String> ops = new Stack<>();
    Stack<Integer> operands = new Stack<>();

    exprEval(expr, ops, operands);
    if (currentDataMode.isEmpty()) {
      currentImme = currentOffset = operands.pop();
    }
  }

  @Override
  public void visitDataSegment(Node data) {
    dataOffset = index;
  }

  @Override
  public void visitDataMode(Node data) {
    currentDataMode = data.getValue().toString();
  }

  private void flushEncoding(Opcode opcode) {
    int encoding;
    switch (opcode) {
      case SDC1:
      case SDC2:
        encoding =
            opcode.partialEncoding
                | currentOpcode << 26
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentShiftAmt << 6
                | currentImme & 0xffff
                | currentOffset & 0x7ff;
        break;
      default:
        encoding =
            opcode.partialEncoding
                | currentOpcode << 26
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentShiftAmt << 6
                | currentImme & 0xffff
                | currentOffset & 0xffff;
    }

    layout.storeWord(encoding, index);
    index += 4;
    currentOpcode = 0;
  }

  public int getDataOffset() {
    return dataOffset;
  }

  public int getTextOffset() {
    return textOffset;
  }

  public Memory getLayout() {
    return layout;
  }

  public Map<String, Integer> getSymbolTable() {
    return Collections.unmodifiableMap(symbolTable);
  }

  private void exprEval(Node root, Stack<String> ops, Stack<Integer> operands) {
    for (Node child : root.getChildren()) {
      exprEval(child, ops, operands);
    }

    if (root.getNodeType() == NodeType.TERMINAL) {
      Object value = root.getValue();
      if (isOp(value.toString())) {
        ops.push(value.toString());
      } else {
        operands.push(((int) value));
      }
    } else {
      switch (root.getConstruct()) {
        case TERM:
        case EXPR:
          exprEval(ops, operands);
          break;
      }
    }
  }

  private void exprEval(Stack<String> ops, Stack<Integer> operands) {
    if (!ops.empty() && !operands.empty()) {
      String op = ops.pop();
      int r = operands.pop();
      int l = operands.pop();

      if (Objects.equals(op, "+")) {
        operands.push(l + r);
      } else if (Objects.equals(op, "-")) {
        operands.push(l - r);
      } else if (Objects.equals(op, "*")) {
        operands.push(l * r);
      } else {
        operands.push(l / r);
      }
    }
  }

  private boolean isOp(String value) {
    return Objects.equals(value, "+")
        || Objects.equals(value, "-")
        || Objects.equals(value, "*")
        || Objects.equals(value, "/");
  }

  private Node getLeftLeaf(Node root) {
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
}
