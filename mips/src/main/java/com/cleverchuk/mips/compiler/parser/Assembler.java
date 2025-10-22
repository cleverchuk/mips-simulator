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

import com.cleverchuk.mips.simulator.binary.Opcode;
import com.cleverchuk.mips.simulator.mem.BigEndianMainMemory;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cleverchuk.mips.compiler.lexer.MipsLexer.CPU_REG_TO_DECI;
import static com.cleverchuk.mips.compiler.lexer.MipsLexer.FPU_REG_TO_DECI;

public class Assembler implements NodeVisitor {
  private int dataOffset = -1;

  private int textOffset = -1;

  private int index = 0;

  private Memory layout = new BigEndianMainMemory(1024);

  private Map<String, Integer> symbolTable = new HashMap<>();

  public static Map<String, Opcode> opcodesMap =
      Arrays.stream(com.cleverchuk.mips.simulator.binary.Opcode.values()).collect(Collectors.toMap(opcode -> opcode.name, Function.identity()));

  public static Map<String, Integer> registers = new HashMap<String, Integer>() {{
    putAll(FPU_REG_TO_DECI);
    putAll(CPU_REG_TO_DECI);
  }};

  private String opcodeName = "null";

  private int currentOpcode = 0;

  private int currentBase = 0;

  private int currentRs = 0;

  private int currentRt = 0;

  private int currentRd = 0;

  private int currentImme = 0;

  private int currentOffset = 0;

  private int currentShiftAmt = 0;

  @Override
  public void visit(Node node) {

  }

  @Override
  public void visitTextSegment(Node text) {
    textOffset = index;
  }

  @Override
  public void visitLabel(Node node) {
    String label = node.getValue().toString();
    if (symbolTable.containsKey(label)) {
      ErrorRecorder.recordError(ErrorRecorder.Error.builder()
          .msg("Reused of label : " + label)
          .line(node.getLine())
          .build());

    } else {
      symbolTable.put(label, index);
    }
  }

  @Override
  public void visitOpcode(Node node) {
    if (currentOpcode != 0) {
      Opcode opcode = Objects.requireNonNull(opcodesMap.get(opcodeName));
      flushEncoding(opcode);
    }

    opcodeName = node.getValue().toString();
    Opcode opcode = Objects.requireNonNull(opcodesMap.get(opcodeName));
    currentOpcode = opcode.opcode;
    index++;
  }

  @Override
  public void visitReg(Node reg) {
    String registerName = reg.getValue().toString();

  }

  @Override
  public void visitBaseRegister(Node register) {
    Deque<Node> deque = new ArrayDeque<>();
    deque.add(register);
    Node root = register;
    while (!deque.isEmpty()) {
      root = deque.getFirst();
      for (Node child : root.children) {
        if (child != null) {
          deque.add(child);
        }
      }
    }


    String registerName = root.getValue().toString();
    currentBase = registers.get(registerName);
  }

  @Override
  public void visitExpression(Node expr) {

  }

  @Override
  public void visitConstant(Node number) {
  }

  @Override
  public void visitDataSegment(Node data) {
    dataOffset = index;
  }

  @Override
  public void visitDataMode(Node data) {
    // code
    index++;
  }

  private void flushEncoding(Opcode opcode) {
    int encoding =
        opcode.partialEncoding | currentOpcode << 26 | currentRs << 21 | currentBase << 21 | currentRt << 16 | currentRd << 11 | currentShiftAmt << 6
        | currentImme & 0xffff0000 | currentOffset & 0xfc000000;

    layout.storeWord(encoding, index);
    index += 4;
    currentOpcode = 0;
  }


}
