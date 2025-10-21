/*
 * MIT License
 *
 * Copyright (c) 2022 CleverChuk
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software",0x0), to deal
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

package com.cleverchuk.mips.simulator.binary;

import static com.cleverchuk.mips.simulator.binary.InstructionFormat.IDIOM;
import static com.cleverchuk.mips.simulator.binary.InstructionFormat.I_TYPE;
import static com.cleverchuk.mips.simulator.binary.InstructionFormat.J_TYPE;
import static com.cleverchuk.mips.simulator.binary.InstructionFormat.R_TYPE;

public enum Opcode {
  // ARITHMETIC OPERATIONS
  ADD("add", 0x00, 0x0000020, R_TYPE, true, true, true),
  ADDI("addi", 0x20, 0x0, I_TYPE, true, true, false),
  ADDIU("addiu", 0x24, 0x0, I_TYPE, true, true, false),
  ADDIUPC("addiupc", 0xec, 0x0, I_TYPE, false, true, false), // FIXME: new
  ADDU("addu", 0x00, 0x0000021, R_TYPE, true, true, true),
  ALIGN("align", 0x7c, 0x0000220, R_TYPE, true, true, true), // FIXME: new
  ALUIPC("aluic", 0xec, 0x001f000, R_TYPE, false, true, false), // FIXME: new
  CLO("clo", 0x00, 0x00000051, R_TYPE, false, true, true),
  CLZ("clz", 0x00, 0x00000010, R_TYPE, false, true, true),
  LA("la", 0x0, 0x0, IDIOM, false, true, false), // assembler idiom, needs translation to actual instruction
  LI("li", 0x0, 0x0, IDIOM, false, true, false), // assembler idiom, needs translation to actual instruction
  LUI("lui", 0x3c, 0x0, I_TYPE, true, false, false), // replace by AUI in R6
  MOVE("move", 0x0, 0x0, IDIOM, false, false, false),// assembler idiom, needs translation to actual instruction
  NEGU("negu", 0x0, 0x0, IDIOM, false, false, false),// assembler idiom, needs translation to actual instruction
  SUB("sub", 0x00, 0x00000022, R_TYPE, true, true, true),
  SUBU("subu", 0x00, 0x00000023, R_TYPE, true, true, true),
  SEB("seb", 0x7c, 0x00000420, R_TYPE, true, false, true),
  SEH("seh", 0x7c, 0x00000620, R_TYPE, true, false, false),

  // SHIFT AND ROTATE OPERATIONS
  SLL("sll", 0x00, 0x0, R_TYPE, true, false, true),
  SLLV("sllv", 0x00, 0x00000004, R_TYPE, true, true, true),
  ROTR("rotr", 0x00, 0x00200002, R_TYPE, true, false, true),
  ROTRV("rotrv", 0x00, 0x00000046, R_TYPE, true, true, true),
  SRA("sra", 0x00, 0x00000003, R_TYPE, true, false, true),
  SRAV("srav", 0x00, 0x00000007, R_TYPE, true, true, true),
  SRL("srl", 0x00, 0x00000002, R_TYPE, true, false, true),
  SRLV("srlv", 0x00, 0x00000006, R_TYPE, true, true, true),
  SSNOP("ssnop", 0x00, 0x00000040, R_TYPE, false, false, false),

  // LOGICAL AND BIT-FIELD OPERATIONS
  AND("and", 0x00, 0x0000024, R_TYPE, true, true, true),
  ANDI("andi", 0x30, 0x0, I_TYPE, true, true, false),
  AUI("aui", 0x3c, 0x0, I_TYPE, true, true, false), // FIXME: new
  AUIPC("auipc", 0xec, 0x001e000, I_TYPE, false, true, false), // FIXME: new
  EXT("ext", 0x7c, 0x0, R_TYPE, true, true, false),
  INS("ins", 0x7c, 0x00000004, R_TYPE, true, true, false),
  NOP("nop", 0x00, 0x0, R_TYPE, false, false, false),
  NOR("nor", 0x00, 0x00000027, R_TYPE, true, true, true),
  NOT("not", 0x0, 0x0, IDIOM, false, false, false),
  OR("or", 0x00, 0x00000025, R_TYPE, true, true, true),
  ORI("ori", 0x34, 0x0, I_TYPE, true, true, false),
  XOR("xor", 0x00, 0x00000026, R_TYPE, true, true, true),
  XORI("xori", 0x38, 0x0, I_TYPE, true, true, false),
  WSBH("wsbh", 0x7c, 0x000000a0, R_TYPE, true, false, true),

  // CONDITION TESTING AND CONDITIONAL MOVE OPERATIONS
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MOVN("movn", 0x00, 0x0000000b, R_TYPE, true, true, true),
  MOVZ("movz", 0x00, 0x0000000a, R_TYPE, true, true, true),
  SLT("slt", 0x00, 0x0000002a, R_TYPE, true, true, true),
  SLTI("slti", 0x28, 0x0, I_TYPE, true, true, false),
  SLTIU("sltiu", 0x2c, 0x0, I_TYPE, true, true, false),
  SLTU("sltu", 0x00, 0x0000002b, R_TYPE, true, true, true),

  // MULTIPLY AND DIVIDE OPERATIONS
  DIV("div", 0x00, 0x0000009a, R_TYPE, true, true, true),
  MOD("mod", 0x00, 0x000000da, R_TYPE, true, true, true),
  MUL("mul", 0x00, 0x00000098, R_TYPE, true, true, true),
  MUH("muh", 0x00, 0x000000d8, R_TYPE, true, true, true),
  MULU("mulu", 0x00, 0x00000099, R_TYPE, true, true, true),
  MUHU("muhu", 0x00, 0x000000d9, R_TYPE, true, true, true),
  DIVU("divu", 0x00, 0x0000009b, R_TYPE, true, true, true),
  MODU("modu", 0x00, 0x000000db, R_TYPE, true, true,  true),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MADD("madd", 0x70, 0x0, R_TYPE, true, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MADDU("maddu", 0x70, 0x00000001, R_TYPE, true, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MSUB("msub", 0x70, 0x00000004, R_TYPE, true, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MSUBU("msubu", 0x70, 0x00000005, R_TYPE, true, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MULT("mult", 0x00, 0x00000018, R_TYPE, true, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MULTU("multu", 0x00, 0x00000019, R_TYPE, true, true, false),

  // JUMPS AND BRANCHES
  BEQ("beq", 0x10, 0x0, I_TYPE, true, true, false),
  BNE("bne", 0x14, 0x0, I_TYPE, true, true, false),
  BOVC("bovc", 0x20, 0x0, I_TYPE, true, true, false),
  BNVC("bnvc", 0x60, 0x0, I_TYPE, true, true, false),
  BREAK("break", 0x0, 0x000000d, J_TYPE, false, false, false),
  @Deprecated(forRemoval = true, since = "Release 6 use BC")
  J("j", 0x08, 0x0, J_TYPE, false, false, false),
  @Deprecated(forRemoval = true, since = "Release 6, use BALC")
  JAL("jal", 0x0c, 0x0, J_TYPE, false, false, false),
  JALR("jalr", 0x0, 0x00000009, R_TYPE, false, true, true),
  JALR_HB("jalr.hb", 0x0, 0x00000409, R_TYPE, false, true, true),
  JR("jr", 0x0, 0x00000009, J_TYPE, false, true, false), // replace by JALR in R6
  JR_HB("jr.hb", 0x0, 0x00000409, J_TYPE, false, true, false), // replace by JALR_HB in R6
  B("b", 0x10, 0x0, I_TYPE, false, false, false), // assembly idiom actual instruction is BEQ r0, r0, offset
  BAL("bal", 0x04, 0x0011000, I_TYPE, false, false, false), // assembly idiom actual instruction is BGEZAL r0, offset
  BALC("balc", 0xe8, 0x0, I_TYPE, false, false, false), // FIXME: new
  BC("bc", 0xc8, 0x0, I_TYPE, false, false, false), // FIXME: new
  BEQZ("beqz", 0x10, 0x0, I_TYPE, false, true, false),
  BGEZ("bgez", 0x04, 0x0001, I_TYPE, false, true, false),
  BGTZ("bgtz", 0x1c, 0x0, I_TYPE, false, true, false),
  BITSWAP("bitswap", 0x7c, 0x0000020, R_TYPE, false, true, false), // FIXME: new
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  BGEZAL("bgezal", 0x0411000, 0x0000020, R_TYPE, false, true, false),
  BLEZALC("blezalc", 0x18, 0x0, R_TYPE, false, true, false), // FIXME: new
  BGEZALC("bgezalc", 0x18, 0x0, R_TYPE, false, true, false), // FIXME: new
  BGTZALC("bgtzalc", 0x1c, 0x0, R_TYPE, false, true, false), // FIXME: new
  BLTZALC("bltzalc", 0x1c, 0x0, R_TYPE, false, true, false), // FIXME: new
  BEQZALC("beqzalc", 0x20, 0x0, R_TYPE, false, true, false), // FIXME: new
  BNEZALC("bnezalc", 0x60, 0x0, R_TYPE, false, true, false), // FIXME: new
  BLEZC("blezc", 0x58, 0x0, R_TYPE, false, true, false), // FIXME: new
  BGEZC("bgezc", 0x58, 0x0, R_TYPE, false, true, false), // FIXME: new
  BGEC("bgec", 0x58, 0x0, R_TYPE, false, true, false), // FIXME: new
  BGTZC("bgtzc", 0x5c, 0x0, R_TYPE, false, true, false), // FIXME: new
  BLTZC("bltzc", 0x5c, 0x0, R_TYPE, false, true, false), // FIXME: new
  BLTC("bltc", 0x5c, 0x0, R_TYPE, false, true, false), // FIXME: new
  BGEUC("bgeuc", 0x18, 0x0, R_TYPE, false, true, false), // FIXME: new
  BLTUC("bltuc", 0x1c, 0x0, R_TYPE, false, true, false), // FIXME: new
  BEQC("beqc", 0x20, 0x0, R_TYPE, false, true, false), // FIXME: new
  BNEC("bnec", 0x60, 0x0, R_TYPE, false, true, false), // FIXME: new
  BEQZC("beqzc", 0xd8, 0x0, R_TYPE, false, true, false), // FIXME: new
  BNEZC("bnezc", 0xf8, 0x0, R_TYPE, false, true, false), // FIXME: new
  BLEZ("blez", 0x18, 0x0, R_TYPE, false, true, false),
  BLTZ("bltz", 0x04, 0x0, R_TYPE, false, true, false),
  BNEZ("bnez", 0x0, 0x0, R_TYPE, false, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  BLTZAL("bltzal", 0x04, 0x0010000, R_TYPE, false, true, false),
  JIALC("jialc", 0xf8, 0x0, I_TYPE, true, false, false),
  JIC("jic", 0xd8, 0x0, R_TYPE, false, true, false),
  NAL("nal", 0x04, 0x00100000, R_TYPE, false, true, false),
  TEQ("teq", 0x00, 0x00000034, R_TYPE, false, true, false),
  TGE("tge", 0x00, 0x00000030, R_TYPE, false, true, false),
  TGEU("tgeu", 0x00, 0x00000031, R_TYPE, false, true, false),
  TLT("tlt", 0x00, 0x00000032, R_TYPE, false, true, false),
  TLTU("tltu", 0x00, 0x00000033, R_TYPE, false, true, false),
  TNE("tne", 0x00, 0x00000036, R_TYPE, false, true, false),

  // LOAD AND STORE OPERATIONS
  LW("lw", 0x8c, 0x0, R_TYPE, false, true, false),
  LWE("lwe", 0x7c, 0x0000002f, R_TYPE, false, true, false),
  SW("sw", 0xac, 0x0, R_TYPE, false, true, false),
  SWE("swe", 0x48, 0x0000001f, R_TYPE, false, true, false),
  SWC1("swc1", 0xe4, 0x0, R_TYPE, false, true, false),
  SWC2("swc2", 0x48, 0x01600000, R_TYPE, false, true, false),
  LB("lb", 0x80, 0x0, R_TYPE, false, true, false),
  LBE("lbe", 0x7c, 0x0000002c, R_TYPE, false, true, false),
  LBU("lbu", 0x90, 0x0, R_TYPE, false, true, false),
  LBUE("lbue", 0x7c, 0x00000028, R_TYPE, false, true, false),
  LH("lh", 0x84, 0x0, R_TYPE, false, true, false),
  LHE("lhe", 0x7c, 0x0000002d, R_TYPE, false, true, false),
  LHU("lhu", 0x94, 0x0, R_TYPE, false, true, false),
  LHUE("lhue", 0x7c, 0x00000029, R_TYPE, false, true, false),
  LSA("lsa", 0x0, 0x00000005, R_TYPE, false, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  LWL("lwl", 0x88, 0x0, R_TYPE, false, true, false),
  LWPC("lwpc", 0xec, 0x00080000, R_TYPE, false, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  LWR("lwr", 0x98, 0x0, R_TYPE, false, true, false),
  SB("sb", 0xa0, 0x0, R_TYPE, false, true, false),
  SBE("sbe", 0x7c, 0x0000001c, R_TYPE, false, true, false),
  SH("sh", 0xa4, 0x0, R_TYPE, false, true, false),
  SHE("she", 0x7c, 0x0000001d, R_TYPE, false, true, false),
  SWL("swl", 0x0, 0x0, R_TYPE, false, true, false),
  SWR("swr", 0x0, 0x0, R_TYPE, false, true, false),
  ULW("ulw", 0x0, 0x0, R_TYPE, false, true, false),
  USW("usw", 0x0, 0x0, R_TYPE, false, true, false),
  CACHE("cache", 0x7c, 0x0000025, R_TYPE, false, true, false),
  CACHEE("cachee", 0x7c, 0x000001b, R_TYPE, false, true, false),

  // ACCUMULATOR ACCESS OPERATIONS
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MFHI("mfhi", 0x00, 0x00000010, R_TYPE, false, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MFLO("mflo", 0x00, 0x00000012, R_TYPE, false, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MTHI("mthi", 0x00, 0x00000011, R_TYPE, false, true, false),
  @Deprecated(forRemoval = true, since = "Removed in Release 6")
  MTLO("mtlo", 0x00, 0x00000013, R_TYPE, false, true, false),

  // ATOMIC READ-MODIFY-WRITE OPERATIONS
  LL("ll", 0x7c, 0x00000036, R_TYPE, false, true, false),
  LLE("lle", 0x7c, 0x0000002e, R_TYPE, false, true, false),
  LLWP("llwp", 0x7c, 0x00000076, R_TYPE, false, true, false),
  LLWPE("llwpe", 0x7c, 0x0000006e, R_TYPE, false, true, false),
  SC("sc", 0x7c, 0x00000026, R_TYPE, false, true, false),
  SCE("sce", 0x7c, 0x0000001e, R_TYPE, false, true, false),
  SCWP("scwp", 0x7c, 0x00000066, R_TYPE, false, true, false),
  SCWPE("scwpe", 0x7c, 0x0000005e, R_TYPE, false, true, false),

  // Data transfer instructions
  LDC1("ldc1", 0xd4, 0x0, R_TYPE, false, true, false),
  LDC2("ldc2", 0x48, 0x01c00000, R_TYPE, false, true, false),
  LWC1("lwc1", 0xc4, 0x0, R_TYPE, false, true, false),
  LWC2("lwc2", 0x48, 0x01400000, R_TYPE, false, true, false),
  SDC1("sdc1", 0xf4, 0x0, R_TYPE, false, true, false),
  SDC2("sdc2", 0x48, 0x0000001e, R_TYPE, false, true, false),
  CFC1("cfc1", 0x44, 0x0040000, R_TYPE, false, true, false),
  CFC2("cfc2", 0x48, 0x0040000, R_TYPE, false, true, false),
  CTC1("ctc1", 0x44, 0x00c00000, R_TYPE, false, true, false),
  CTC2("ctc2", 0x48, 0x00c00000, R_TYPE, false, true, false),
  MFC0("mfc0", 0x40, 0x0, R_TYPE, false, true, false),
  MFC1("mfc1", 0x44, 0x0, R_TYPE, false, true, false),
  MFC2("mfc2", 0x48, 0x0, R_TYPE, false, true, false),
  MFHC0("mfhc0", 0x40, 0x00400000, R_TYPE, false, true, false),
  MFHC1("mfhc1", 0x44, 0x00600000, R_TYPE, false, true, false),
  MFHC2("mfhc2", 0x48, 0x00600000, R_TYPE, false, true, false),
  MTC0("mtc0", 0x40, 0x00800000, R_TYPE, false, true, false),
  MTC1("mtc1", 0x44, 0x00800000, R_TYPE, false, true, false),
  MTC2("mtc2", 0x48, 0x00800000, R_TYPE, false, true, false),
  MTHC0("mthc0", 0x40, 0x00c00000, R_TYPE, false, true, false),
  MTHC1("mthc1", 0x44, 0x00e00000, R_TYPE, false, true, false),
  MTHC2("mthc2", 0x48, 0x00e00000, R_TYPE, false, true, false),
  PREF("pref", 0x7c, 0x00000035, R_TYPE, false, true, false),
  PREFE("prefe", 0x7c, 0x00000023, R_TYPE, false, true, false),
  RDHWR("rdhwr", 0x7c, 0x0000003b, R_TYPE, false, true, false),
  RDPGPR("rdpgpr", 0x40, 0x01400000, R_TYPE, false, true, false),

  // FPU IEEE arithmetic operations
  ABS_S("abs.s", 0x44, 0x0200005, R_TYPE, false, true, false),
  ABS_D("abs.d", 0x44, 0x0220005, R_TYPE, false, true, false),
  ADD_S("add.s", 0x44, 0x0200000, R_TYPE, false, true, false),
  CMP_AF_S("cmp.af.s", 0x44, 0x02800000, R_TYPE, false, true, false),
  CMP_AF_D("cmp.af.d", 0x44, 0x02a00000, R_TYPE, false, true, false),
  CMP_UN_S("cmp.un.s", 0x44, 0x02800001, R_TYPE, false, true, false),
  CMP_UN_D("cmp.un.d", 0x44, 0x02a00001, R_TYPE, false, true, false),
  CMP_EQ_S("cmp.eq.s", 0x44, 0x02800002, R_TYPE, false, true, false),
  CMP_EQ_D("cmp.eq.d", 0x44, 0x02a00002, R_TYPE, false, true, false),
  CMP_UEQ_S("cmp.ueq.s", 0x44, 0x02800003, R_TYPE, false, true, false),
  CMP_UEQ_D("cmp.ueq.d", 0x44, 0x02a00003, R_TYPE, false, true, false),
  CMP_LT_S("cmp.lt.s", 0x44, 0x02800004, R_TYPE, false, true, false),
  CMP_LT_D("cmp.lt.d", 0x44, 0x02a00004, R_TYPE, false, true, false),
  CMP_ULT_S("cmp.ult.s", 0x44, 0x02800005, R_TYPE, false, true, false),
  CMP_ULT_D("cmp.ult.d", 0x44, 0x02a00005, R_TYPE, false, true, false),
  CMP_LE_S("cmp.le.s", 0x44, 0x02800006, R_TYPE, false, true, false),
  CMP_LE_D("cmp.le.d", 0x44, 0x02a000006, R_TYPE, false, true, false),
  CMP_ULE_S("cmp.ule.s", 0x44, 0x02800007, R_TYPE, false, true, false),
  CMP_ULE_D("cmp.ule.d", 0x44, 0x02a00007, R_TYPE, false, true, false),
  CMP_SAF_S("cmp.saf.s", 0x44, 0x02800008, R_TYPE, false, true, false),
  CMP_SAF_D("cmp.saf.d", 0x44, 0x02a00008, R_TYPE, false, true, false),
  CMP_SUN_S("cmp.sun.s", 0x44, 0x02800009, R_TYPE, false, true, false),
  CMP_SUN_D("cmp.sun.d", 0x44, 0x02a00009, R_TYPE, false, true, false),
  CMP_SEQ_S("cmp.seq.s", 0x44, 0x0280000a, R_TYPE, false, true, false),
  CMP_SEQ_D("cmp.seq.d", 0x44, 0x02a0000a, R_TYPE, false, true, false),
  CMP_SUEQ_S("cmp.sueq.s", 0x44, 0x0280000b, R_TYPE, false, true, false),
  CMP_SUEQ_D("cmp.sueq.d", 0x44, 0x02a0000b, R_TYPE, false, true, false),
  CMP_SLT_S("cmp.slt.s", 0x44, 0x0280000c, R_TYPE, false, true, false),
  CMP_SLT_D("cmp.slt.d", 0x44, 0x02a0000c, R_TYPE, false, true, false),
  CMP_SULT_S("cmp.sult.s", 0x44, 0x0280000d, R_TYPE, false, true, false),
  CMP_SULT_D("cmp.sult.d", 0x44, 0x02a0000d, R_TYPE, false, true, false),
  CMP_SLE_S("cmp.sle.s", 0x44, 0x0280000e, R_TYPE, false, true, false),
  CMP_SLE_D("cmp.sle.d", 0x44, 0x02a0000e, R_TYPE, false, true, false),
  CMP_SULE_S("cmp.sule.s", 0x44, 0x0280000f, R_TYPE, false, true, false),
  CMP_SULE_D("cmp.sule.d", 0x44, 0x02a0000f, R_TYPE, false, true, false),
  CMP_AT_S("cmp.at.s", 0x44, 0x02800010, R_TYPE, false, true, false),
  CMP_AT_D("cmp.at.d", 0x44, 0x02a00010, R_TYPE, false, true, false),
  CMP_OR_S("cmp.or.s", 0x44, 0x02800011, R_TYPE, false, true, false),
  CMP_OR_D("cmp.or.d", 0x44, 0x02a00011, R_TYPE, false, true, false),
  CMP_UNE_S("cmp.une.s", 0x44, 0x02800012, R_TYPE, false, true, false),
  CMP_UNE_D("cmp.une.d", 0x44, 0x02a00012, R_TYPE, false, true, false),
  CMP_NE_S("cmp.ne.s", 0x44, 0x02800013, R_TYPE, false, true, false),
  CMP_NE_D("cmp.ne.d", 0x44, 0x02a00013, R_TYPE, false, true, false),
  CMP_UGE_S("cmp.uge.s", 0x44, 0x02800014, R_TYPE, false, true, false),
  CMP_UGE_D("cmp.uge.d", 0x44, 0x02a00014, R_TYPE, false, true, false),
  CMP_OGE_S("cmp.oge.s", 0x44, 0x02800015, R_TYPE, false, true, false),
  CMP_OGE_D("cmp.oge.d", 0x44, 0x02a00015, R_TYPE, false, true, false),
  CMP_UGT_S("cmp.ugt.s", 0x44, 0x02800016, R_TYPE, false, true, false),
  CMP_UGT_D("cmp.ugt.d", 0x44, 0x02a00016, R_TYPE, false, true, false),
  CMP_OGT_S("cmp.ogt.s", 0x44, 0x02800017, R_TYPE, false, true, false),
  CMP_OGT_D("cmp.ogt.d", 0x44, 0x02a00017, R_TYPE, false, true, false),
  CMP_SAT_S("cmp.sat.s", 0x44, 0x02800018, R_TYPE, false, true, false),
  CMP_SAT_D("cmp.sat.d", 0x44, 0x02a00018, R_TYPE, false, true, false),
  CMP_SOR_S("cmp.sor.s", 0x44, 0x02800019, R_TYPE, false, true, false),
  CMP_SOR_D("cmp.sor.d", 0x44, 0x02a00019, R_TYPE, false, true, false),
  CMP_SUNE_S("cmp.sune.s", 0x44, 0x0280001a, R_TYPE, false, true, false),
  CMP_SUNE_D("cmp.sune.d", 0x44, 0x02a0001a, R_TYPE, false, true, false),
  CMP_SNE_S("cmp.sne.s", 0x44, 0x0280001b, R_TYPE, false, true, false),
  CMP_SNE_D("cmp.sne.d", 0x44, 0x02a0001b, R_TYPE, false, true, false),
  CMP_SUGE_S("cmp.suge.s", 0x44, 0x0280001c, R_TYPE, false, true, false),
  CMP_SUGE_D("cmp.suge.d", 0x44, 0x02a0001c, R_TYPE, false, true, false),
  CMP_SOGE_S("cmp.soge.s", 0x44, 0x0280001d, R_TYPE, false, true, false),
  CMP_SOGE_D("cmp.soge.d", 0x44, 0x02a0001d, R_TYPE, false, true, false),
  CMP_SUGT_S("cmp.sugt.s", 0x44, 0x0280001e, R_TYPE, false, true, false),
  CMP_SUGT_D("cmp.sugt.d", 0x44, 0x02a0001e, R_TYPE, false, true, false),
  CMP_SOGT_S("cmp.sogt.s", 0x44, 0x0280001f, R_TYPE, false, true, false),
  CMP_SOGT_D("cmp.sogt.d", 0x44, 0x02a0001f, R_TYPE, false, true, false),
  COP2("cop2", 0x48, 0x02000000, R_TYPE, false, true, false),
  CRC32B("crc32b", 0x7c, 0x0000000f, R_TYPE, false, true, false),
  CRC32H("crc32h", 0x7c, 0x0000004f, R_TYPE, false, true, false),
  CRC32W("crc32w", 0x7c, 0x0000008f, R_TYPE, false, true, false),
  CRC32CB("crc32cb", 0x7c, 0x0000010f, R_TYPE, false, true, false),
  CRC32CH("crc32ch", 0x7c, 0x0000014f, R_TYPE, false, true, false),
  CRC32CW("crc32cw", 0x7c, 0x0000018f, R_TYPE, false, true, false),
  DIV_S("div.s", 0x44, 0x02000003, R_TYPE, false, true, false),
  DIV_D("div.d", 0x44, 0x02200003, R_TYPE, false, true, false),
  MUL_S("mul.s", 0x44, 0x02000002, R_TYPE, false, true, false),
  MUL_D("mul.d", 0x44, 0x02200002, R_TYPE, false, true, false),
  NEG_S("neg.s", 0x44, 0x02000007, R_TYPE, false, true, false),
  NEG_D("neg.d", 0x44, 0x02200007, R_TYPE, false, true, false),
  SQRT_S("sqrt.s", 0x44, 0x02000004, R_TYPE, false, true, false),
  SQRT_D("sqrt.d", 0x44, 0x02200004, R_TYPE, false, true, false),
  SUB_S("sub.s", 0x44, 0x02000001, R_TYPE, false, true, false),
  SUB_D("sub.d", 0x44, 0x02200001, R_TYPE, false, true, false),

  // FPU non-IEEE-approximate Arithmetic Instructions
  RECIP_S("recip.s", 0x44, 0x02000015, R_TYPE, false, true, false),
  RECIP_D("recip.d", 0x44, 0x02200015, R_TYPE, false, true, false),
  RSQRT_S("rsqrt.s", 0x44, 0x02000016, R_TYPE, false, true, false),
  RSQRT_D("rsqrt.d", 0x44, 0x02200016, R_TYPE, false, true, false),

  // FPU Fused Multiply-Accumulate Arithmetic Operations (Release 6)
  MADDF_S("maddf.s", 0x44, 0x02000018, R_TYPE, false, true, false),
  MADDF_D("maddf.d", 0x44, 0x02200018, R_TYPE, false, true, false),
  MSUBF_S("msubf.s", 0x44, 0x02000019, R_TYPE, false, true, false),
  MSUBF_D("msubf.d", 0x44, 0x02200019, R_TYPE, false, true, false),

  // Floating Point Comparison Instructions
  CLASS_S("class.s", 0x44, 0x0200001b, R_TYPE, false, true, false),
  CLASS_D("class.d", 0x44, 0x0220001b, R_TYPE, false, true, false),
  MAX_S("max.s", 0x44, 0x0200001e, R_TYPE, false, true, false),
  MAX_D("max.d", 0x44, 0x0220001e, R_TYPE, false, true, false),
  MAXA_S("maxa.s", 0x44, 0x0200001f, R_TYPE, false, true, false),
  MAXA_D("maxa.d", 0x44, 0x0220001f, R_TYPE, false, true, false),
  MIN_S("min.s", 0x44, 0x0200001c, R_TYPE, false, true, false),
  MIN_D("min.d", 0x44, 0x0220001c, R_TYPE, false, true, false),
  MINA_S("mina.s", 0x44, 0x0200001d, R_TYPE, false, true, false),
  MINA_D("mina.d", 0x44, 0x0220001d, R_TYPE, false, true, false),

  // FPU Conversion Operations Using the FCSR Rounding Mode
  CVT_D_S("cvt.d.s", 0x44, 0x02000021, R_TYPE, false, true, false),
  CVT_D_W("cvt.d.w", 0x44, 0x02800021, R_TYPE, false, true, false),
  CVT_D_L("cvt.d.l", 0x44, 0x02a00021, R_TYPE, false, true, false),
  CVT_L_S("cvt.l.s", 0x44, 0x02000025, R_TYPE, false, true, false),
  CVT_L_D("cvt.l.d", 0x44, 0x02200025, R_TYPE, false, true, false),
  CVT_S_D("cvt.s.d", 0x44, 0x02200020, R_TYPE, false, true, false),
  CVT_S_W("cvt.s.w", 0x44, 0x02800020, R_TYPE, false, true, false),
  CVT_S_L("cvt.s.l", 0x44, 0x02a00020, R_TYPE, false, true, false),
  CVT_W_S("cvt.w.s", 0x44, 0x02000024, R_TYPE, false, true, false),
  CVT_W_D("cvt.w.d", 0x44, 0x0220002, R_TYPE, false, true, false),
  RINT_S("rint.s", 0x44, 0x0200001a, R_TYPE, false, true, false),
  RINT_D("rint.d", 0x44, 0x022001a, R_TYPE, false, true, false),

  // FPU Conversion Operations Using a Directed Rounding Mode
  CEIL_L_S("ceil.l.s", 0x44, 0x020000a, R_TYPE, false, true, false),
  CEIL_L_D("ceil.l.d", 0x44, 0x022000a, R_TYPE, false, true, false),
  CEIL_W_S("ceil.w.s", 0x44, 0x020000e, R_TYPE, false, true, false),
  CEIL_W_D("ceil.w.d", 0x44, 0x022000e, R_TYPE, false, true, false),
  FLOOR_L_S("floor.l.s", 0x44, 0x0200000b, R_TYPE, false, true, false),
  FLOOR_L_D("floor.l.d", 0x44, 0x0220000b, R_TYPE, false, true, false),
  FLOOR_W_S("floor.w.s", 0x44, 0x0200000f, R_TYPE, false, true, false),
  FLOOR_W_D("floor.w.d", 0x44, 0x0220000f, R_TYPE, false, true, false),
  ROUND_L_S("round.l.s", 0x44, 0x02000008, R_TYPE, false, true, false),
  ROUND_L_D("round.l.d", 0x44, 0x02200008, R_TYPE, false, true, false),
  ROUND_W_S("round.w.s", 0x44, 0x0200000c, R_TYPE, false, true, false),
  ROUND_W_D("round.w.d", 0x44, 0x0220000c, R_TYPE, false, true, false),
  TRUNC_L_S("trunc.l.s", 0x44, 0x02000009, R_TYPE, false, true, false),
  TRUNC_L_D("trunc.l.d", 0x44, 0x02200009, R_TYPE, false, true, false),
  TRUNC_W_S("trunc.w.s", 0x44, 0x0200000d, R_TYPE, false, true, false),
  TRUNC_W_D("trunc.w.d", 0x44, 0x0220000d, R_TYPE, false, true, false),

  // FPU Formatted Unconditional Operand Move Instructions
  MOV_S("mov.s", 0x44, 0x02000006, R_TYPE, false, true, false),
  MOV_D("mov.d", 0x44, 0x02200006, R_TYPE, false, true, false),

  // FPU Conditional Select Instructions
  SEL_S("sel.s", 0x44, 0x02000010, R_TYPE, false, true, false),
  SEL_D("sel.d", 0x44, 0x02200010, R_TYPE, false, true, false),
  SELEQZ("seleqz", 0x00, 0x00000035, R_TYPE, false, true, false),
  SELEQZ_S("seleqz.s", 0x00, 0x02000014, R_TYPE, false, true, false),
  SELEQZ_D("seleqz.d", 0x00, 0x02200014, R_TYPE, false, true, false),
  SELNEZ("selnez", 0x00, 0x00000037, R_TYPE, false, true, false),
  SELNEZ_S("selnez.s", 0x00, 0x02000017, R_TYPE, false, true, false),
  SELNEZ_D("selnez.d", 0x00, 0x02200017, R_TYPE, false, true, false),

  // FPU Conditional Branch Instructions
  BC1EQZ("bc1eqz", 0x44, 0x0120000, R_TYPE, false, true, false),
  BC1NEZ("bc1nez", 0x44, 0x01a0000, R_TYPE, false, true, false),
  BC2EQZ("bc2eqz", 0x48, 0x0120000, R_TYPE, false, true, false),
  BC2NEZ("bc2nez", 0x48, 0x01a0000, R_TYPE, false, true, false),

  // System utilities
  DERET("deret", 0x40, 0x0200001f, R_TYPE, false, true, false),
  DI("di", 0x40, 0x01606000, R_TYPE, false, true, false),
  DVP("dvp", 0x40, 0x01600024, R_TYPE, false, true, false),
  EVP("evp", 0x40, 0x01600004, R_TYPE, false, true, false),
  EHB("ehb", 0x00, 0x000000c0, R_TYPE, false, true, false),
  EI("ei", 0x40, 0x01606020, R_TYPE, false, true, false),
  ERET("eret", 0x40, 0x02000018, R_TYPE, false, true, false),
  ERETNC("eretnc", 0x40, 0x02000058, R_TYPE, false, true, false),
  GINVI("ginvi", 0x7c, 0x0000003d, R_TYPE, false, true, false),
  GINVT("ginvt", 0x7c, 0x000000bd, R_TYPE, false, true, false),
  PAUSE("pause", 0x00, 0x0, R_TYPE, false, true, false),
  SDBBP("sdbbp", 0x00, 0x0000000e, R_TYPE, false, true, false),
  SIGRIE("sigrie", 0x04, 0x00170000, R_TYPE, false, true, false),
  SYSCALL("syscall", 0x00, 0x0000000c, R_TYPE, false, true, false),
  SYNC("sync", 0x00, 0x0000000f, R_TYPE, false, true, false),
  SYNCI("synci", 0x04, 0x001f0000, R_TYPE, false, true, false),
  TLBINV("tlbinv", 0x40, 0x02000003, R_TYPE, false, true, false),
  TLBINVF("tlbinvf", 0x40, 0x02000004, R_TYPE, false, true, false),
  TLBP("tlbp", 0x40, 0x02000008, R_TYPE, false, true, false),
  TLBR("tlbr", 0x40, 0x02000001, R_TYPE, false, true, false),
  TLBWI("tlbwi", 0x40, 0x02000002, R_TYPE, false, true, false),
  TLBWR("tlbwr", 0x40, 0x02000006, R_TYPE, false, true, false),
  WAIT("wait", 0x40, 0x02000020, R_TYPE, false, true, false),
  WRPGPR("wrpgpr", 0x40, 0x01c00000, R_TYPE, false, true, false),
  ;

  public final String name;

  public final int opcode;

  public final int partialEncoding;

  public final InstructionFormat format;

  public final boolean rs;

  public final boolean rt;

  public final boolean rd;

  Opcode(String name, int opcode, int partialEncoding, InstructionFormat format, boolean rt, boolean rs, boolean rd) {
    this.name = name;
    this.opcode = opcode;
    this.partialEncoding = partialEncoding;
    this.format = format;
    this.rt = rt;
    this.rs = rs;
    this.rd = rd;
  }
}
