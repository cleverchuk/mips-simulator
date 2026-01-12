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

import com.cleverchuk.mips.simulator.binary.InstructionDecoder;
import com.cleverchuk.mips.simulator.binary.Opcode;

public class Disassembler {

  public String disassemble(int instruction) {

    Opcode opcode = InstructionDecoder.decode(instruction);
    if (opcode == null) {
      return "";
    }

    switch (opcode) {
      case ADD:
        return add(instruction);
      case ADDU:
        return addu(instruction);
      case ADDIU:
        return addiu(instruction);
      case ADDIUPC:
        return addiupc(instruction);
      case ALIGN:
        return align(instruction);
      case ALUIPC:
        return aluipc(instruction);
      case CLO:
        return clo(instruction);
      case CLZ:
        return clz(instruction);
      case SUB:
        return sub(instruction);
      case SUBU:
        return subu(instruction);
      case SEB:
        return seb(instruction);
      case SEH:
        return seh(instruction);
      case SLL:
        return sll(instruction);
      case SLLV:
        return sllv(instruction);
      case ROTR:
        return rotr(instruction);
      case ROTRV:
        return rotrv(instruction);
      case SRA:
        return sra(instruction);
      case SRAV:
        return srav(instruction);
      case SRL:
        return srl(instruction);
      case SRLV:
        return srlv(instruction);
      case AND:
        return and_(instruction);
      case ANDI:
        return andi(instruction);
      case AUI:
        return aui(instruction);
      case AUIPC:
        return auipc(instruction);
      case BAL:
        return bal(instruction);
      case BALC:
        return balc(instruction);
      case BC:
        return bc(instruction);
      case BC1EQZ:
        return bc1eqz(instruction);
      case BC1NEZ:
        return bc1nez(instruction);
      case BC2EQZ:
        return bc2eqz(instruction);
      case BC2NEZ:
        return bc2nez(instruction);
      case EXT:
        return ext(instruction);
      case INS:
        return ins(instruction);
      case NOR:
        return nor(instruction);
      case OR:
        return or_(instruction);
      case ORI:
        return ori(instruction);
      case XOR:
        return xor_(instruction);
      case XORI:
        return xori(instruction);
      case WSBH:
        return wsbh(instruction);
      case MOVN:
        return movn(instruction);
      case MOVZ:
        return movz(instruction);
      case SLT:
        return slt(instruction);
      case SLTI:
        return slti(instruction);
      case SLTIU:
        return sltiu(instruction);
      case SLTU:
        return sltu(instruction);
      case DIV:
        return div(instruction);
      case MOD:
        return mod(instruction);
      case MUL:
        return mul(instruction);
      case MUH:
        return muh(instruction);
      case MULU:
        return mulu(instruction);
      case MUHU:
        return muhu(instruction);
      case DIVU:
        return divu(instruction);
      case MODU:
        return modu(instruction);
      case MADD:
        return madd(instruction);
      case MADDU:
        return maddu(instruction);
      case MSUB:
        return msub(instruction);
      case MSUBU:
        return msubu(instruction);
      case MULT:
        return mult(instruction);
      case MULTU:
        return multu(instruction);
      case BEQ:
        return beq(instruction);
      case BEQC:
        return beqc(instruction);
      case BEQZALC:
        return beqzalc(instruction);
      case BNE:
        return bne(instruction);
      case BNEC:
        return bnec(instruction);
      case BNEZC:
        return bnezc(instruction);
      case BOVC:
        return bovc(instruction);
      case BNVC:
        return bnvc(instruction);
      case BEQZC:
        return beqzc(instruction);
      case BREAK:
        return break_(instruction);
      case J:
        return j(instruction);
      case JAL:
        return jal(instruction);
      case JALR:
        return jalr(instruction);
      case JIC:
        return jic(instruction);
      case JALR_HB:
        return jalr_hb(instruction);
      case JR:
        return jr(instruction);
      case JR_HB:
        return jr_hb(instruction);
      case BGEZ:
        return bgez(instruction);
      case BGTZ:
        return bgtz(instruction);
      case BITSWAP:
        return bitswap(instruction);
      case BGEZAL:
        return bgezal(instruction);
      case BLEZALC:
        return blezalc(instruction);
      case BGEZALC:
        return bgezalc(instruction);
      case BGTZALC:
        return bgtzalc(instruction);
      case BLTZALC:
        return bltzalc(instruction);
      case BNEZALC:
        return bnezalc(instruction);
      case BLEZC:
        return blezc(instruction);
      case BGEZC:
        return bgezc(instruction);
      case BGEC:
        return bgec(instruction);
      case BGTZC:
        return bgtzc(instruction);
      case BLTZC:
        return bltzc(instruction);
      case BLTC:
        return bltc(instruction);
      case BGEUC:
        return bgeuc(instruction);
      case BLTUC:
        return bltuc(instruction);
      case BLEZ:
        return blez(instruction);
      case BLTZ:
        return bltz(instruction);
      case BLTZAL:
        return bltzal(instruction);
      case JIALC:
        return jialc(instruction);
      case NAL:
        return nal(instruction);
      case SELEQZ:
        return seleqz(instruction);
      case SELNEZ:
        return selnez(instruction);
      case TEQ:
        return teq(instruction);
      case TGE:
        return tge(instruction);
      case TGEU:
        return tgeu(instruction);
      case TLT:
        return tlt(instruction);
      case TLTU:
        return tltu(instruction);
      case TNE:
        return tne(instruction);
      case LW:
        return lw(instruction);
      case LWE:
        return lwe(instruction);
      case SW:
        return sw(instruction);
      case SWE:
        return swe(instruction);
      case SWC1:
        return swc1(instruction);
      case SWC2:
        return swc2(instruction);
      case LB:
        return lb(instruction);
      case LBE:
        return lbe(instruction);
      case LBU:
        return lbu(instruction);
      case LBUE:
        return lbue(instruction);
      case LH:
        return lh(instruction);
      case LHE:
        return lhe(instruction);
      case LHU:
        return lhu(instruction);
      case LHUE:
        return lhue(instruction);
      case LSA:
        return lsa(instruction);
      case LWL:
        return lwl(instruction);
      case LWPC:
        return lwpc(instruction);
      case LWR:
        return lwr(instruction);
      case SB:
        return sb(instruction);
      case SBE:
        return sbe(instruction);
      case SH:
        return sh(instruction);
      case SHE:
        return she(instruction);
      case SWL:
        return swl(instruction);
      case SWR:
        return swr(instruction);
      case CACHE:
        return cache(instruction);
      case CACHEE:
        return cachee(instruction);
      case MFHI:
        return mfhi(instruction);
      case MFLO:
        return mflo(instruction);
      case MTHI:
        return mthi(instruction);
      case MTLO:
        return mtlo(instruction);
      case LL:
        return ll(instruction);
      case LLE:
        return lle(instruction);
      case LLWP:
        return llwp(instruction);
      case LLWPE:
        return llwpe(instruction);
      case SC:
        return sc(instruction);
      case SCE:
        return sce(instruction);
      case SCWP:
        return scwp(instruction);
      case SCWPE:
        return scwpe(instruction);
      case LDC1:
        return ldc1(instruction);
      case LDC2:
        return ldc2(instruction);
      case LWC1:
        return lwc1(instruction);
      case LWC2:
        return lwc2(instruction);
      case SDC1:
        return sdc1(instruction);
      case SDC2:
        return sdc2(instruction);
      case CFC1:
        return cfc1(instruction);
      case CFC2:
        return cfc2(instruction);
      case CTC1:
        return ctc1(instruction);
      case CTC2:
        return ctc2(instruction);
      case MFC0:
        return mfc0(instruction);
      case MFC1:
        return mfc1(instruction);
      case MFC2:
        return mfc2(instruction);
      case MFHC0:
        return mfhc0(instruction);
      case MFHC1:
        return mfhc1(instruction);
      case MFHC2:
        return mfhc2(instruction);
      case MTC0:
        return mtc0(instruction);
      case MTC1:
        return mtc1(instruction);
      case MTC2:
        return mtc2(instruction);
      case MTHC0:
        return mthc0(instruction);
      case MTHC1:
        return mthc1(instruction);
      case MTHC2:
        return mthc2(instruction);
      case PREF:
        return pref(instruction);
      case PREFE:
        return prefe(instruction);
      case RDHWR:
        return rdhwr(instruction);
      case RDPGPR:
        return rdpgpr(instruction);
      case ABS_S:
        return abs_s(instruction);
      case ABS_D:
        return abs_d(instruction);
      case ADD_S:
        return add_s(instruction);
      case ADD_D:
        return add_d(instruction);
      case CMP_AF_S:
        return cmp_af_s(instruction);
      case CMP_AF_D:
        return cmp_af_d(instruction);
      case CMP_UN_S:
        return cmp_un_s(instruction);
      case CMP_UN_D:
        return cmp_un_d(instruction);
      case CMP_EQ_S:
        return cmp_eq_s(instruction);
      case CMP_EQ_D:
        return cmp_eq_d(instruction);
      case CMP_UEQ_S:
        return cmp_ueq_s(instruction);
      case CMP_UEQ_D:
        return cmp_ueq_d(instruction);
      case CMP_LT_S:
        return cmp_lt_s(instruction);
      case CMP_LT_D:
        return cmp_lt_d(instruction);
      case CMP_ULT_S:
        return cmp_ult_s(instruction);
      case CMP_ULT_D:
        return cmp_ult_d(instruction);
      case CMP_LE_S:
        return cmp_le_s(instruction);
      case CMP_LE_D:
        return cmp_le_d(instruction);
      case CMP_ULE_S:
        return cmp_ule_s(instruction);
      case CMP_ULE_D:
        return cmp_ule_d(instruction);
      case CMP_SAF_S:
        return cmp_saf_s(instruction);
      case CMP_SAF_D:
        return cmp_saf_d(instruction);
      case CMP_SUN_S:
        return cmp_sun_s(instruction);
      case CMP_SUN_D:
        return cmp_sun_d(instruction);
      case CMP_SEQ_S:
        return cmp_seq_s(instruction);
      case CMP_SEQ_D:
        return cmp_seq_d(instruction);
      case CMP_SUEQ_S:
        return cmp_sueq_s(instruction);
      case CMP_SUEQ_D:
        return cmp_sueq_d(instruction);
      case CMP_SLT_S:
        return cmp_slt_s(instruction);
      case CMP_SLT_D:
        return cmp_slt_d(instruction);
      case CMP_SULT_S:
        return cmp_sult_s(instruction);
      case CMP_SULT_D:
        return cmp_sult_d(instruction);
      case CMP_SLE_S:
        return cmp_sle_s(instruction);
      case CMP_SLE_D:
        return cmp_sle_d(instruction);
      case CMP_SULE_S:
        return cmp_sule_s(instruction);
      case CMP_SULE_D:
        return cmp_sule_d(instruction);
      case CMP_AT_S:
        return cmp_at_s(instruction);
      case CMP_AT_D:
        return cmp_at_d(instruction);
      case CMP_OR_S:
        return cmp_or_s(instruction);
      case CMP_OR_D:
        return cmp_or_d(instruction);
      case CMP_UNE_S:
        return cmp_une_s(instruction);
      case CMP_UNE_D:
        return cmp_une_d(instruction);
      case CMP_NE_S:
        return cmp_ne_s(instruction);
      case CMP_NE_D:
        return cmp_ne_d(instruction);
      case CMP_UGE_S:
        return cmp_uge_s(instruction);
      case CMP_UGE_D:
        return cmp_uge_d(instruction);
      case CMP_OGE_S:
        return cmp_oge_s(instruction);
      case CMP_OGE_D:
        return cmp_oge_d(instruction);
      case CMP_UGT_S:
        return cmp_ugt_s(instruction);
      case CMP_UGT_D:
        return cmp_ugt_d(instruction);
      case CMP_OGT_S:
        return cmp_ogt_s(instruction);
      case CMP_OGT_D:
        return cmp_ogt_d(instruction);
      case CMP_SAT_S:
        return cmp_sat_s(instruction);
      case CMP_SAT_D:
        return cmp_sat_d(instruction);
      case CMP_SOR_S:
        return cmp_sor_s(instruction);
      case CMP_SOR_D:
        return cmp_sor_d(instruction);
      case CMP_SUNE_S:
        return cmp_sune_s(instruction);
      case CMP_SUNE_D:
        return cmp_sune_d(instruction);
      case CMP_SNE_S:
        return cmp_sne_s(instruction);
      case CMP_SNE_D:
        return cmp_sne_d(instruction);
      case CMP_SUGE_S:
        return cmp_suge_s(instruction);
      case CMP_SUGE_D:
        return cmp_suge_d(instruction);
      case CMP_SOGE_S:
        return cmp_soge_s(instruction);
      case CMP_SOGE_D:
        return cmp_soge_d(instruction);
      case CMP_SUGT_S:
        return cmp_sugt_s(instruction);
      case CMP_SUGT_D:
        return cmp_sugt_d(instruction);
      case CMP_SOGT_S:
        return cmp_sogt_s(instruction);
      case CMP_SOGT_D:
        return cmp_sogt_d(instruction);
      case CRC32B:
        return crc32b(instruction);
      case CRC32H:
        return crc32h(instruction);
      case CRC32W:
        return crc32w(instruction);
      case CRC32CB:
        return crc32cb(instruction);
      case CRC32CH:
        return crc32ch(instruction);
      case CRC32CW:
        return crc32cw(instruction);
      case DIV_S:
        return div_s(instruction);
      case DIV_D:
        return div_d(instruction);
      case MUL_S:
        return mul_s(instruction);
      case MUL_D:
        return mul_d(instruction);
      case NEG_S:
        return neg_s(instruction);
      case NEG_D:
        return neg_d(instruction);
      case SQRT_S:
        return sqrt_s(instruction);
      case SQRT_D:
        return sqrt_d(instruction);
      case SUB_S:
        return sub_s(instruction);
      case SUB_D:
        return sub_d(instruction);
      case RECIP_S:
        return recip_s(instruction);
      case RECIP_D:
        return recip_d(instruction);
      case RSQRT_S:
        return rsqrt_s(instruction);
      case RSQRT_D:
        return rsqrt_d(instruction);
      case MADDF_S:
        return maddf_s(instruction);
      case MADDF_D:
        return maddf_d(instruction);
      case MSUBF_S:
        return msubf_s(instruction);
      case MSUBF_D:
        return msubf_d(instruction);
      case CLASS_S:
        return class_s(instruction);
      case CLASS_D:
        return class_d(instruction);
      case MAX_S:
        return max_s(instruction);
      case MAX_D:
        return max_d(instruction);
      case MAXA_S:
        return maxa_s(instruction);
      case MAXA_D:
        return maxa_d(instruction);
      case MIN_S:
        return min_s(instruction);
      case MIN_D:
        return min_d(instruction);
      case MINA_S:
        return mina_s(instruction);
      case MINA_D:
        return mina_d(instruction);
      case CVT_D_S:
        return cvt_d_s(instruction);
      case CVT_D_W:
        return cvt_d_w(instruction);
      case CVT_D_L:
        return cvt_d_l(instruction);
      case CVT_L_S:
        return cvt_l_s(instruction);
      case CVT_L_D:
        return cvt_l_d(instruction);
      case CVT_S_D:
        return cvt_s_d(instruction);
      case CVT_S_W:
        return cvt_s_w(instruction);
      case CVT_S_L:
        return cvt_s_l(instruction);
      case CVT_W_S:
        return cvt_w_s(instruction);
      case CVT_W_D:
        return cvt_w_d(instruction);
      case RINT_S:
        return rint_s(instruction);
      case RINT_D:
        return rint_d(instruction);
      case CEIL_L_S:
        return ceil_l_s(instruction);
      case CEIL_L_D:
        return ceil_l_d(instruction);
      case CEIL_W_S:
        return ceil_w_s(instruction);
      case CEIL_W_D:
        return ceil_w_d(instruction);
      case FLOOR_L_S:
        return floor_l_s(instruction);
      case FLOOR_L_D:
        return floor_l_d(instruction);
      case FLOOR_W_S:
        return floor_w_s(instruction);
      case FLOOR_W_D:
        return floor_w_d(instruction);
      case ROUND_L_S:
        return round_l_s(instruction);
      case ROUND_L_D:
        return round_l_d(instruction);
      case ROUND_W_S:
        return round_w_s(instruction);
      case ROUND_W_D:
        return round_w_d(instruction);
      case TRUNC_L_S:
        return trunc_l_s(instruction);
      case TRUNC_L_D:
        return trunc_l_d(instruction);
      case TRUNC_W_S:
        return trunc_w_s(instruction);
      case TRUNC_W_D:
        return trunc_w_d(instruction);
      case MOV_S:
        return mov_s(instruction);
      case MOV_D:
        return mov_d(instruction);
      case SEL_S:
        return sel_s(instruction);
      case SEL_D:
        return sel_d(instruction);
      case SELEQZ_S:
        return seleqz_s(instruction);
      case SELEQZ_D:
        return seleqz_d(instruction);
      case SELNEZ_S:
        return selnez_s(instruction);
      case SELNEZ_D:
        return selnez_d(instruction);
      case DERET:
        return deret(instruction);
      case DI:
        return di(instruction);
      case DVP:
        return dvp(instruction);
      case EVP:
        return evp(instruction);
      case EI:
        return ei(instruction);
      case ERET:
        return eret(instruction);
      case ERETNC:
        return eretnc(instruction);
      case GINVI:
        return ginvi(instruction);
      case GINVT:
        return ginvt(instruction);
      case PAUSE:
        return pause(instruction);
      case SDBBP:
        return sdbbp(instruction);
      case SIGRIE:
        return sigrie(instruction);
      case SYSCALL:
        return syscall(instruction);
      case SYNC:
        return sync(instruction);
      case SYNCI:
        return synci(instruction);
      case TLBINV:
        return tlbinv(instruction);
      case TLBINVF:
        return tlbinvf(instruction);
      case TLBP:
        return tlbp(instruction);
      case TLBR:
        return tlbr(instruction);
      case TLBWI:
        return tlbwi(instruction);
      case TLBWR:
        return tlbwr(instruction);
      case WAIT:
        return wait(instruction);
      case WRPGPR:
        return wrpgpr(instruction);
      case COP2:
        return cop2(instruction);
    }
    return "";
  }

  private String add(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "add $" + rd + ", $" + rs + ", $" + rt;
  }

  private String addu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "addu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String addiu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "addiu $" + rt + ", $" + rs + ", " + imm;
  }

  private String addiupc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int imm = instruction & 0xffff;
    return "addiupc $" + rs + ", " + imm;
  }

  private String sub(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "sub $" + rd + ", $" + rs + ", $" + rt;
  }

  private String subu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "subu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String seb(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "seb $" + rd + ", $" + rt;
  }

  private String seh(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "seh $" + rd + ", $" + rt;
  }

  private String align(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int bp = (instruction >> 6) & 0x3;
    return "align $" + rd + ", $" + rs + ", $" + rt + ", " + bp;
  }

  private String aluipc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "aluipc $" + rs + ", " + imm;
  }

  private String clo(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "clo $" + rd + ", $" + rs;
  }

  private String clz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "clz $" + rd + ", $" + rs;
  }

  private String sll(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;
    return "sll $" + rd + ", $" + rt + ", " + sa;
  }

  private String sllv(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "sllv $" + rd + ", $" + rt + ", $" + rs;
  }

  private String rotr(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;
    return "rotr $" + rd + ", $" + rt + ", " + sa;
  }

  private String rotrv(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "rotrv $" + rd + ", $" + rt + ", $" + rs;
  }

  private String sra(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;
    return "sra $" + rd + ", $" + rt + ", " + sa;
  }

  private String srav(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "srav $" + rd + ", $" + rt + ", $" + rs;
  }

  private String srl(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;
    return "srl $" + rd + ", $" + rt + ", " + sa;
  }

  private String srlv(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "srlv $" + rd + ", $" + rt + ", $" + rs;
  }

  private String wsbh(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "wsbh $" + rd + ", $" + rt;
  }

  private String bitswap(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "bitswap $" + rd + ", $" + rt;
  }

  private String and_(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "and $" + rd + ", $" + rs + ", $" + rt;
  }

  private String andi(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "andi $" + rt + ", $" + rs + ", " + imm;
  }

  private String nor(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "nor $" + rd + ", $" + rs + ", $" + rt;
  }

  private String or_(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "or $" + rd + ", $" + rs + ", $" + rt;
  }

  private String ori(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "ori $" + rt + ", $" + rs + ", " + imm;
  }

  private String xor_(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "xor $" + rd + ", $" + rs + ", $" + rt;
  }

  private String xori(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "xori $" + rt + ", $" + rs + ", " + imm;
  }

  private String ext(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int pos = (instruction >> 6) & 0x1f;
    int size = ((instruction >> 11) & 0x1f);
    return "ext $" + rt + ", $" + rs + ", " + pos + ", " + size;
  }

  private String ins(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int pos = (instruction >> 6) & 0x1f;
    int size = ((instruction >> 11) & 0x1f) + 1;
    return "ins $" + rt + ", $" + rs + ", " + pos + ", " + size;
  }

  private String aui(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "aui $" + rt + ", $" + rs + ", " + imm;
  }

  private String auipc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "auipc $" + rs + ", " + imm;
  }

  private String movn(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "movn $" + rd + ", $" + rs + ", $" + rt;
  }

  private String movz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "movz $" + rd + ", $" + rs + ", $" + rt;
  }

  private String slt(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "slt $" + rd + ", $" + rs + ", $" + rt;
  }

  private String slti(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "slti $" + rt + ", $" + rs + ", " + imm;
  }

  private String sltiu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    return "sltiu $" + rt + ", $" + rs + ", " + imm;
  }

  private String sltu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "sltu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String div(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "div $" + rd + ", $" + rs + ", $" + rt;
  }

  private String divu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "divu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String modu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "modu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String mod(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "mod $" + rd + ", $" + rs + ", $" + rt;
  }

  private String mul(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "mul $" + rd + ", $" + rs + ", $" + rt;
  }

  private String muh(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "muh $" + rd + ", $" + rs + ", $" + rt;
  }

  private String mulu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "mulu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String muhu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "muhu $" + rd + ", $" + rs + ", $" + rt;
  }

  private String madd(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "madd $" + rs + ", $" + rt;
  }

  private String maddu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "maddu $" + rs + ", $" + rt;
  }

  private String msub(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "msub $" + rs + ", $" + rt;
  }

  private String msubu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "msubu $" + rs + ", $" + rt;
  }

  private String mult(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "mult $" + rs + ", $" + rt;
  }

  private String multu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "multu $" + rs + ", $" + rt;
  }

  private String bal(int instruction) {
    short offset = (short) (instruction & 0xffff);
    return "bal " + offset;
  }

  private String balc(int instruction) {
    int offset = instruction & 0x3ffffff;
    return "balc " + offset;
  }

  private String bc(int instruction) {
    int offset = instruction & 0x3ffffff;
    return "bc " + offset;
  }

  private String bc1eqz(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bc1eqz $f" + ft + ", " + offset;
  }

  private String bc1nez(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bc1nez $f" + ft + ", " + offset;
  }

  private String bc2eqz(int instruction) {
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bc2eqz $" + ct + ", " + offset;
  }

  private String bc2nez(int instruction) {
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bc2nez $" + ct + ", " + offset;
  }

  private String beq(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "beq $" + rs + ", $" + rt + ", " + offset;
  }

  private String beqc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "beqc $" + rs + ", $" + rt + ", " + offset;
  }

  private String beqzalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "beqzalc $" + rt + ", " + offset;
  }

  private String bne(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bne $" + rs + ", $" + rt + ", " + offset;
  }

  private String bnec(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bnec $" + rs + ", $" + rt + ", " + offset;
  }

  private String bnezc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int offset = instruction & 0x1fffff;
    return "bnezc $" + rs + ", " + offset;
  }

  private String bovc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bovc $" + rs + ", $" + rt + ", " + offset;
  }

  private String bnvc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bnvc $" + rs + ", $" + rt + ", " + offset;
  }

  private String beqzc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int offset = instruction & 0x1fffff;
    return "beqzc $" + rs + ", " + offset;
  }

  private String bgez(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgez $" + rs + ", " + offset;
  }

  private String bgtz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgtz $" + rs + ", " + offset;
  }

  private String bgezal(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgezal $" + rs + ", " + offset;
  }

  private String blezalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "blezalc $" + rt + ", " + offset;
  }

  private String bgezalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgezalc $" + rt + ", " + offset;
  }

  private String bgtzalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgtzalc $" + rt + ", " + offset;
  }

  private String bltzalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bltzalc $" + rt + ", " + offset;
  }

  private String bnezalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bnezalc $" + rt + ", " + offset;
  }

  private String blezc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "blezc $" + rt + ", " + offset;
  }

  private String bgezc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgezc $" + rt + ", " + offset;
  }

  private String bgec(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgec $" + rs + ", $" + rt + ", " + offset;
  }

  private String bgtzc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgtzc $" + rt + ", " + offset;
  }

  private String bltzc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bltzc $" + rt + ", " + offset;
  }

  private String bltc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bltc $" + rs + ", $" + rt + ", " + offset;
  }

  private String bgeuc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bgeuc $" + rs + ", $" + rt + ", " + offset;
  }

  private String bltuc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bltuc $" + rs + ", $" + rt + ", " + offset;
  }

  private String blez(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "blez $" + rs + ", " + offset;
  }

  private String bltz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bltz $" + rs + ", " + offset;
  }

  private String bltzal(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "bltzal $" + rs + ", " + offset;
  }

  private String nal(int instruction) {
    return "nal";
  }

  private String break_(int instruction) {
    return "break";
  }

  private String j(int instruction) {
    int instr_index = instruction & 0x3ffffff;
    return "j " + instr_index;
  }

  private String jal(int instruction) {
    int instr_index = instruction & 0x3ffffff;
    return "jal " + instr_index;
  }

  private String jalr(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "jalr $" + rd + ", $" + rs;
  }

  private String jic(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "jic $" + rt + ", " + offset;
  }

  private String jalr_hb(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "jalr.hb $" + rd + ", $" + rs;
  }

  private String jr(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    return "jr $" + rs;
  }

  private String jr_hb(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    return "jr.hb $" + rs;
  }

  private String jialc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "jialc $" + rt + ", " + offset;
  }

  private String seleqz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "seleqz $" + rd + ", $" + rs + ", $" + rt;
  }

  private String selnez(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "selnez $" + rd + ", $" + rs + ", $" + rt;
  }

  private String teq(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "teq $" + rs + ", $" + rt;
  }

  private String tge(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "tge $" + rs + ", $" + rt;
  }

  private String tgeu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "tgeu $" + rs + ", $" + rt;
  }

  private String tlt(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "tlt $" + rs + ", $" + rt;
  }

  private String tltu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "tltu $" + rs + ", $" + rt;
  }

  private String tne(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "tne $" + rs + ", $" + rt;
  }

  private String lw(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lw $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lwe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "lwe $" + rt + ", " + offset + "($" + base + ")";
  }

  private String sw(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "sw $" + rt + ", " + offset + "($" + base + ")";
  }

  private String swe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "swe $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lb(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lb $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lbe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "lbe $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lbu(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lbu $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lbue(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "lbue $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lh(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lh $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lhe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "lhe $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lhu(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lhu $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lhue(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "lhue $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lsa(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = ((instruction >> 6) & 0x3) + 1;
    return "lsa $" + rd + ", $" + rs + ", $" + rt + ", " + sa;
  }

  private String lwl(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lwl $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lwpc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int offset = instruction & 0x7ffff;
    return "lwpc $" + rs + ", " + offset;
  }

  private String lwr(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lwr $" + rt + ", " + offset + "($" + base + ")";
  }

  private String sb(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "sb $" + rt + ", " + offset + "($" + base + ")";
  }

  private String sbe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "sbe $" + rt + ", " + offset + "($" + base + ")";
  }

  private String sh(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "sh $" + rt + ", " + offset + "($" + base + ")";
  }

  private String she(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "she $" + rt + ", " + offset + "($" + base + ")";
  }

  private String swl(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "swl $" + rt + ", " + offset + "($" + base + ")";
  }

  private String swr(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "swr $" + rt + ", " + offset + "($" + base + ")";
  }

  private String cache(int instruction) {

    return "cache";
  }

  private String cachee(int instruction) {

    return "cachee";
  }

  private String mfhi(int instruction) {
    int rd = (instruction >> 11) & 0x1f;
    return "mfhi $" + rd;
  }

  private String mflo(int instruction) {
    int rd = (instruction >> 11) & 0x1f;
    return "mflo $" + rd;
  }

  private String mthi(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    return "mthi $" + rs;
  }

  private String mtlo(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    return "mtlo $" + rs;
  }

  private String ll(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "ll $" + rt + ", " + offset + "($" + base + ")";
  }

  private String lle(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "lle $" + rt + ", " + offset + "($" + base + ")";
  }

  private String llwp(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "llwp $" + rt + ", $" + rd + ", ($" + base + ")";
  }

  private String llwpe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "llwpe $" + rt + ", $" + rd + ", ($" + base + ")";
  }

  private String sc(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "sc $" + rt + ", " + offset + "($" + base + ")";
  }

  private String sce(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);
    return "sce $" + rt + ", " + offset + "($" + base + ")";
  }

  private String scwp(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "scwp $" + rt + ", $" + rd + ", ($" + base + ")";
  }

  private String scwpe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "scwpe $" + rt + ", $" + rd + ", ($" + base + ")";
  }

  private String pref(int instruction) {

    return "pref";
  }

  private String prefe(int instruction) {

    return "prefe";
  }

  private String rdhwr(int instruction) {

    return "rdhwr";
  }

  private String rdpgpr(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "rdpgpr $" + rd + ", $" + rt;
  }

  private String deret(int instruction) {

    return "deret";
  }

  private String di(int instruction) {

    return "di";
  }

  private String dvp(int instruction) {

    return "dvp";
  }

  private String evp(int instruction) {

    return "evp";
  }

  private String ei(int instruction) {

    return "ei";
  }

  private String eret(int instruction) {

    return "eret";
  }

  private String eretnc(int instruction) {

    return "eretnc";
  }

  private String ginvi(int instruction) {

    return "ginvi";
  }

  private String ginvt(int instruction) {

    return "ginvt";
  }

  private String pause(int instruction) {

    return "pause";
  }

  private String sdbbp(int instruction) {
    return "sdbbp";
  }

  private String sigrie(int instruction) {
    return "sigrie";
  }

  private String syscall(int instruction) {
    return "syscall";
  }

  private String sync(int instruction) {

    return "sync";
  }

  private String synci(int instruction) {

    return "synci";
  }

  private String tlbinv(int instruction) {

    return "tlbinv";
  }

  private String tlbinvf(int instruction) {

    return "tlbinvf";
  }

  private String tlbp(int instruction) {

    return "tlbp";
  }

  private String tlbr(int instruction) {

    return "tlbr";
  }

  private String tlbwi(int instruction) {

    return "tlbwi";
  }

  private String tlbwr(int instruction) {

    return "tlbwr";
  }

  private String wait(int instruction) {

    return "wait";
  }

  private String wrpgpr(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    return "wrpgpr $" + rd + ", $" + rt;
  }

  private String cop2(int instruction) {

    return "cop2";
  }

  private String swc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "swc1 $f" + ft + ", " + offset + "($" + base + ")";
  }

  private String swc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);
    return "swc2 $" + ct + ", " + offset + "($" + base + ")";
  }

  private String ldc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "ldc1 $f" + ft + ", " + offset + "($" + base + ")";
  }

  private String ldc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);
    return "ldc2 $" + ct + ", " + offset + "($" + base + ")";
  }

  private String lwc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "lwc1 $f" + ft + ", " + offset + "($" + base + ")";
  }

  private String lwc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);
    return "lwc2 $" + ct + ", " + offset + "($" + base + ")";
  }

  private String sdc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);
    return "sdc1 $f" + ft + ", " + offset + "($" + base + ")";
  }

  private String sdc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);
    return "sdc2 $" + ct + ", " + offset + "($" + base + ")";
  }

  private String cfc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    return "cfc1 $" + rt + ", $f" + fs;
  }

  private String cfc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;
    return "cfc2 $" + rt + ", $" + cs;
  }

  private String ctc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    return "ctc1 $" + rt + ", $f" + fs;
  }

  private String ctc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;
    return "ctc2 $" + rt + ", $" + cs;
  }

  private String mfc0(int instruction) {

    return "mfc0";
  }

  private String mfc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    return "mfc1 $" + rt + ", $f" + fs;
  }

  private String mfc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;
    return "mfc2 $" + rt + ", $" + cs;
  }

  private String mfhc0(int instruction) {

    return "mfhc0";
  }

  private String mfhc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    return "mfhc1 $" + rt + ", $f" + fs;
  }

  private String mfhc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;
    return "mfhc2 $" + rt + ", $" + cs;
  }

  private String mtc0(int instruction) {
    return "mtc0";
  }

  private String mtc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    return "mtc1 $" + rt + ", $f" + fs;
  }

  private String mtc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;
    return "mtc2 $" + rt + ", $" + cs;
  }

  private String mthc0(int instruction) {

    return "mthc0";
  }

  private String mthc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    return "mthc1 $" + rt + ", $f" + fs;
  }

  private String mthc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;
    return "mthc2 $" + rt + ", $" + cs;
  }

  private String abs_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "abs.s $f" + fd + ", $f" + fs;
  }

  private String abs_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "abs.d $f" + fd + ", $f" + fs;
  }

  private String add_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "add.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String add_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "add.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String div_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "div.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String div_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "div.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String mul_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "mul.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String mul_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "mul.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String neg_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "neg.s $f" + fd + ", $f" + fs;
  }

  private String neg_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "neg.d $f" + fd + ", $f" + fs;
  }

  private String sqrt_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "sqrt.s $f" + fd + ", $f" + fs;
  }

  private String sqrt_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "sqrt.d $f" + fd + ", $f" + fs;
  }

  private String sub_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "sub.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String sub_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "sub.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String recip_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "recip.s $f" + fd + ", $f" + fs;
  }

  private String recip_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "recip.d $f" + fd + ", $f" + fs;
  }

  private String rsqrt_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "rsqrt.s $f" + fd + ", $f" + fs;
  }

  private String rsqrt_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "rsqrt.d $f" + fd + ", $f" + fs;
  }

  private String maddf_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "maddf.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String maddf_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "maddf.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String msubf_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "msubf.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String msubf_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "msubf.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String class_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "class.s $f" + fd + ", $f" + fs;
  }

  private String class_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "class.d $f" + fd + ", $f" + fs;
  }

  private String max_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "max.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String max_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "max.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String maxa_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "maxa.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String maxa_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "maxa.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String min_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "min.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String min_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "min.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String mina_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "mina.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String mina_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "mina.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String rint_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "rint.s $f" + fd + ", $f" + fs;
  }

  private String rint_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "rint.d $f" + fd + ", $f" + fs;
  }

  private String mov_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "mov.s $f" + fd + ", $f" + fs;
  }

  private String mov_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "mov.d $f" + fd + ", $f" + fs;
  }

  private String sel_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "sel.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String sel_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "sel.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String seleqz_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "seleqz.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String seleqz_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "seleqz.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String selnez_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "selnez.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String selnez_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "selnez.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_af_s(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.af.s $f" + fd;
  }

  private String cmp_af_d(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.af.d $f" + fd;
  }

  private String cmp_un_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.un.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_un_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.un.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_eq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.eq.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_eq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.eq.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ueq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ueq.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ueq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ueq.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_lt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.lt.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_lt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.lt.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ult_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ult.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ult_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ult.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_le_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.le.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_le_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.le.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ule_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ule.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ule_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ule.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_saf_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.saf.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_saf_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.saf.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sun_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sun.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sun_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sun.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_seq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.seq.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_seq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.seq.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sueq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sueq.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sueq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sueq.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_slt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.slt.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_slt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.slt.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sult_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sult.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sult_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sult.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sle_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sle.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sle_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sle.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sule_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sule.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sule_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sule.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_at_s(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.at.s $f" + fd;
  }

  private String cmp_at_d(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.at.d $f" + fd;
  }

  private String cmp_or_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.or.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_or_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.or.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_une_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.une.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_une_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.une.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ne_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ne.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ne_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ne.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_uge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.uge.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_uge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.uge.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_oge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.oge.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_oge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.oge.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ugt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ugt.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ugt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ugt.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ogt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ogt.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_ogt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.ogt.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sat_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sat.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sat_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sat.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sor_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sor.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sor_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sor.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sune_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sune.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sune_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sune.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sne_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sne.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sne_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sne.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_suge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.suge.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_suge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.suge.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_soge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.soge.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_soge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.soge.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sugt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sugt.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sugt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sugt.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sogt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sogt.s $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String cmp_sogt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cmp.sogt.d $f" + fd + ", $f" + fs + ", $f" + ft;
  }

  private String crc32b(int instruction) {
    int rd = (instruction >> 21) & 0x1f;
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "crc32b $" + rd + ", $" + rs + ", $" + rt;
  }

  private String crc32h(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "crc32h $" + rs + ", $" + rt;
  }

  private String crc32w(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "crc32w $" + rs + ", $" + rt;
  }

  private String crc32cb(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "crc32cb $" + rs + ", $" + rt;
  }

  private String crc32ch(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "crc32ch $" + rs + ", $" + rt;
  }

  private String crc32cw(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    return "crc32cw $" + rs + ", $" + rt;
  }

  private String cvt_d_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.d.s $f" + fd + ", $f" + fs;
  }

  private String cvt_d_w(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.d.w $f" + fd + ", $f" + fs;
  }

  private String cvt_d_l(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.d.l $f" + fd + ", $f" + fs;
  }

  private String cvt_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.l.s $f" + fd + ", $f" + fs;
  }

  private String cvt_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.l.d $f" + fd + ", $f" + fs;
  }

  private String cvt_s_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.s.d $f" + fd + ", $f" + fs;
  }

  private String cvt_s_w(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.s.w $f" + fd + ", $f" + fs;
  }

  private String cvt_s_l(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.s.l $f" + fd + ", $f" + fs;
  }

  private String cvt_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.w.s $f" + fd + ", $f" + fs;
  }

  private String cvt_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "cvt.w.d $f" + fd + ", $f" + fs;
  }

  private String ceil_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "ceil.l.s $f" + fd + ", $f" + fs;
  }

  private String ceil_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "ceil.l.d $f" + fd + ", $f" + fs;
  }

  private String ceil_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "ceil.w.s $f" + fd + ", $f" + fs;
  }

  private String ceil_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "ceil.w.d $f" + fd + ", $f" + fs;
  }

  private String floor_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "floor.l.s $f" + fd + ", $f" + fs;
  }

  private String floor_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "floor.l.d $f" + fd + ", $f" + fs;
  }

  private String floor_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "floor.w.s $f" + fd + ", $f" + fs;
  }

  private String floor_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "floor.w.d $f" + fd + ", $f" + fs;
  }

  private String round_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "round.l.s $f" + fd + ", $f" + fs;
  }

  private String round_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "round.l.d $f" + fd + ", $f" + fs;
  }

  private String round_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "round.w.s $f" + fd + ", $f" + fs;
  }

  private String round_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "round.w.d $f" + fd + ", $f" + fs;
  }

  private String trunc_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "trunc.l.s $f" + fd + ", $f" + fs;
  }

  private String trunc_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "trunc.l.d $f" + fd + ", $f" + fs;
  }

  private String trunc_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "trunc.w.s $f" + fd + ", $f" + fs;
  }

  private String trunc_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;
    return "trunc.w.d $f" + fd + ", $f" + fs;
  }
}
