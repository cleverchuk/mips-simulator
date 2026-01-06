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

import static com.cleverchuk.mips.compiler.lexer.MipsLexer.CPU_REG_TO_DECI;
import static com.cleverchuk.mips.compiler.lexer.MipsLexer.FPU_REG_TO_DECI;

import com.cleverchuk.mips.compiler.parser.Construct;
import com.cleverchuk.mips.compiler.parser.ErrorRecorder;
import com.cleverchuk.mips.compiler.parser.Node;
import com.cleverchuk.mips.compiler.parser.NodeType;
import com.cleverchuk.mips.compiler.parser.NodeVisitor;
import com.cleverchuk.mips.simulator.binary.InstructionIR;
import com.cleverchuk.mips.simulator.binary.Opcode;
import com.cleverchuk.mips.simulator.mem.BigEndianMainMemory;
import com.cleverchuk.mips.simulator.mem.Memory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Assembler implements NodeVisitor {
  private int dataOffset = -1;

  private int textOffset = -1;

  private int index = 0;

  private int stackPointer = 0;

  private int textBoundary = 0;

  private final Memory layout = new BigEndianMainMemory(1024);

  private final Map<String, Integer> symbolTable = new HashMap<>();

  private final List<InstructionIR> irs = new ArrayList<>();

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

  private String currentDataMode = "";

  private byte regBitfield = 0; // rt = 001, 1, rs = 010, 2, rd = 100, 4

  private byte posBitfield = 0; // pos = 001, 1, size = 010, 2,

  private byte opBitfield = 0; // op = 001, 1

  private InstructionIR.Builder irBuilder = null;

  private int sourceOffset = 0;

  private byte cLayout = 0; // inst = 01, data = 10

  private boolean laSeen = false;

  private ArrayDeque<EncodingEmitObserver> observers = new ArrayDeque<>();

  @Override
  public void visitTextSegment(Node text) {
    textOffset = index;
    currentDataMode = "";
    sourceOffset = text.getLine();
    cLayout = 1;
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
      if ((cLayout & 1 /*instruction*/) > 0) {
        symbolTable.put(label, textOffset + (leftLeaf.getLine() - sourceOffset - 1) * 4);
        if (laSeen) {
          // LA produces additional instruction
          symbolTable.put(label, textOffset + (leftLeaf.getLine() - sourceOffset) * 4);
        }
      }

      if ((cLayout & 2 /*data*/) > 0) {
        symbolTable.put(label, index);
      }
    }
  }

  @Override
  public void visitOpcode(Node node) {
    String opcodeName = node.getValue().toString();
    Opcode newOpcode = Objects.requireNonNull(opcodesMap.get(opcodeName));

    opcode = newOpcode;
    regBitfield = opBitfield = posBitfield = 0;
    irBuilder = InstructionIR.builder().withOpcode(newOpcode);
    if (newOpcode == Opcode.LA) {
      laSeen = true;
    }
  }

  @Override
  public void visitReg(Node reg) {
    String registerName = reg.getValue().toString();
    int regNum = Objects.requireNonNull(registers.get(registerName));
    if (opcode.rd && (regBitfield & 4) == 0) {
      regBitfield |= 4;
      irBuilder.withRd(regNum);
    } else if (opcode.rs && (regBitfield & 2) == 0) {
      regBitfield |= 2;
      irBuilder.withRs(regNum);
    } else if (opcode.rt && (regBitfield & 1) == 0) {
      regBitfield |= 1;
      irBuilder.withRt(regNum);
    }
  }

  @Override
  public void visitBaseRegister(Node register) {
    String registerName = getLeftLeaf(register).getValue().toString();
    irBuilder.withRt(
        irBuilder
            .build()
            .getRs()); // reassign here because based is currently stored in rt from visitReg
    irBuilder.withRs(Objects.requireNonNull(registers.get(registerName)));
  }

  @Override
  public void visitExpression(Node expr) {
    Stack<String> ops = new Stack<>();
    Stack<Number> operands = new Stack<>();

    exprEval(expr, ops, operands);
    if (currentDataMode.isEmpty()) {
      int constant = operands.pop().intValue();
      if (opcode == Opcode.ALIGN
          || opcode == Opcode.ROTR
          || opcode == Opcode.SLL
          || opcode == Opcode.SRA
          || opcode == Opcode.SRL) {
        irBuilder.withSa(constant);
      } else if (opcode == Opcode.CACHE
          || opcode == Opcode.CACHEE
          || opcode == Opcode.PREF
          || opcode == Opcode.PREFE) {
        if ((opBitfield & 1) == 0) {
          irBuilder.withSa(constant);
          opBitfield |= 1;
        } else {
          irBuilder.withImmediate(constant).withOffset(constant);
        }
      } else if (opcode == Opcode.EXT || opcode == Opcode.INS) {
        if ((posBitfield & 1) == 0) {
          irBuilder.withPos(constant);
          posBitfield |= 1;
        } else if ((posBitfield & 2) == 0) {
          irBuilder.withSize(constant);
          posBitfield |= 2;
        }
      } else if (opcode == Opcode.GINVT || opcode == Opcode.LSA) {
        irBuilder.withSa(constant);
      } else {
        irBuilder.withImmediate(constant).withOffset(constant);
      }
    } else {
      switch (currentDataMode) {
        case "byte":
          layout.store(operands.pop().byteValue(), index);
          index += 1;
          break;
        case "half":
          layout.storeHalf(operands.pop().shortValue(), index);
          index += 2;
          break;
        case "word":
          layout.storeWord(operands.pop().intValue(), index);
          index += 4;
          break;
        case "float":
          layout.storeWord(Float.floatToRawIntBits(operands.pop().floatValue()), index);
          index += 4;
          break;
        case "double":
          layout.storeDword(Double.doubleToRawLongBits(operands.pop().doubleValue()), index);
          index += 8;
          break;
        case "space":
          index += operands.pop().intValue();
          break;
      }
    }
  }

  @Override
  public void visitDataSegment(Node data) {
    dataOffset = index;
    cLayout = 2;
  }

  @Override
  public void visitDataMode(Node data) {
    Node leftLeaf = getLeftLeaf(data);
    currentDataMode = leftLeaf.getValue().toString();
  }

  @Override
  public void
  visitData(Node data) {
    Node rightLeaf = getRightLeaf(data);
    switch (currentDataMode) {
      case "ascii":
        writeASCII(rightLeaf.getValue().toString());
        break;

      case "asciiz":
        writeASCII(rightLeaf.getValue().toString());
        layout.store((byte) 0, index);
        index += 1;
        break;
    }
  }

  @Override
  public void visitSegment(Node segment) {
    flush();
    laSeen = false;

    if (textOffset > -1) { // set on text segment parsed
      textBoundary = index;
    }

    stackPointer = Math.max(stackPointer, index * 128);
  }

  @Override
  public void visitOperand(Node operand) {
    Node leftLeaf = getLeftLeaf(operand);
    if (leftLeaf.getConstruct() == Construct.LABEL) {
      irBuilder.withLabel(leftLeaf.getValue().toString());
    }
  }

  @Override
  public void visitInstruction(Node instruction) {
    irs.add(irBuilder.build());
  }

  private void flushEncoding(InstructionIR instructionIR) {
    int encoding;
    Integer address;
    Opcode lookupOpcode;

    int currentRs = instructionIR.getRs();
    int currentRt = instructionIR.getRt();
    int currentPos = instructionIR.getPos();

    int currentRd = instructionIR.getRd();
    int currentOffset = instructionIR.getOffset();
    int currentImme = instructionIR.getImmediate();

    int currentShiftAmt = instructionIR.getSa();
    int currentSize = instructionIR.getSize();
    String currentLabel = instructionIR.getLabel();
    Opcode opcode = instructionIR.getOpcode();

    switch (opcode) {
      case SDC2:
      case LDC2:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 16
                | currentRd << 21
                | currentRs << 11 // swapped with rd, special R6 encoding
                | currentOffset & 0x7ff;
        break;
      case B:
      case BEQZ:
      case BEQ:
        address = symbolTable.get(currentLabel);
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("beq"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | (address != null ? computePcRelativeOffset(address) : currentImme) & 0xffff;
        break;
      case LA:
        address = symbolTable.get(currentLabel);
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("aui"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRt << 16
                | (address != null ? (address >> 16) : currentImme) & 0xffff;
        layout.storeWord(encoding, index);
        index += 4;

        lookupOpcode = Objects.requireNonNull(opcodesMap.get("ori"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRt << 21
                | currentRt << 16
                | (address != null ? address : currentImme) & 0xffff;
        break;
      case LI:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("ori"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentImme & 0xffff;
        break;
      case MOVE:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("or"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11;
        break;
      case NEGU:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("subu"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11;
        break;
      case NOP:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("sll"));
        encoding = lookupOpcode.partialEncoding | lookupOpcode.opcode;
        break;
      case NOT:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("nor"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11;
        break;
      case BNEZ:
        address = symbolTable.get(currentLabel);
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("bne"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | (address != null ? computePcRelativeOffset(address) : currentImme) & 0xffff;
        break;
      case BAL:
        address = symbolTable.get(currentLabel);
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | (address != null ? computePcRelativeOffset(address) : currentImme) & 0xffff;
        break;
      case BALC:
      case BC:
        address = symbolTable.get(currentLabel);
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | (address != null ? computePcRelativeOffset(address) : currentImme) & 0x3ffffff;
        break;
      case J:
      case JAL:
        address = symbolTable.get(currentLabel);
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | (address != null ? address / 4 : currentImme) & 0x3ffffff;
        break;
      case ULW:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("lwl"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentOffset & 0x7ff;
        layout.storeWord(encoding, index);
        index += 4;
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("lwr"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | (currentOffset + 3) & 0x7ff;
        break;
      case USW:
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("swl"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentOffset & 0x7ff;
        layout.storeWord(encoding, index);
        index += 4;
        lookupOpcode = Objects.requireNonNull(opcodesMap.get("swr"));
        encoding =
            lookupOpcode.partialEncoding
                | lookupOpcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentOffset + 3 & 0x7ff;
        break;
      case ABS_D:
      case ABS_S:
      case ADD_D:
      case ADD_S:
      case CMP_AF_D:
      case CMP_AF_S:
      case CMP_AT_D:
      case CMP_AT_S:
      case CMP_EQ_D:
      case CMP_EQ_S:
      case CMP_LE_D:
      case CMP_LE_S:
      case CMP_LT_D:
      case CMP_LT_S:
      case CMP_NE_D:
      case CMP_NE_S:
      case CMP_OGE_D:
      case CMP_OGE_S:
      case CMP_OGT_D:
      case CMP_OGT_S:
      case CMP_OR_D:
      case CMP_OR_S:
      case CMP_SAF_D:
      case CMP_SAF_S:
      case CMP_SAT_D:
      case CMP_SAT_S:
      case CMP_SEQ_D:
      case CMP_SEQ_S:
      case CMP_SLE_D:
      case CMP_SLE_S:
      case CMP_SLT_D:
      case CMP_SLT_S:
      case CMP_SNE_D:
      case CMP_SNE_S:
      case CMP_SOGE_D:
      case CMP_SOGE_S:
      case CMP_SOGT_D:
      case CMP_SOGT_S:
      case CMP_SOR_D:
      case CMP_SOR_S:
      case CMP_SUEQ_D:
      case CMP_SUEQ_S:
      case CMP_SUGE_D:
      case CMP_SUGE_S:
      case CMP_SUGT_D:
      case CMP_SUGT_S:
      case CMP_SULE_D:
      case CMP_SULE_S:
      case CMP_SULT_D:
      case CMP_SULT_S:
      case CMP_SUN_D:
      case CMP_SUN_S:
      case CMP_SUNE_D:
      case CMP_SUNE_S:
      case CMP_UEQ_D:
      case CMP_UEQ_S:
      case CMP_UGE_D:
      case CMP_UGE_S:
      case CMP_UGT_D:
      case CMP_UGT_S:
      case CMP_ULE_D:
      case CMP_ULE_S:
      case CMP_ULT_D:
      case CMP_ULT_S:
      case CMP_UN_D:
      case CMP_UN_S:
      case CMP_UNE_D:
      case CMP_UNE_S:
      case DIV_D:
      case DIV_S:
      case MADDF_D:
      case MADDF_S:
      case MAX_D:
      case MAXA_D:
      case MAXA_S:
      case MAX_S:
      case MIN_D:
      case MINA_D:
      case MINA_S:
      case MIN_S:
      case MOV_D:
      case MOV_S:
      case MSUBF_D:
      case MSUBF_S:
      case MUL_D:
      case MUL_S:
      case CEIL_L_D:
      case CEIL_L_S:
      case CEIL_W_D:
      case CEIL_W_S:
      case CLASS_D:
      case CLASS_S:
      case CVT_D_L:
      case CVT_D_S:
      case CVT_D_W:
      case CVT_L_D:
      case CVT_L_S:
      case CVT_S_D:
      case CVT_S_L:
      case CVT_S_W:
      case CVT_W_D:
      case CVT_W_S:
      case FLOOR_L_D:
      case FLOOR_L_S:
      case FLOOR_W_D:
      case FLOOR_W_S:
      case NEG_D:
      case NEG_S:
      case RECIP_D:
      case RECIP_S:
      case RINT_D:
      case RINT_S:
      case ROUND_L_D:
      case ROUND_L_S:
      case ROUND_W_D:
      case ROUND_W_S:
      case RSQRT_D:
      case RSQRT_S:
      case SEL_D:
      case SEL_S:
      case SELEQZ_D:
      case SELEQZ_S:
      case SELNEZ_D:
      case SELNEZ_S:
      case SQRT_D:
      case SQRT_S:
      case SUB_D:
      case SUB_S:
      case TRUNC_L_D:
      case TRUNC_L_S:
      case TRUNC_W_D:
      case TRUNC_W_S:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 16
                | currentRs << 11
                | currentRd << 6;
        break;
      case ADDIU:
      case ANDI:
      case AUI:
      case ORI:
      case SLTI:
      case SLTIU:
      case XORI:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 21
                | currentRs << 16 // rs contains rt due to format ordering
                | currentImme & 0xffff;
        break;
      case BC1EQZ:
      case BC1NEZ:
      case BC2EQZ:
      case BC2NEZ:
      case BEQC:
      case BEQZALC:
      case BEQZC:
      case BGEC:
      case BGEZAL:
      case BGEZ:
      case BGEUC:
      case BGTZALC:
      case BGTZC:
      case BGTZ:
      case BLEZALC:
      case BLEZC:
      case BLEZ:
      case BLTC:
      case BLTUC:
      case BLTZAL:
      case BLTZ:
      case BNE:
      case BNEC:
      case BNEZALC:
      case BNEZC:
      case BNVC:
      case BOVC:
      case JIC:
      case JIALC:
        address = symbolTable.get(currentLabel);
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | currentRt << 16
                | (address != null ? computePcRelativeOffset(address) : currentImme) & 0xffff;
        break;
      case BGEZC:
      case BGEZALC:
      case BLTZALC:
      case BLTZC:
        address = symbolTable.get(currentLabel);
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 21
                | currentRt << 16
                | (address != null ? computePcRelativeOffset(address) : currentImme) & 0xffff;
        break;
      case CACHE:
      case CACHEE:
      case PREF:
      case PREFE:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | currentShiftAmt << 16
                | (currentOffset & 0xffff) << 7;
        break;
      case CFC1:
      case CTC1:
      case MFC1:
      case MFHC1:
      case MTC1:
      case MTHC1:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 16 // rt
                | currentRt << 11; // fs
        break;
      case CFC2:
      case CTC2:
      case MTC2:
      case MFC2:
      case MFHC2:
      case MTHC2:
        encoding = opcode.partialEncoding | opcode.opcode | currentRt << 11 | currentRs << 16;
        break;
      case DVP:
      case EI:
      case EVP:
        encoding = opcode.partialEncoding | opcode.opcode | currentRt << 16 | currentImme & 0xffff;
        break;
      case COP2:
        encoding = opcode.partialEncoding | opcode.opcode | currentImme & 0x1ffffff;
        break;
      case CRC32B:
      case CRC32CB:
      case CRC32CH:
      case CRC32CW:
      case CRC32H:
      case CRC32W:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 21
                | currentRs << 16; // rt is captured in rs
        break;
      case EXT:
      case INS:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 21 // rs captured in rt
                | currentRs << 16
                | currentSize << 11
                | currentPos << 6;
        break;
      case GINVT:
        encoding = opcode.partialEncoding | opcode.opcode | currentRs << 21 | currentShiftAmt << 8;
        break;
      case LBE:
      case LBUE:
      case LHE:
      case LHUE:
      case LL:
      case LLE:
      case LWE:
      case SBE:
      case SC:
      case SCE:
      case SHE:
      case SWE:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | currentRt << 16
                | (currentOffset & 0x1ff) << 7;
        break;
      case LLWP:
      case LLWPE:
      case SCWP:
      case SCWPE:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | currentRd << 16 // rt is captured in rd. format: llwp rt, rd, (base)
                | currentRt << 11;
        break;
      case LWC2:
      case SWC2:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 11
                | currentRt << 16
                | currentOffset & 0x7ff;
        break;
      case LWL:
      case LWR:
      case SDC1:
      case SWC1:
      case SWL:
      case SWR:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentOffset & 0xffff;
        break;
      case LWPC:
        address = symbolTable.get(currentLabel);
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | (address != null ? address : currentImme) & 0x3ffff;
        break;
      case MFC0:
      case MFHC0:
      case MTC0:
      case MTHC0:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRd << 16
                | currentRt << 11 // rd is captured in rt
                | currentImme & 0x7;
        break;
      case RDHWR:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRd << 16
                | currentRt << 11 // rd is captured in rt
                | (currentImme & 0x7) << 6;
        break;
      case ROTRV:
      case SLLV:
      case SRAV:
      case SRLV:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 21 // rs
                | currentRs << 16 // rt
                | currentRd << 11;
        break;
      case SDBBP:
        encoding = opcode.partialEncoding | opcode.opcode | (currentImme & 0xfffff) << 6;
        break;
      case SRA:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRt << 16
                | currentRd << 11
                | (currentShiftAmt & 0x1f) << 6;
        break;
      case SYNC:
        encoding = opcode.partialEncoding | opcode.opcode | (currentImme & 0x1f) << 6;
        break;
      case SYNCI:
        encoding =
            opcode.partialEncoding | opcode.opcode | currentRt << 21 | (currentImme & 0xffff);
        break;
      case JALR:
      case JALR_HB:
        if (currentRs == 0) { // swap for single operand
          currentRs = currentRd;
          currentRd = 31;
        }

        encoding = opcode.partialEncoding | opcode.opcode | currentRs << 21 | currentRd << 11;
        break;
      case LW:
        if (currentLabel != null) {
          address = symbolTable.get(currentLabel);
          lookupOpcode = Objects.requireNonNull(opcodesMap.get("aui"));
          encoding =
              lookupOpcode.partialEncoding
                  | lookupOpcode.opcode
                  | currentRt << 16
                  | (address != null ? (address >> 16) : currentImme) & 0xffff;

          layout.storeWord(encoding, index);
          index += 4;
          lookupOpcode = Objects.requireNonNull(opcodesMap.get("ori"));

          encoding =
              lookupOpcode.partialEncoding
                  | lookupOpcode.opcode
                  | currentRt << 21
                  | currentRt << 16
                  | (address != null ? address : currentImme) & 0xffff;

          layout.storeWord(encoding, index);
          index += 4;
          if (currentRt == 0) {
            currentRt = currentRs;
          }
        }
      case ADD:
      case ADDIUPC:
      case ADDU:
      case ALIGN:
      case ALUIPC:
      case AND:
      case AUIPC:
      case BITSWAP:
      case BREAK:
      case CLO:
      case CLZ:
      case DI:
      case DIV:
      case DIVU:
      case DERET:
      case EHB:
      case ERET:
      case ERETNC:
      case GINVI:
      case JR:
      case JR_HB:
      case LB:
      case LBU:
      case LDC1:
      case LH:
      case LHU:
      case LSA:
      case LUI:
      case LWC1:
      case MADD:
      case MADDU:
      case MFHI:
      case MFLO:
      case MOD:
      case MODU:
      case MOVN:
      case MOVZ:
      case MSUB:
      case MSUBU:
      case MTHI:
      case MTLO:
      case MUH:
      case MUHU:
      case MUL:
      case MULT:
      case MULTU:
      case MULU:
      case NAL:
      case NOR:
      case OR:
      case PAUSE:
      case RDPGPR:
      case ROTR:
      case SB:
      case SEB:
      case SEH:
      case SELEQZ:
      case SELNEZ:
      case SH:
      case SIGRIE:
      case SLL:
      case SLT:
      case SLTU:
      case SRL:
      case SSNOP:
      case SUB:
      case SUBU:
      case SW:
      case SYSCALL:
      case TEQ:
      case TGE:
      case TGEU:
      case TLBINV:
      case TLBINVF:
      case TLBP:
      case TLBR:
      case TLBWI:
      case TLBWR:
      case TLT:
      case TLTU:
      case TNE:
      case WAIT:
      case WRPGPR:
      case WSBH:
      case XOR:
      default:
        encoding =
            opcode.partialEncoding
                | opcode.opcode
                | currentRs << 21
                | currentRt << 16
                | currentRd << 11
                | currentShiftAmt << 6
                | currentImme & 0xffff
                | currentOffset & 0xffff;
    }

    layout.storeWord(encoding, index);
    index += 4;
    int finalEncoding = encoding;
    observers.forEach(observer -> observer.onEmit(finalEncoding));
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

  public int getStackPointer() {
    return stackPointer;
  }

  public int getSourceOffset() {
    return sourceOffset;
  }

  public int getTextBoundary() {
    return textBoundary;
  }

  public Map<String, Integer> getSymbolTable() {
    return Collections.unmodifiableMap(symbolTable);
  }

  private void exprEval(Node root, Stack<String> ops, Stack<Number> operands) {
    for (Node child : root.getChildren()) {
      exprEval(child, ops, operands);
    }

    if (root.getNodeType() == NodeType.TERMINAL) {
      Object value = root.getValue();
      if (isOp(value.toString())) {
        ops.push(value.toString());
      } else {
        operands.push((Number) value);
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

  private void exprEval(Stack<String> ops, Stack<Number> operands) {
    if (!ops.empty() && !operands.empty()) {
      String op = ops.pop();
      int r = operands.pop().intValue();
      int l = operands.pop().intValue();

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

  private Node getRightLeaf(Node root) {
    Deque<Node> nodes = new ArrayDeque<>();
    nodes.add(root);
    Node leaf = null;
    while (!nodes.isEmpty()) {
      root = nodes.removeLast();
      if (root.getNodeType() == NodeType.TERMINAL) {
        leaf = root;
        break;
      }
      nodes.addAll(root.getChildren());
    }

    return leaf;
  }

  private void writeASCII(CharSequence tokens) {
    for (int i = 1, end = tokens.length() - 1; i < end; i++, index++) {
      if (tokens.charAt(i) == '\\' && i + 1 < end && tokens.charAt(i + 1) == 'n') {
        layout.store((byte) 10, index);
        i++;
      } else {
        layout.store((byte) tokens.charAt(i), index);
      }
    }
  }

  private void flush() {
    for (InstructionIR ir : irs) {
      flushEncoding(ir);
    }
  }

  public void resetInternalState() {
    irs.clear();
    symbolTable.clear();
    dataOffset = -1;

    textOffset = -1;
    index = 0;
    stackPointer = 0;

    textBoundary = 0;
  }

  private int computePcRelativeOffset(int address) {
    return (address - index - 4) / 4;
  }

  public void addObserver(EncodingEmitObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(EncodingEmitObserver observer) {
    observers.remove(observer);
  }
}
