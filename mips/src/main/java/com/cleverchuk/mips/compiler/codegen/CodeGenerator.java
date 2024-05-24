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

package com.cleverchuk.mips.compiler.codegen;

import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.parser.NodeType;
import com.cleverchuk.mips.compiler.parser.SymbolTable;
import com.cleverchuk.mips.simulator.VirtualInstruction;
import com.cleverchuk.mips.simulator.cpu.CpuInstruction;
import com.cleverchuk.mips.simulator.cpu.CpuOpcode;
import com.cleverchuk.mips.simulator.fpu.FpuInstruction;
import com.cleverchuk.mips.simulator.fpu.FpuOpcode;
import com.cleverchuk.mips.simulator.mem.Memory;
import com.cleverchuk.mips.simulator.mem.StorageType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import javax.inject.Inject;


public final class CodeGenerator {
    public static final char VALUE_DELIMITER = '#';

    private final Memory memory;

    private final List<VirtualInstruction> instructions = new ArrayList<>();

    private int dataSegmentOffset = -1;

    private int textSegmentOffset = -1;

    private int memOffset = 0;

    private boolean fpuInstruction;

    public static char getValueDelimiter() {
        return VALUE_DELIMITER;
    }

    public Memory getMemory() {
        return memory;
    }

    public List<VirtualInstruction> getInstructions() {
        return instructions;
    }

    public int getDataSegmentOffset() {
        return dataSegmentOffset;
    }

    public int getTextSegmentOffset() {
        return textSegmentOffset;
    }

    public int getMemOffset() {
        return memOffset;
    }

    @Inject
    public CodeGenerator(Memory memory) {
        this.memory = memory;
    }

    public void flush() {
        dataSegmentOffset = -1;
        textSegmentOffset = -1;
        memOffset = 0;
        instructions.clear();
    }

    /**
     * Transforms AST to simulator instructions. The operand are destination register(rd/fd), source register(rs/fs),
     * source register(rt/ft) then followed by any other operand for special opcodes
     *
     * @param root AST root
     */
    public void generate(Node root) {
        for (Node child : root.getChildren()) {
            generate(child);
            root.setValue(concatenate(root.getValue(), child.getValue()));
        }

        if (root.getNodeType() == NodeType.NONTERMINAL) {
            switch (root.getConstruct()) {
                case TERM:
                case EXPR:
                    root.setValue(evalExpr(root.getValue()));
                    break;

                case ZEROOP:
                    instructions.add(buildInstruction(root.getChildren().get(0).getLine(),
                            root.getChildren().get(0).getValue().toString(), null,
                            null, null, null));
                    break;

                case ONEOP:
                    instructions.add(buildInstruction(root.getChildren().get(0).getLine(),
                            root.getChildren().get(0).getValue().toString(),
                            root.getChildren().get(1).getValue().toString(), null,
                            null, null));
                    break;

                case TWOOP:
                    instructions.add(buildInstruction(root.getChildren().get(0).getLine(),
                            root.getChildren().get(0).getValue().toString(),
                            root.getChildren().get(1).getValue().toString(),
                            root.getChildren().get(2).getValue().toString(), null, null));
                    break;

                case THREEOP:
                    instructions.add(buildInstruction(root.getChildren().get(0).getLine(),
                            root.getChildren().get(0).getValue().toString(),
                            root.getChildren().get(1).getValue().toString(),
                            root.getChildren().get(2).getValue().toString(),
                            root.getChildren().get(3).getValue().toString(), null));
                    break;

                case FOUROP:
                    instructions.add(buildInstruction(root.getChildren().get(0).getLine(),
                            root.getChildren().get(0).getValue().toString(),
                            root.getChildren().get(1).getValue().toString(),
                            root.getChildren().get(2).getValue().toString(),
                            root.getChildren().get(3).getValue().toString(),
                            root.getChildren().get(4).getValue().toString()));
                    break;

                case DATADECL:
                    loadMemory(root.getValue().toString());
                    break;

                case INSTRUCTION:
                    if (textSegmentOffset < 0) {
                        textSegmentOffset = root.getLine();
                    }
                    break;

                case TEXTDECL:
                    Node label = root.getChildren().get(0);
                    if (label.getConstruct() == Construct.LABEL) {
                        SymbolTable.insert(label.getValue().toString(), instructions.size() - 1);
                    }
                    break;

                case TEXTSEG:
                    textSegmentOffset = root.getLine();
                    break;

                case DATASEG:
                    dataSegmentOffset = root.getLine();
                    break;
            }
        }
    }

    private Object concatenate(Object first, Object second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.toString() + VALUE_DELIMITER + second;
    }

    private Object evalExpr(Object expr) {
        String[] tokens = expr.toString().split("#");
        Stack<Integer> stack = new Stack<>();

        String token;
        int len = tokens.length - 1;
        boolean negate = false;
        for (int i = 0; i < len; i++) {
            token = tokens[i];
            char c = token.charAt(0);
            if (token.length() > 1) {
                c = 0;
            }
            if (i == 0 && c == '-') {
                negate = true;
            } else if (isOp(c)) {
                int l = stack.pop();
                int r;
                if (tokens[i + 1].charAt(0) == '-' && i + 2 <= len) {
                    i++;
                    r = -1 * Integer.parseInt(tokens[i + 1]);
                } else {
                    r = Integer.parseInt(tokens[i + 1]);
                }
                if (token.charAt(0) == '+') {
                    stack.push(l + r);
                } else if (token.charAt(0) == '-') {
                    stack.push(l - r);
                } else if (token.charAt(0) == '*') {
                    stack.push(l * r);
                } else {
                    stack.push(l / r);
                }
            } else {
                if (negate) {
                    negate = false;
                    stack.push(-1 * Integer.parseInt(token));
                } else {
                    stack.push(Integer.parseInt(token));
                }
            }
        }
        if (stack.isEmpty()) {
            return tokens[0];
        }
        return stack.pop();
    }

    private boolean isOp(char c) {
        return c == '-' || c == '+' || c == '*' || c == '/';
    }

    private VirtualInstruction buildInstruction(int line, String opcode, String operand0, String operand1, String operand2, String operand3) {
        CpuOpcode cpuOpcode = CpuOpcode.parse(opcode);
        if (cpuOpcode != null) {
            return buildCpuInstruction(line, opcode, operand0, operand1, operand2, operand3);
        }

        return buildFpuInstruction(line, opcode, operand0, operand1, operand2);
    }

    private VirtualInstruction buildFpuInstruction(int line, String opcode, String operand0, String operand1, String operand2) {
        FpuOpcode opCode = FpuOpcode.parse(opcode);
        FpuInstruction.FpuInstructionBuilder builder = FpuInstruction.builder()
                .line(line)
                .opcode(opCode);

        if (operand0 == null || operand1 == null) {
            throw new RuntimeException("Unknown instruction");
        }

        if (MipsLexer.isRegister(operand0)) {
            try {
                short offset = Short.parseShort(operand1);
                builder.ft("$" + operand0);
                builder.offset(offset);

            } catch (Exception ignore) {
                builder.fd("$" + operand0);
            }
        }

        if (MipsLexer.isRegister(operand1)) {
            builder.fs("$" + operand1);
        } else if (operand1.contains("#")) {
            String[] tokens = operand1.split("#");
            builder.offset(Integer.parseInt(tokens[0]));
            builder.fs("$" + tokens[1]);
        }

        if (MipsLexer.isRegister(operand2)) {
            builder.ft("$" + operand2);
        }

        FpuInstruction fpuInstruction = builder.build();
        if (Objects.equals(null, fpuInstruction.fd)  && Objects.equals(null, fpuInstruction.ft) && Objects.equals(null, fpuInstruction.fs)){
            ErrorRecorder.recordError(
                    ErrorRecorder.Error.builder()
                            .line(line)
                            .msg("Invalid fpu instruction")
                            .build()
            );
        }

        this.fpuInstruction = true;
        return fpuInstruction;
    }

    private CpuInstruction buildCpuInstruction(int line, String opcode, String operand0, String operand1, String operand2, String operand3) {
        CpuOpcode opCode = CpuOpcode.valueOf(opcode.toUpperCase());
        CpuInstruction.CpuInstructionBuilder builder = CpuInstruction.builder()
                .line(line)
                .opcode(opCode);

        if (operand0 == null) { // Zero operand opcode
            return builder
                    .build();

        } else if (operand1 == null) { // One operand opcode
            if (MipsLexer.isRegister(operand0)) {
                builder.rd("$" + operand0);
            } else {
                try {
                    builder.immediateValue(Integer.parseInt(operand0));
                } catch (Exception e) {
                    builder.label(operand0);
                }
            }

        } else if (operand2 == null) {// Two operand opcode
            if (MipsLexer.isRegister(operand0)) {
                builder.rd("$" + operand0);
            }

            if (MipsLexer.isRegister(operand1)) {
                builder.rs("$" + operand1);
            } else if (operand1.contains("#")) {
                String[] tokens = operand1.split("#");
                builder.offset(Integer.parseInt(tokens[0]));
                builder.rs("$" + tokens[1]);
            } else {
                try {
                    builder.immediateValue(Integer.parseInt(operand1));
                } catch (Exception e) {
                    builder.label(operand1);
                }
            }

        } else if (operand3 == null) { // Three operand opcode

            if (MipsLexer.isRegister(operand0)) {
                builder.rd("$" + operand0);
            }

            if (MipsLexer.isRegister(operand1)) {
                builder.rs("$" + operand1);
            }

            if (MipsLexer.isRegister(operand2)) {
                builder.rt("$" + operand2);
            } else if (operand2.contains("#")) {
                String[] tokens = operand2.split("#");
                builder.offset(Integer.parseInt(tokens[0]));
                builder.rt("$" + tokens[1]);

            } else {
                try {
                    builder.immediateValue(Integer.parseInt(operand2));
                } catch (Exception e) {
                    builder.label(operand2);
                }
            }

        } else { // Four operand opcode
            builder.rd("$" + operand0)
                    .rs("$" + operand1)
                    .pos(Integer.parseInt(operand2))
                    .size(Integer.parseInt(operand3));
        }

        CpuInstruction cpuInstruction = builder.build();
        if (!checkBitWidth(cpuInstruction)) {
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(cpuInstruction.line)
                    .msg("offset outside bit range")
                    .build());
        }
        return cpuInstruction;
    }

    private void loadMemory(String data) {
        String[] tokens = data.split("#");
        String label = tokens[0];
        String type = tokens[1].toUpperCase();

        SymbolTable.insert(label, memOffset);
        switch (StorageType.valueOf(type)) {
            case SPACE:
                memOffset += Integer.parseInt(tokens[2]);
                break;

            case WORD:
            case INT:
                for (int i = 2; i < tokens.length; i++, memOffset += 4) {
                    memory.storeWord(Integer.parseInt(tokens[i]), memOffset);
                }
                break;

            case BYTE:
            case CHAR:
                for (int i = 2; i < tokens.length; i++, memOffset++) {
                    memory.store(Byte.parseByte(tokens[i]), memOffset);
                }
                break;

            case HALF:
                for (int i = 2; i < tokens.length; i++, memOffset += 2) {
                    memory.storeHalf(Short.parseShort(tokens[i]), memOffset);
                }
                break;

            case ASCIIZ:
                writeASCII(tokens);
                memory.store((byte) 0, memOffset++);
                break;

            case ASCII:
                writeASCII(tokens);
                break;
            case FLOAT:
                for (int i = 2; i < tokens.length; i++, memOffset += 4) {
                    memory.storeWord(Float.floatToRawIntBits(Float.parseFloat(tokens[i])), memOffset);
                }
                break;
            case DOUBLE:
                for (int i = 2; i < tokens.length; i++, memOffset += 8) {
                    memory.storeDword(Double.doubleToRawLongBits(Double.parseDouble(tokens[i])), memOffset);
                }
        }
    }

    private void writeASCII(String[] tokens) {
        int start = tokens[2].indexOf('"') + 1, end = tokens[2].lastIndexOf('"');
        char[] temp = tokens[2].substring(start, end).toCharArray();
        for (int i = 0; i < temp.length; i++, memOffset++) {

            if (temp[i] == '\\' && i + 1 < temp.length && temp[i + 1] == 'n') {
                memory.store((byte) 10, memOffset);
                i++;
            } else {
                memory.store((byte) temp[i], memOffset);
            }
        }
    }

    private boolean checkBitWidth(CpuInstruction cpuInstruction) {
        switch (cpuInstruction.opcode) {
            default:
                return true;

            case ROTR:
            case SRL:
            case SRA:
            case SLL:
                return check5BitWidth(cpuInstruction);

            case ADDIU:
            case SLTIU:
                return Math.abs(cpuInstruction.immediateValue) <= 0xff_ff;

            case ADDI:
            case LUI:
            case ANDI:
            case ORI:
            case XORI:
            case SLTI:
                return check16BitConstant(cpuInstruction);

            case LB:
            case LBU:
            case LH:
            case LHU:
            case LW:
            case LWL:
            case LWR:
            case SB:
            case SH:
            case SW:
            case SWL:
            case SWR:
            case ULW:
            case USW:
            case LL:
            case SC:
                return check16BitOffset(cpuInstruction);

            case B:
            case BAL:
            case BEQ:
            case BEQZ:
            case BGEZ:
            case BGEZAL:
            case BGTZ:
            case BLEZ:
            case BLTZ:
            case BLTZAL:
            case BNEZ:
            case BNE:
                return check18BitOffset(cpuInstruction);
        }
    }

    private boolean check5BitWidth(CpuInstruction cpuInstruction) {
        if (cpuInstruction.immediateValue >= 0) {
            return cpuInstruction.immediateValue <= 0x1f;
        }
        return cpuInstruction.immediateValue >= -15;
    }

    private boolean check16BitOffset(CpuInstruction cpuInstruction) {
        if (cpuInstruction.offset >= 0) {
            return cpuInstruction.offset <= 0xff_ff;
        }

        return Short.MIN_VALUE <= cpuInstruction.offset;
    }


    private boolean check18BitOffset(CpuInstruction cpuInstruction) {
        if (cpuInstruction.immediateValue >= 0) {
            return cpuInstruction.immediateValue <= 131072;
        }

        return -131071 <= cpuInstruction.immediateValue;
    }

    private boolean check16BitConstant(CpuInstruction cpuInstruction) {
        return Short.MIN_VALUE <= cpuInstruction.immediateValue && cpuInstruction.immediateValue <= Short.MAX_VALUE;
    }

    public boolean hasFpuInstruction() {
        return fpuInstruction;
    }
}
