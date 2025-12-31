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

package com.cleverchuk.mips.simulator.binary;

import com.cleverchuk.mips.simulator.mem.Memory;
import com.cleverchuk.mips.simulator.registers.Cop2ControlRegisterFileArray;
import com.cleverchuk.mips.simulator.registers.Cop2RegisterFileArray;
import com.cleverchuk.mips.simulator.registers.FpcRegisterFileArray;
import com.cleverchuk.mips.simulator.registers.FpuRegisterFileArray;
import com.cleverchuk.mips.simulator.registers.GprRegisterFileArray;
import com.cleverchuk.mips.simulator.registers.ShadowRegisterFileArray;

public class CentralProcessor {

  private final Memory memory;

  private final FpuRegisterFileArray fpuRegisterFileArray = new FpuRegisterFileArray();

  private final GprRegisterFileArray gprFileArray = new GprRegisterFileArray();

  private final Cop2RegisterFileArray cop2RegisterFileArray = new Cop2RegisterFileArray();

  private final ShadowRegisterFileArray shadowRegisterFileArray = new ShadowRegisterFileArray();

  private final Cop2ControlRegisterFileArray cop2ControlRegisterFileArray =
      new Cop2ControlRegisterFileArray();

  private final FpcRegisterFileArray fpcRegisterFileArray = new FpcRegisterFileArray();

  private int pc;

  private int hi = 0;

  private int lo = 0;

  private byte copBit = 2; // 10 = enabled

  public CentralProcessor(Memory memory, int pc, int sp, byte copBit) {
    this.memory = memory;
    this.pc = pc;
    this.copBit = copBit;

    gprFileArray.getFile(29).writeWord(sp);
  }

  public int getPc() {
    return pc;
  }

  public void setPc(int pc) {
    this.pc = pc;
  }

  public int getHi() {
    return hi;
  }

  public int getLo() {
    return lo;
  }

  public FpuRegisterFileArray getFpuRegisterFileArray() {
    return fpuRegisterFileArray;
  }

  public GprRegisterFileArray getGprFileArray() {
    return gprFileArray;
  }

  public Cop2RegisterFileArray getCop2RegisterFileArray() {
    return cop2RegisterFileArray;
  }

  public void execute() throws Exception {
    int instruction = memory.readWord(pc);
    if (isCop(instruction)) {
      if ((copBit & 2) != 2) {
        return;
      }
    }

    pc += 4;
    Opcode opcode = InstructionDecoder.decode(instruction);
    if (opcode == null) {
      throw new UnpredictableException("Unknown opcode: " + instruction);
    }

    switch (opcode) {
      case ADD:
        add(instruction);
        break;
      case ADDU:
        addu(instruction);
        break;
      case ADDIU:
        addiu(instruction);
        break;
      case ADDIUPC:
        addiupc(instruction);
        break;
      case ALIGN:
        align(instruction);
        break;
      case ALUIPC:
        aluipc(instruction);
        break;
      case CLO:
        clo(instruction);
        break;
      case CLZ:
        clz(instruction);
        break;
      case SUB:
        sub(instruction);
        break;
      case SUBU:
        subu(instruction);
        break;
      case SEB:
        seb(instruction);
        break;
      case SEH:
        seh(instruction);
        break;
      case SLL:
        sll(instruction);
        break;
      case SLLV:
        sllv(instruction);
        break;
      case ROTR:
        rotr(instruction);
        break;
      case ROTRV:
        rotrv(instruction);
        break;
      case SRA:
        sra(instruction);
        break;
      case SRAV:
        srav(instruction);
        break;
      case SRL:
        srl(instruction);
        break;
      case SRLV:
        srlv(instruction);
        break;
      case AND:
        and_(instruction); // 'and' is often a keyword/reserved
        break;
      case ANDI:
        andi(instruction);
        break;
      case AUI:
        aui(instruction);
        break;
      case AUIPC:
        auipc(instruction);
        break;
      case BAL:
        bal(instruction);
        break;
      case BALC:
        balc(instruction);
        break;
      case BC:
        bc(instruction);
        break;
      case BC1EQZ:
        bc1eqz(instruction);
        break;
      case BC1NEZ:
        bc1nez(instruction);
        break;
      case BC2EQZ:
        bc2eqz(instruction);
        break;
      case BC2NEZ:
        bc2nez(instruction);
        break;
      case EXT:
        ext(instruction);
        break;
      case INS:
        ins(instruction);
        break;
      case NOR:
        nor(instruction);
        break;
      case OR:
        or_(instruction); // 'or' is often a keyword/reserved
        break;
      case ORI:
        ori(instruction);
        break;
      case XOR:
        xor_(instruction); // 'xor' is often a keyword/reserved
        break;
      case XORI:
        xori(instruction);
        break;
      case WSBH:
        wsbh(instruction);
        break;
      case MOVN:
        movn(instruction);
        break;
      case MOVZ:
        movz(instruction);
        break;
      case SLT:
        slt(instruction);
        break;
      case SLTI:
        slti(instruction);
        break;
      case SLTIU:
        sltiu(instruction);
        break;
      case SLTU:
        sltu(instruction);
        break;
      case DIV:
        div(instruction);
        break;
      case MOD:
        mod(instruction);
        break;
      case MUL:
        mul(instruction);
        break;
      case MUH:
        muh(instruction);
        break;
      case MULU:
        mulu(instruction);
        break;
      case MUHU:
        muhu(instruction);
        break;
      case DIVU:
        divu(instruction);
        break;
      case MODU:
        modu(instruction);
        break;
      case MADD:
        madd(instruction);
        break;
      case MADDU:
        maddu(instruction);
        break;
      case MSUB:
        msub(instruction);
        break;
      case MSUBU:
        msubu(instruction);
        break;
      case MULT:
        mult(instruction);
        break;
      case MULTU:
        multu(instruction);
        break;
      case BEQ:
        beq(instruction);
        break;
      case BEQC:
        beqc(instruction);
        break;
      case BEQZALC:
        beqzalc(instruction);
        break;
      case BNE:
        bne(instruction);
        break;
      case BNEC:
        bnec(instruction);
        break;
      case BNEZC:
        bnezc(instruction);
        break;
      case BOVC:
        bovc(instruction);
        break;
      case BNVC:
        bnvc(instruction);
        break;
      case BEQZC:
        beqzc(instruction);
        break;
      case BREAK:
        break_(instruction); // 'break' is a keyword
        break;
      case J:
        j(instruction);
        break;
      case JAL:
        jal(instruction);
        break;
      case JALR:
        jalr(instruction);
        break;
      case JIC:
        jic(instruction);
        break;
      case JALR_HB:
        jalr_hb(instruction);
        break;
      case JR:
        jr(instruction);
        break;
      case JR_HB:
        jr_hb(instruction);
        break;
      case BGEZ:
        bgez(instruction);
        break;
      case BGTZ:
        bgtz(instruction);
        break;
      case BITSWAP:
        bitswap(instruction);
        break;
      case BGEZAL:
        bgezal(instruction);
        break;
      case BLEZALC:
        blezalc(instruction);
        break;
      case BGEZALC:
        bgezalc(instruction);
        break;
      case BGTZALC:
        bgtzalc(instruction);
        break;
      case BLTZALC:
        bltzalc(instruction);
        break;
      case BNEZALC:
        bnezalc(instruction);
        break;
      case BLEZC:
        blezc(instruction);
        break;
      case BGEZC:
        bgezc(instruction);
        break;
      case BGEC:
        bgec(instruction);
        break;
      case BGTZC:
        bgtzc(instruction);
        break;
      case BLTZC:
        bltzc(instruction);
        break;
      case BLTC:
        bltc(instruction);
        break;
      case BGEUC:
        bgeuc(instruction);
        break;
      case BLTUC:
        bltuc(instruction);
        break;
      case BLEZ:
        blez(instruction);
        break;
      case BLTZ:
        bltz(instruction);
        break;
      case BLTZAL:
        bltzal(instruction);
        break;
      case JIALC:
        jialc(instruction);
        break;
      case NAL:
        nal(instruction);
        break;
      case SELEQZ:
        seleqz(instruction);
        break;
      case SELNEZ:
        selnez(instruction);
        break;
      case TEQ:
        teq(instruction);
        break;
      case TGE:
        tge(instruction);
        break;
      case TGEU:
        tgeu(instruction);
        break;
      case TLT:
        tlt(instruction);
        break;
      case TLTU:
        tltu(instruction);
        break;
      case TNE:
        tne(instruction);
        break;
      case LW:
        lw(instruction);
        break;
      case LWE:
        lwe(instruction);
        break;
      case SW:
        sw(instruction);
        break;
      case SWE:
        swe(instruction);
        break;
      case SWC1:
        swc1(instruction);
        break;
      case SWC2:
        swc2(instruction);
        break;
      case LB:
        lb(instruction);
        break;
      case LBE:
        lbe(instruction);
        break;
      case LBU:
        lbu(instruction);
        break;
      case LBUE:
        lbue(instruction);
        break;
      case LH:
        lh(instruction);
        break;
      case LHE:
        lhe(instruction);
        break;
      case LHU:
        lhu(instruction);
        break;
      case LHUE:
        lhue(instruction);
        break;
      case LSA:
        lsa(instruction);
        break;
      case LWL:
        lwl(instruction);
        break;
      case LWPC:
        lwpc(instruction);
        break;
      case LWR:
        lwr(instruction);
        break;
      case SB:
        sb(instruction);
        break;
      case SBE:
        sbe(instruction);
        break;
      case SH:
        sh(instruction);
        break;
      case SHE:
        she(instruction);
        break;
      case SWL:
        swl(instruction);
        break;
      case SWR:
        swr(instruction);
        break;
      case CACHE:
        cache(instruction);
        break;
      case CACHEE:
        cachee(instruction);
        break;
      case MFHI:
        mfhi(instruction);
        break;
      case MFLO:
        mflo(instruction);
        break;
      case MTHI:
        mthi(instruction);
        break;
      case MTLO:
        mtlo(instruction);
        break;
      case LL:
        ll(instruction);
        break;
      case LLE:
        lle(instruction);
        break;
      case LLWP:
        llwp(instruction);
        break;
      case LLWPE:
        llwpe(instruction);
        break;
      case SC:
        sc(instruction);
        break;
      case SCE:
        sce(instruction);
        break;
      case SCWP:
        scwp(instruction);
        break;
      case SCWPE:
        scwpe(instruction);
        break;
      case LDC1:
        ldc1(instruction);
        break;
      case LDC2:
        ldc2(instruction);
        break;
      case LWC1:
        lwc1(instruction);
        break;
      case LWC2:
        lwc2(instruction);
        break;
      case SDC1:
        sdc1(instruction);
        break;
      case SDC2:
        sdc2(instruction);
        break;
      case CFC1:
        cfc1(instruction);
        break;
      case CFC2:
        cfc2(instruction);
        break;
      case CTC1:
        ctc1(instruction);
        break;
      case CTC2:
        ctc2(instruction);
        break;
      case MFC0:
        mfc0(instruction);
        break;
      case MFC1:
        mfc1(instruction);
        break;
      case MFC2:
        mfc2(instruction);
        break;
      case MFHC0:
        mfhc0(instruction);
        break;
      case MFHC1:
        mfhc1(instruction);
        break;
      case MFHC2:
        mfhc2(instruction);
        break;
      case MTC0:
        mtc0(instruction);
        break;
      case MTC1:
        mtc1(instruction);
        break;
      case MTC2:
        mtc2(instruction);
        break;
      case MTHC0:
        mthc0(instruction);
        break;
      case MTHC1:
        mthc1(instruction);
        break;
      case MTHC2:
        mthc2(instruction);
        break;
      case PREF:
        pref(instruction);
        break;
      case PREFE:
        prefe(instruction);
        break;
      case RDHWR:
        rdhwr(instruction);
        break;
      case RDPGPR:
        rdpgpr(instruction);
        break;
      case ABS_S:
        abs_s(instruction);
        break;
      case ABS_D:
        abs_d(instruction);
        break;
      case ADD_S:
        add_s(instruction);
        break;
      case ADD_D:
        add_d(instruction);
        break;
      case CMP_AF_S:
        cmp_af_s(instruction);
        break;
      case CMP_AF_D:
        cmp_af_d(instruction);
        break;
      case CMP_UN_S:
        cmp_un_s(instruction);
        break;
      case CMP_UN_D:
        cmp_un_d(instruction);
        break;
      case CMP_EQ_S:
        cmp_eq_s(instruction);
        break;
      case CMP_EQ_D:
        cmp_eq_d(instruction);
        break;
      case CMP_UEQ_S:
        cmp_ueq_s(instruction);
        break;
      case CMP_UEQ_D:
        cmp_ueq_d(instruction);
        break;
      case CMP_LT_S:
        cmp_lt_s(instruction);
        break;
      case CMP_LT_D:
        cmp_lt_d(instruction);
        break;
      case CMP_ULT_S:
        cmp_ult_s(instruction);
        break;
      case CMP_ULT_D:
        cmp_ult_d(instruction);
        break;
      case CMP_LE_S:
        cmp_le_s(instruction);
        break;
      case CMP_LE_D:
        cmp_le_d(instruction);
        break;
      case CMP_ULE_S:
        cmp_ule_s(instruction);
        break;
      case CMP_ULE_D:
        cmp_ule_d(instruction);
        break;
      case CMP_SAF_S:
        cmp_saf_s(instruction);
        break;
      case CMP_SAF_D:
        cmp_saf_d(instruction);
        break;
      case CMP_SUN_S:
        cmp_sun_s(instruction);
        break;
      case CMP_SUN_D:
        cmp_sun_d(instruction);
        break;
      case CMP_SEQ_S:
        cmp_seq_s(instruction);
        break;
      case CMP_SEQ_D:
        cmp_seq_d(instruction);
        break;
      case CMP_SUEQ_S:
        cmp_sueq_s(instruction);
        break;
      case CMP_SUEQ_D:
        cmp_sueq_d(instruction);
        break;
      case CMP_SLT_S:
        cmp_slt_s(instruction);
        break;
      case CMP_SLT_D:
        cmp_slt_d(instruction);
        break;
      case CMP_SULT_S:
        cmp_sult_s(instruction);
        break;
      case CMP_SULT_D:
        cmp_sult_d(instruction);
        break;
      case CMP_SLE_S:
        cmp_sle_s(instruction);
        break;
      case CMP_SLE_D:
        cmp_sle_d(instruction);
        break;
      case CMP_SULE_S:
        cmp_sule_s(instruction);
        break;
      case CMP_SULE_D:
        cmp_sule_d(instruction);
        break;
      case CMP_AT_S:
        cmp_at_s(instruction);
        break;
      case CMP_AT_D:
        cmp_at_d(instruction);
        break;
      case CMP_OR_S:
        cmp_or_s(instruction);
        break;
      case CMP_OR_D:
        cmp_or_d(instruction);
        break;
      case CMP_UNE_S:
        cmp_une_s(instruction);
        break;
      case CMP_UNE_D:
        cmp_une_d(instruction);
        break;
      case CMP_NE_S:
        cmp_ne_s(instruction);
        break;
      case CMP_NE_D:
        cmp_ne_d(instruction);
        break;
      case CMP_UGE_S:
        cmp_uge_s(instruction);
        break;
      case CMP_UGE_D:
        cmp_uge_d(instruction);
        break;
      case CMP_OGE_S:
        cmp_oge_s(instruction);
        break;
      case CMP_OGE_D:
        cmp_oge_d(instruction);
        break;
      case CMP_UGT_S:
        cmp_ugt_s(instruction);
        break;
      case CMP_UGT_D:
        cmp_ugt_d(instruction);
        break;
      case CMP_OGT_S:
        cmp_ogt_s(instruction);
        break;
      case CMP_OGT_D:
        cmp_ogt_d(instruction);
        break;
      case CMP_SAT_S:
        cmp_sat_s(instruction);
        break;
      case CMP_SAT_D:
        cmp_sat_d(instruction);
        break;
      case CMP_SOR_S:
        cmp_sor_s(instruction);
        break;
      case CMP_SOR_D:
        cmp_sor_d(instruction);
        break;
      case CMP_SUNE_S:
        cmp_sune_s(instruction);
        break;
      case CMP_SUNE_D:
        cmp_sune_d(instruction);
        break;
      case CMP_SNE_S:
        cmp_sne_s(instruction);
        break;
      case CMP_SNE_D:
        cmp_sne_d(instruction);
        break;
      case CMP_SUGE_S:
        cmp_suge_s(instruction);
        break;
      case CMP_SUGE_D:
        cmp_suge_d(instruction);
        break;
      case CMP_SOGE_S:
        cmp_soge_s(instruction);
        break;
      case CMP_SOGE_D:
        cmp_soge_d(instruction);
        break;
      case CMP_SUGT_S:
        cmp_sugt_s(instruction);
        break;
      case CMP_SUGT_D:
        cmp_sugt_d(instruction);
        break;
      case CMP_SOGT_S:
        cmp_sogt_s(instruction);
        break;
      case CMP_SOGT_D:
        cmp_sogt_d(instruction);
        break;
      case CRC32B:
        crc32b(instruction);
        break;
      case CRC32H:
        crc32h(instruction);
        break;
      case CRC32W:
        crc32w(instruction);
        break;
      case CRC32CB:
        crc32cb(instruction);
        break;
      case CRC32CH:
        crc32ch(instruction);
        break;
      case CRC32CW:
        crc32cw(instruction);
        break;
      case DIV_S:
        div_s(instruction);
        break;
      case DIV_D:
        div_d(instruction);
        break;
      case MUL_S:
        mul_s(instruction);
        break;
      case MUL_D:
        mul_d(instruction);
        break;
      case NEG_S:
        neg_s(instruction);
        break;
      case NEG_D:
        neg_d(instruction);
        break;
      case SQRT_S:
        sqrt_s(instruction);
        break;
      case SQRT_D:
        sqrt_d(instruction);
        break;
      case SUB_S:
        sub_s(instruction);
        break;
      case SUB_D:
        sub_d(instruction);
        break;
      case RECIP_S:
        recip_s(instruction);
        break;
      case RECIP_D:
        recip_d(instruction);
        break;
      case RSQRT_S:
        rsqrt_s(instruction);
        break;
      case RSQRT_D:
        rsqrt_d(instruction);
        break;
      case MADDF_S:
        maddf_s(instruction);
        break;
      case MADDF_D:
        maddf_d(instruction);
        break;
      case MSUBF_S:
        msubf_s(instruction);
        break;
      case MSUBF_D:
        msubf_d(instruction);
        break;
      case CLASS_S:
        class_s(instruction);
        break;
      case CLASS_D:
        class_d(instruction);
        break;
      case MAX_S:
        max_s(instruction);
        break;
      case MAX_D:
        max_d(instruction);
        break;
      case MAXA_S:
        maxa_s(instruction);
        break;
      case MAXA_D:
        maxa_d(instruction);
        break;
      case MIN_S:
        min_s(instruction);
        break;
      case MIN_D:
        min_d(instruction);
        break;
      case MINA_S:
        mina_s(instruction);
        break;
      case MINA_D:
        mina_d(instruction);
        break;
      case CVT_D_S:
        cvt_d_s(instruction);
        break;
      case CVT_D_W:
        cvt_d_w(instruction);
        break;
      case CVT_D_L:
        cvt_d_l(instruction);
        break;
      case CVT_L_S:
        cvt_l_s(instruction);
        break;
      case CVT_L_D:
        cvt_l_d(instruction);
        break;
      case CVT_S_D:
        cvt_s_d(instruction);
        break;
      case CVT_S_W:
        cvt_s_w(instruction);
        break;
      case CVT_S_L:
        cvt_s_l(instruction);
        break;
      case CVT_W_S:
        cvt_w_s(instruction);
        break;
      case CVT_W_D:
        cvt_w_d(instruction);
        break;
      case RINT_S:
        rint_s(instruction);
        break;
      case RINT_D:
        rint_d(instruction);
        break;
      case CEIL_L_S:
        ceil_l_s(instruction);
        break;
      case CEIL_L_D:
        ceil_l_d(instruction);
        break;
      case CEIL_W_S:
        ceil_w_s(instruction);
        break;
      case CEIL_W_D:
        ceil_w_d(instruction);
        break;
      case FLOOR_L_S:
        floor_l_s(instruction);
        break;
      case FLOOR_L_D:
        floor_l_d(instruction);
        break;
      case FLOOR_W_S:
        floor_w_s(instruction);
        break;
      case FLOOR_W_D:
        floor_w_d(instruction);
        break;
      case ROUND_L_S:
        round_l_s(instruction);
        break;
      case ROUND_L_D:
        round_l_d(instruction);
        break;
      case ROUND_W_S:
        round_w_s(instruction);
        break;
      case ROUND_W_D:
        round_w_d(instruction);
        break;
      case TRUNC_L_S:
        trunc_l_s(instruction);
        break;
      case TRUNC_L_D:
        trunc_l_d(instruction);
        break;
      case TRUNC_W_S:
        trunc_w_s(instruction);
        break;
      case TRUNC_W_D:
        trunc_w_d(instruction);
        break;
      case MOV_S:
        mov_s(instruction);
        break;
      case MOV_D:
        mov_d(instruction);
        break;
      case SEL_S:
        sel_s(instruction);
        break;
      case SEL_D:
        sel_d(instruction);
        break;
      case SELEQZ_S:
        seleqz_s(instruction);
        break;
      case SELEQZ_D:
        seleqz_d(instruction);
        break;
      case SELNEZ_S:
        selnez_s(instruction);
        break;
      case SELNEZ_D:
        selnez_d(instruction);
        break;
      case DERET:
        deret(instruction);
        break;
      case DI:
        di(instruction);
        break;
      case DVP:
        dvp(instruction);
        break;
      case EVP:
        evp(instruction);
        break;
      case EI:
        ei(instruction);
        break;
      case ERET:
        eret(instruction);
        break;
      case ERETNC:
        eretnc(instruction);
        break;
      case GINVI:
        ginvi(instruction);
        break;
      case GINVT:
        ginvt(instruction);
        break;
      case PAUSE:
        pause(instruction);
        break;
      case SDBBP:
        sdbbp(instruction);
        break;
      case SIGRIE:
        sigrie(instruction);
        break;
      case SYSCALL:
        syscall(instruction);
        break;
      case SYNC:
        sync(instruction);
        break;
      case SYNCI:
        synci(instruction);
        break;
      case TLBINV:
        tlbinv(instruction);
        break;
      case TLBINVF:
        tlbinvf(instruction);
        break;
      case TLBP:
        tlbp(instruction);
        break;
      case TLBR:
        tlbr(instruction);
        break;
      case TLBWI:
        tlbwi(instruction);
        break;
      case TLBWR:
        tlbwr(instruction);
        break;
      case WAIT:
        wait(instruction);
        break;
      case WRPGPR:
        wrpgpr(instruction);
        break;
      case COP2:
        cop2(instruction);
        break;
    }
  }

  private boolean isCop(int instruction) {
    return ((instruction >> 26) & 17) == 17;
  }

  private void add(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source + target);
  }

  private void addu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    gprFileArray.getFile(rd).writeWord((int) (source + target));
  }

  private void addiu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rt).writeWord(source + imm);
  }

  private void addiupc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int imm = signExtend(instruction & 0xffff, 16) << 2;

    gprFileArray.getFile(rs).writeWord(pc - 4 + imm);
  }

  private void sub(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(Math.subtractExact(source, target));
  }

  private void subu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source - target);
  }

  private void seb(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(signExtend(extractBits(target, 0x0, 0x8), 8));
  }

  private void seh(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(signExtend(extractBits(target, 0x0, 0x10), 0x10));
  }

  private void align(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int bp = (instruction >> 6) & 0x3;
    int source = gprFileArray.getFile(rs).readWord() >> (8 * (4 - bp));
    int target = gprFileArray.getFile(rt).readWord() << (8 * bp);

    gprFileArray.getFile(rd).writeWord(source | target);
  }

  private void aluipc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short imm = (short) (instruction & 0xffff);
    gprFileArray.getFile(rs).writeWord(~0x0ffff & (pc - 4 + (signExtend(imm, 16) << 16)));
  }

  private void clo(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int source = gprFileArray.getFile(rs).readWord();

    int i = 32, mask = 0x80000000;
    for (; (source & mask) != 0 && i > 0; i--, mask >>>= 1)
      ;
    gprFileArray.getFile(rd).writeWord(32 - i);
  }

  private void clz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rd).writeWord(Integer.numberOfLeadingZeros(source));
  }

  private void sll(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(target << sa);
  }

  private void sllv(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(target << source);
  }

  private void rotr(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray
        .getFile(rd)
        .writeWord(
            (extractBits(target, 0x0, sa) << (0x20 - sa))
                | (extractBits(target, sa, 0x20 - sa) >>> sa));
  }

  private void rotrv(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int sa = source & 0x1f;

    gprFileArray
        .getFile(rd)
        .writeWord(
            (extractBits(target, 0x0, sa) << (0x20 - sa)) | extractBits(target, sa, 0x20 - sa));
  }

  private void sra(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(target >> sa);
  }

  private void srav(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int sa = source & 0x1f;

    gprFileArray.getFile(rd).writeWord(target >> sa);
  }

  private void srl(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = (instruction >> 6) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(target >>> sa);
  }

  private void srlv(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int sa = source & 0x1f;

    gprFileArray.getFile(rd).writeWord(target >>> sa);
  }

  private void wsbh(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray
        .getFile(rd)
        .writeWord(
            (extractBits(target, 0x10, 0x8) << 0x18)
                | (extractBits(target, 0x18, 0x8) << 0x10)
                | (extractBits(target, 0x0, 0x8) << 0x8)
                | extractBits(target, 0x8, 0x8));
  }

  private void bitswap(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray
        .getFile(rd)
        .writeWord(
            (0x0ff & reverseByte(extractBits(target, 0x18, 0x8)) << 0x18)
                | 0x0ff & (reverseByte(extractBits(target, 0x10, 0x8)) << 0x10)
                | 0x0ff & (reverseByte(extractBits(target, 0x8, 0x8)) << 0x8)
                | 0x0ff & reverseByte(extractBits(target, 0x0, 0x8)));
  }

  private void and_(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source & target);
  }

  private void andi(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rt).writeWord(source & imm);
  }

  private void nor(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(~(source | target));
  }

  private void or_(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source | target);
  }

  private void ori(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rt).writeWord(source | imm);
  }

  private void xor_(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source ^ target);
  }

  private void xori(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rt).writeWord(source ^ imm);
  }

  private void ext(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int pos = (instruction >> 6) & 0x1f;
    int size = ((instruction >> 11) & 0x1f);

    int source = gprFileArray.getFile(rs).readWord();
    int extracted = extractBits(source, pos, size);
    gprFileArray.getFile(rt).writeWord(extracted);
  }

  private void ins(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int pos = (instruction >> 6) & 0x1f;
    int size = ((instruction >> 11) & 0x1f) + 1;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();

    int mask = ((1 << size) - 1) << pos;
    int result = (target & ~mask) | (extractBits(source, pos, size) << pos);
    gprFileArray.getFile(rt).writeWord(result);
  }

  private void aui(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rt).writeWord(source | (signExtend(imm, 16) << 16));
  }

  private void auipc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    gprFileArray.getFile(rs).writeWord(pc - 4 + (signExtend(imm, 16) << 16));
  }

  private void movn(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (target != 0) {
      gprFileArray.getFile(rd).writeWord(source);
    }
  }

  private void movz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (target == 0) {
      gprFileArray.getFile(rd).writeWord(source);
    }
  }

  private void slt(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = source < target ? 1 : 0;
    gprFileArray.getFile(rd).writeWord(result);
  }

  private void slti(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int result = source < imm ? 1 : 0;
    gprFileArray.getFile(rt).writeWord(result);
  }

  private void sltiu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short imm = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int result = Integer.compareUnsigned(source, imm) < 0 ? 1 : 0;
    gprFileArray.getFile(rt).writeWord(result);
  }

  private void sltu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = Integer.compareUnsigned(source, target) < 0 ? 1 : 0;
    gprFileArray.getFile(rd).writeWord(result);
  }

  private void div(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source / target);
  }

  private void divu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = (int) (Integer.toUnsignedLong(source) / Integer.toUnsignedLong(target));

    gprFileArray.getFile(rd).writeWord(result);
  }

  private void modu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = Integer.remainderUnsigned(source, target);

    gprFileArray.getFile(rd).writeWord(result);
  }

  private void mod(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(rd).writeWord(source % target);
  }

  private void mul(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long result = source * target;

    gprFileArray.getFile(rd).writeWord((int) result);
  }

  private void muh(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long result = source * target;

    gprFileArray.getFile(rd).writeWord((int) (result >> 32));
  }

  private void mulu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    long result = source * target;

    gprFileArray.getFile(rd).writeWord((int) result);
  }

  private void muhu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    long result = source * target;

    gprFileArray.getFile(rd).writeWord((int) (result >> 32));
  }

  private void madd(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long result = ((((long) hi) << 32) | lo) + source * target;

    hi = (int) (result >> 32);
    lo = (int) result;
  }

  private void maddu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    long result = ((((long) hi) << 32) | lo) + source * target;

    hi = (int) (result >> 32);
    lo = (int) result;
  }

  private void msub(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long acc = ((long) hi) << 32 | lo;

    long result = acc - source * target;
    hi = (int) (result >> 32);
    lo = (int) result;
  }

  private void msubu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    long acc = ((long) hi) << 32 | lo;

    long result = acc - source * target;
    hi = (int) (result >> 32);
    lo = (int) result;
  }

  private void mult(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long result = source * target;

    hi = (int) (result >> 32);
    lo = (int) result;
  }

  private void multu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    long result = source * target;

    hi = (int) (result >> 32);
    lo = (int) result;
  }

  private void bal(int instruction) throws Exception {
    short offset = (short) (instruction & 0xffff);
    gprFileArray.getFile(31).writeWord(pc + 4);
    execute(); // delay slot
    pc += (offset << 2);
  }

  private void balc(int instruction) {
    int offset = instruction & 0x3ffffff;
    gprFileArray.getFile(31).writeWord(pc);
    pc += signExtend(offset << 2, 28);
  }

  private void bc(int instruction) {
    int offset = instruction & 0x3ffffff;
    pc += signExtend(offset << 2, 28);
  }

  private void bc1eqz(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = fpuRegisterFileArray.getFile(ft).readWord();
    if ((target & 1) == 0) {
      pc += (offset << 2);
    }
  }

  private void bc1nez(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = fpuRegisterFileArray.getFile(ft).readWord();
    if ((target & 1) == 1) {
      pc += (offset << 2);
    }
  }

  private void bc2eqz(int instruction) {
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = cop2ControlRegisterFileArray.getFile(ct).readWord();
    if (target == 0) {
      pc += (offset << 2);
    }
  }

  private void bc2nez(int instruction) {
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = cop2ControlRegisterFileArray.getFile(ct).readWord();
    if (target != 0) {
      pc += (offset << 2);
    }
  }

  private void beq(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source == target) {
      pc += (offset << 2);
    }
  }

  private void beqc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source == target) {
      pc += (offset << 2);
    }
  }

  private void beqzalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target == 0) {
      gprFileArray.getFile(31).writeWord(pc);
      pc += offset << 2;
    }
  }

  private void bne(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source != target) {
      pc += (offset << 2);
    }
  }

  private void bnec(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source != target) {
      pc += (offset << 2);
    }
  }

  private void bnezc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int offset = signExtend(instruction & 0x1fffff, 21);

    int source = gprFileArray.getFile(rs).readWord();
    if (source != 0) {
      pc += (offset << 2);
    }
  }

  private void bovc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long result = source + target;
    if (result != (int) result) {
      pc += offset << 2;
    }
  }

  private void bnvc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    long source = gprFileArray.getFile(rs).readWord();
    long target = gprFileArray.getFile(rt).readWord();
    long result = source + target;
    if (result == (int) result) {
      pc += offset << 2;
    }
  }

  private void beqzc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int offset = instruction & 0x1fffff;

    int source = gprFileArray.getFile(rs).readWord();
    if (source == 0) {
      pc += signExtend(offset << 2, 23);
    }
  }

  private void bgez(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    if (source >= 0) {
      pc += (offset << 2);
    }
  }

  private void bgtz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    if (source > 0) {
      pc += (offset << 2);
    }
  }

  private void bgezal(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    if (source >= 0) {
      gprFileArray.getFile(31).writeWord(pc); // no delay slot implementation
      pc += (offset << 2);
    }
  }

  private void blezalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target <= 0) {
      gprFileArray.getFile(31).writeWord(pc);
      pc += offset << 2;
    }
  }

  private void bgezalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target >= 0) {
      gprFileArray.getFile(31).writeWord(pc);
      pc += offset << 2;
    }
  }

  private void bgtzalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target > 0) {
      gprFileArray.getFile(31).writeWord(pc);
      pc += offset << 2;
    }
  }

  private void bltzalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target < 0) {
      gprFileArray.getFile(31).writeWord(pc);
      pc += offset << 2;
    }
  }

  private void bnezalc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target != 0) {
      gprFileArray.getFile(31).writeWord(pc);
      pc += offset << 2;
    }
  }

  private void blezc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target <= 0) {
      pc += offset << 2;
    }
  }

  private void bgezc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target >= 0) {
      pc += offset << 2;
    }
  }

  private void bgec(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source >= target) {
      pc += offset << 2;
    }
  }

  private void bgtzc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target > 0) {
      pc += offset << 2;
    }
  }

  private void bltzc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    if (target < 0) {
      pc += offset << 2;
    }
  }

  private void bltc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source < target) {
      pc += offset << 2;
    }
  }

  private void bgeuc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    long source = gprFileArray.getFile(rs).readWord() & 0xffffffffL;
    long target = gprFileArray.getFile(rt).readWord() & 0xffffffffL;
    if (source >= target) {
      pc += offset << 2;
    }
  }

  private void bltuc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    long source = gprFileArray.getFile(rs).readWord() & 0xffffffffL;
    long target = gprFileArray.getFile(rt).readWord() & 0xffffffffL;
    if (source < target) {
      pc += offset << 2;
    }
  }

  private void blez(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    if (source <= 0) {
      pc += offset << 2;
    }
  }

  private void bltz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    if (source < 0) {
      pc += offset << 2;
    }
  }

  private void bltzal(int instruction) throws Exception {
    int rs = (instruction >> 21) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int source = gprFileArray.getFile(rs).readWord();
    if (source < 0) {
      gprFileArray.getFile(31).writeWord(pc + 4);
      execute(); // delay slot implementation
      pc += offset << 2;
    }
  }

  private void nal(int instruction) {
    gprFileArray.getFile(31).writeWord(pc); // no delay slot implementation
  }

  private void break_(int instruction) {
    throw new BreakException();
  }

  private void j(int instruction) {
    int instr_index = instruction & 0x3ffffff;
    pc = (pc & 0xf0000000) | (instr_index << 2);
  }

  private void jal(int instruction) throws Exception {
    int instr_index = instruction & 0x3ffffff;
    gprFileArray.getFile(31).writeWord(pc + 4);
    execute(); // delay slot implementation
    pc = (pc & 0xf0000000) | (instr_index << 2);
  }

  private void jalr(int instruction) throws Exception {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    if (source % 4 != 0) {
      throw new AddressErrorException("jalr: Effective address is not 4-byte aligned");
    }
    gprFileArray.getFile(rd).writeWord(pc + 4);
    execute(); // delay slot

    pc = source;
  }

  private void jic(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    pc = target + offset;
  }

  private void jalr_hb(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    gprFileArray.getFile(rd).writeWord(pc);
    pc = source;
  }

  private void jr(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    pc = gprFileArray.getFile(rs).readWord();
  }

  private void jr_hb(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    pc = gprFileArray.getFile(rs).readWord();
  }

  private void jialc(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int target = gprFileArray.getFile(rt).readWord();
    gprFileArray.getFile(31).writeWord(pc);
    pc = target + offset;
  }

  private void seleqz(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = (target == 0) ? source : 0;
    gprFileArray.getFile(rd).writeWord(result);
  }

  private void selnez(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = (target != 0) ? source : 0;
    gprFileArray.getFile(rd).writeWord(result);
  }

  private void teq(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source == target) {
      throw new TrapException();
    }
  }

  private void tge(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source >= target) {
      throw new TrapException();
    }
  }

  private void tgeu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    if (source >= target) {
      throw new TrapException();
    }
  }

  private void tlt(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source < target) {
      throw new TrapException();
    }
  }

  private void tltu(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    long source = Integer.toUnsignedLong(gprFileArray.getFile(rs).readWord());
    long target = Integer.toUnsignedLong(gprFileArray.getFile(rt).readWord());
    if (source < target) {
      throw new TrapException();
    }
  }

  private void tne(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    if (source != target) {
      throw new TrapException();
    }
  }

  private void lw(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int word = memory.readWord(address);
    gprFileArray.getFile(rt).writeWord(word);
  }

  private void lwe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int word = memory.readWord(address);
    gprFileArray.getFile(rt).writeWord(word);
  }

  private void sw(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.storeWord(target, address);
  }

  private void swe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.storeWord(target, address);
  }

  private void lb(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int bite = memory.read(address);
    gprFileArray.getFile(rt).writeWord(bite);
  }

  private void lbe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    byte bite = memory.read(address);
    gprFileArray.getFile(rt).writeWord(bite);
  }

  private void lbu(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int ubite = memory.read(address) & 0xff;
    gprFileArray.getFile(rt).writeWord(ubite);
  }

  private void lbue(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int ubite = memory.read(address) & 0xff;
    gprFileArray.getFile(rt).writeWord(ubite);
  }

  private void lh(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int half = memory.readHalf(address);
    gprFileArray.getFile(rt).writeWord(half);
  }

  private void lhe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    short half = memory.readHalf(address);
    gprFileArray.getFile(rt).writeWord(half);
  }

  private void lhu(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int uhalf = memory.readHalf(address) & 0xffff;
    gprFileArray.getFile(rt).writeWord(uhalf);
  }

  private void lhue(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int uhalf = memory.readHalf(address) & 0xffff;
    gprFileArray.getFile(rt).writeWord(uhalf);
  }

  private void lsa(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;
    int sa = ((instruction >> 6) & 0x3) + 1;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int result = (source << sa) + target;
    gprFileArray.getFile(rd).writeWord(result);
  }

  private void lwl(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    int mem = memory.readHalf(address);

    target &= 0xffff;
    target |= (mem << 16);
    gprFileArray.getFile(rt).writeWord(target);
  }

  private void lwpc(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int offset = instruction & 0x7ffff;

    int address = pc + signExtend(offset << 2, 21);
    int result = memory.readWord(address);
    gprFileArray.getFile(rs).writeWord(result);
  }

  private void lwr(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    int mem = memory.read(address);

    int eAddr = address - 1;
    if (eAddr >= 0) {
      mem = memory.readHalf(eAddr);
    }

    target &= 0xffff0000;
    target |= mem;
    gprFileArray.getFile(rt).writeWord(target);
  }

  private void sb(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.store((byte) target, address);
  }

  private void sbe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.store((byte) target, address);
  }

  private void sh(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.storeHalf((short) target, address);
  }

  private void she(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.storeHalf((short) target, address);
  }

  private void swl(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.storeHalf((short) (target >> 16), address);
  }

  private void swr(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    int eAddr = address - 1;

    if (eAddr >= 0) {
      memory.storeHalf((short) target, eAddr);
    } else {
      memory.store((byte) target, address);
    }
  }

  private void cache(int instruction) {
    // noop
  }

  private void cachee(int instruction) {
    // noop
  }

  private void mfhi(int instruction) {
    int rd = (instruction >> 11) & 0x1f;
    int hi = this.hi;
    gprFileArray.getFile(rd).writeWord(hi);
  }

  private void mflo(int instruction) {
    int rd = (instruction >> 11) & 0x1f;
    int lo = this.lo;
    gprFileArray.getFile(rd).writeWord(lo);
  }

  private void mthi(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    hi = gprFileArray.getFile(rs).readWord();
  }

  private void mtlo(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int source = gprFileArray.getFile(rs).readWord();
    lo = source;
  }

  // Note: atomic instructions always succeeds because we're not really going try to simulate
  // synchronization
  private void ll(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int mem = memory.readWord(address);
    gprFileArray.getFile(rt).writeWord(mem);
  }

  private void lle(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int result = memory.readWord(address);
    gprFileArray.getFile(rt).writeWord(result);
  }

  private void llwp(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int address = gprFileArray.getFile(base).readWord();
    gprFileArray.getFile(rd).writeWord(memory.readWord(address));
    gprFileArray.getFile(rt).writeWord(memory.readWord(address + 4));
  }

  private void llwpe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int address = gprFileArray.getFile(base).readWord();
    gprFileArray.getFile(rd).writeWord(memory.readWord(address));
    gprFileArray.getFile(rt).writeWord(memory.readWord(address + 4));
  }

  private void sc(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = gprFileArray.getFile(rt).readWord();
    memory.storeWord(target, address);

    gprFileArray.getFile(rt).writeWord(1);
  }

  private void sce(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    short offset = (short) ((instruction & (0x1ff << 7)) >> 7);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int value = gprFileArray.getFile(rt).readWord();
    memory.storeWord(value, address);

    gprFileArray.getFile(rt).writeWord(1);
  }

  private void scwp(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int address = gprFileArray.getFile(base).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int dest = gprFileArray.getFile(rd).readWord();

    memory.storeWord(dest, address);
    memory.storeWord(target, address + 4);
    gprFileArray.getFile(rt).writeWord(1);
  }

  private void scwpe(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int address = gprFileArray.getFile(base).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int dest = gprFileArray.getFile(rd).readWord();

    memory.storeWord(dest, address);
    memory.storeWord(target, address + 4);
    gprFileArray.getFile(rt).writeWord(1);
  }

  private void pref(int instruction) {
    // noop
  }

  private void prefe(int instruction) {
    // noop
  }

  private void rdhwr(int instruction) {
    // noop
  }

  private void rdpgpr(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int dest = shadowRegisterFileArray.getFile(rd).readWord();
    gprFileArray.getFile(rt).writeWord(dest);
  }

  private void deret(int instruction) {
    // noop
  }

  private void di(int instruction) {
    // noop
  }

  private void dvp(int instruction) {
    // noop
  }

  private void evp(int instruction) {
    // noop
  }

  private void ei(int instruction) {
    // noop
  }

  private void eret(int instruction) {
    // noop
  }

  private void eretnc(int instruction) {
    // noop
  }

  private void ginvi(int instruction) {
    // noop
  }

  private void ginvt(int instruction) {
    // noop
  }

  private void pause(int instruction) {
    // noop
  }

  private void sdbbp(int instruction) {
    throw new DebugBreakpointException();
  }

  private void sigrie(int instruction) {
    throw new ReservedInstructionException();
  }

  private void syscall(int instruction) {
    throw new SyscallException();
  }

  private void sync(int instruction) {
    // noop
  }

  private void synci(int instruction) {
    // noop
  }

  private void tlbinv(int instruction) {
    // noop
  }

  private void tlbinvf(int instruction) {
    // noop
  }

  private void tlbp(int instruction) {
    // noop
  }

  private void tlbr(int instruction) {
    // noop
  }

  private void tlbwi(int instruction) {
    // noop
  }

  private void tlbwr(int instruction) {
    // noop
  }

  private void wait(int instruction) {
    // noop
  }

  private void wrpgpr(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int rd = (instruction >> 11) & 0x1f;

    int value = gprFileArray.getFile(rt).readWord();
    shadowRegisterFileArray.getFile(rd).writeWord(value);
  }

  private void cop2(int instruction) {
    // noop
  }

  private void swc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = fpuRegisterFileArray.getFile(ft).readWord();
    memory.storeWord(target, address);
  }

  private void swc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int target = cop2RegisterFileArray.getFile(ct).readWord();
    memory.storeWord(target, address);
  }

  private void ldc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    long mem = memory.readDWord(address);
    fpuRegisterFileArray.getFile(ft).writeDword(mem);
  }

  private void ldc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    long mem = memory.readDWord(address);
    cop2RegisterFileArray.getFile(ct).writeDword(mem);
  }

  private void lwc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int mem = memory.readWord(address);
    fpuRegisterFileArray.getFile(ft).writeWord(mem);
  }

  private void lwc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    int mem = memory.readWord(address);
    cop2RegisterFileArray.getFile(ct).writeWord(mem);
  }

  private void sdc1(int instruction) {
    int base = (instruction >> 21) & 0x1f;
    int ft = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0xffff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    long target = fpuRegisterFileArray.getFile(ft).readDword();
    memory.storeDword(target, address);
  }

  private void sdc2(int instruction) {
    int base = (instruction >> 11) & 0x1f;
    int ct = (instruction >> 16) & 0x1f;
    short offset = (short) (instruction & 0x7ff);

    int address = gprFileArray.getFile(base).readWord() + offset;
    long target = cop2RegisterFileArray.getFile(ct).readDword();
    memory.storeDword(target, address);
  }

  private void cfc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;

    int source = fpcRegisterFileArray.getFile(fs).readWord();
    gprFileArray.getFile(rt).writeWord(source);
  }

  private void cfc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;

    int source = cop2ControlRegisterFileArray.getFile(cs).readWord();
    gprFileArray.getFile(rt).writeWord(source);
  }

  private void ctc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    fpcRegisterFileArray.getFile(fs).writeWord(target);
  }

  private void ctc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    cop2ControlRegisterFileArray.getFile(cs).writeWord(target);
  }

  private void mfc0(int instruction) {
    // noop
  }

  private void mfc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;

    int source = fpuRegisterFileArray.getFile(fs).readWord();
    gprFileArray.getFile(rt).writeWord(source);
  }

  private void mfc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;

    int source = cop2RegisterFileArray.getFile(cs).readWord();
    gprFileArray.getFile(rt).writeWord(source);
  }

  private void mfhc0(int instruction) {
    // noop
  }

  private void mfhc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;

    long source = fpuRegisterFileArray.getFile(fs).readDword();
    gprFileArray.getFile(rt).writeWord((int) (source >> 32));
  }

  private void mfhc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;

    long source = cop2RegisterFileArray.getFile(cs).readDword();
    gprFileArray.getFile(rt).writeWord((int) (source >> 32));
  }

  private void mtc0(int instruction) { // noop
  }

  private void mtc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    fpuRegisterFileArray.getFile(fs).writeWord(target);
  }

  private void mtc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    cop2RegisterFileArray.getFile(cs).writeWord(target);
  }

  private void mthc0(int instruction) {
    // noop
  }

  private void mthc1(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    long source = fpuRegisterFileArray.getFile(fs).readDword();
    fpuRegisterFileArray.getFile(fs).writeDword((source & 0xffffffffL) | ((long) target << 32));
  }

  private void mthc2(int instruction) {
    int rt = (instruction >> 16) & 0x1f;
    int cs = (instruction >> 11) & 0x1f;

    int target = gprFileArray.getFile(rt).readWord();
    long source = cop2RegisterFileArray.getFile(cs).readDword();
    cop2RegisterFileArray.getFile(cs).writeDword((source & 0xffffffffL) | ((long) target) << 32);
  }

  private void abs_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(Math.abs(source));
  }

  private void abs_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(Math.abs(source));
  }

  private void add_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(source + target);
  }

  private void add_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(source + target);
  }

  private void div_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(source / target);
  }

  private void div_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(source / target);
  }

  private void mul_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(source * target);
  }

  private void mul_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(source * target);
  }

  private void neg_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(-source);
  }

  private void neg_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(-source);
  }

  private void sqrt_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle((float) Math.sqrt(source));
  }

  private void sqrt_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(Math.sqrt(source));
  }

  private void sub_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(source - target);
  }

  private void sub_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(source - target);
  }

  private void recip_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(1.0f / source);
  }

  private void recip_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(1.0 / source);
  }

  private void rsqrt_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(1.0f / (float) Math.sqrt(source));
  }

  private void rsqrt_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(1.0 / Math.sqrt(source));
  }

  private void maddf_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    float dest = fpuRegisterFileArray.getFile(fd).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(dest + (source * target));
  }

  private void maddf_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    double dest = fpuRegisterFileArray.getFile(fd).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(dest + (source * target));
  }

  private void msubf_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    float dest = fpuRegisterFileArray.getFile(fd).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(dest - (source * target));
  }

  private void msubf_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    double dest = fpuRegisterFileArray.getFile(fd).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(dest - (source * target));
  }

  // TODO: verify
  private void class_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    int result = 0;
    if (Float.isNaN(source)) {
      int bits = Float.floatToRawIntBits(source);
      if ((bits & 0x00400000) != 0) {
        result = 0x2; // qNAN
      } else {
        result = 0x1; // sNAN
      }
    } else if (Float.isInfinite(source)) {
      result = source < 0 ? 0x4 /*-inf*/ : 0x40 /*+inf*/;
    } else if (source == 0) {
      int bits = Float.floatToRawIntBits(source);
      result = (bits & 0x80000000) != 0 ? 0x8 /*-norm*/ : 0x200 /*+0*/;
    } else {
      int bits = Float.floatToRawIntBits(source);
      int exp = (bits >> 23) & 0xff;
      if (exp == 0) {
        result = source < 0 ? 0x10 /*-subnorm*/ : 0x100 /*+subnorm*/;
      } else {
        result = source < 0 ? 0x20 /*-0*/ : 0x80 /*+norm*/;
      }
    }
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  // TODO: verify
  private void class_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    int result = 0;
    if (Double.isNaN(source)) {
      long bits = Double.doubleToRawLongBits(source);
      if ((bits & 0x0008000000000000L) != 0) {
        result = 0x2;
      } else {
        result = 0x1;
      }
    } else if (Double.isInfinite(source)) {
      result = source < 0 ? 0x4 : 0x40;
    } else if (source == 0) {
      long bits = Double.doubleToRawLongBits(source);
      result = (bits & 0x8000000000000000L) != 0 ? 0x8 : 0x200;
    } else {
      long bits = Double.doubleToRawLongBits(source);
      long exp = (bits >> 52) & 0x7ff;
      if (exp == 0) {
        result = source < 0 ? 0x10 : 0x100;
      } else {
        result = source < 0 ? 0x20 : 0x80;
      }
    }
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void max_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(Math.max(source, target));
  }

  private void max_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(Math.max(source, target));
  }

  private void maxa_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    float result = Math.abs(source) > Math.abs(target) ? source : target;
    fpuRegisterFileArray.getFile(fd).writeSingle(result);
  }

  private void maxa_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    double result = Math.abs(source) > Math.abs(target) ? source : target;
    fpuRegisterFileArray.getFile(fd).writeDouble(result);
  }

  private void min_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(Math.min(source, target));
  }

  private void min_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(Math.min(source, target));
  }

  private void mina_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    float result = Math.abs(source) < Math.abs(target) ? source : target;
    fpuRegisterFileArray.getFile(fd).writeSingle(result);
  }

  private void mina_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    double result = Math.abs(source) < Math.abs(target) ? source : target;
    fpuRegisterFileArray.getFile(fd).writeDouble(result);
  }

  private void rint_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle((float) Math.rint(source));
  }

  private void rint_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(Math.rint(source));
  }

  private void mov_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle(source);
  }

  private void mov_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble(source);
  }

  private void sel_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    int dest = fpuRegisterFileArray.getFile(fd).readWord();
    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeSingle((dest & 1) == 0 ? source : target);
  }

  private void sel_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    long dest = fpuRegisterFileArray.getFile(fd).readDword();
    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDouble((dest & 1) == 0 ? source : target);
  }

  private void seleqz_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    int target = fpuRegisterFileArray.getFile(ft).readWord();
    fpuRegisterFileArray.getFile(fd).writeSingle((target & 1) == 0 ? source : 0.0f);
  }

  private void seleqz_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    long target = fpuRegisterFileArray.getFile(ft).readDword();
    fpuRegisterFileArray.getFile(fd).writeDouble((target & 1) == 0 ? source : 0.0);
  }

  private void selnez_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    int target = fpuRegisterFileArray.getFile(ft).readWord();
    fpuRegisterFileArray.getFile(fd).writeSingle((target & 1) != 0 ? source : 0.0f);
  }

  private void selnez_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    long target = fpuRegisterFileArray.getFile(ft).readDword();
    fpuRegisterFileArray.getFile(fd).writeDouble((target & 1) != 0 ? source : 0.0);
  }

  private void cmp_af_s(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    fpuRegisterFileArray.getFile(fd).writeWord(0);
  }

  private void cmp_af_d(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    fpuRegisterFileArray.getFile(fd).writeDword(0);
  }

  private void cmp_un_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (Float.isNaN(source) || Float.isNaN(target)) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_un_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (Double.isNaN(source) || Double.isNaN(target)) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_eq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source == target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_eq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source == target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_ueq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source == target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_ueq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source == target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_lt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source < target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_lt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source < target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_ult_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source < target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_ult_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source < target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_le_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source <= target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_le_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source <= target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_ule_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source <= target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_ule_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source <= target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  // No distinction for qNAN or sNAN for all signaling operation here
  private void cmp_saf_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord(0);

    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_saf_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword(0);

    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sun_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    fpuRegisterFileArray.getFile(fd).writeWord(unordered ? 0xffffffff : 0);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sun_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    fpuRegisterFileArray.getFile(fd).writeDword(unordered ? 0xffffffffffffffffL : 0);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_seq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source == target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_seq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source == target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sueq_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source == target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sueq_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source == target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_slt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source < target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_slt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source < target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sult_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source < target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sult_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source < target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sle_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source <= target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sle_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source <= target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sule_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source <= target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sule_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source <= target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_at_s(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    fpuRegisterFileArray.getFile(fd).writeWord(0xffffffff);
  }

  private void cmp_at_d(int instruction) {
    int fd = (instruction >> 6) & 0x1f;
    fpuRegisterFileArray.getFile(fd).writeDword(0xffffffffffffffffL);
  }

  private void cmp_or_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean ordered = !Float.isNaN(source) && !Float.isNaN(target);

    int result = ordered ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_or_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean ordered = !Double.isNaN(source) && !Double.isNaN(target);

    long result = ordered ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_une_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source != target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_une_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source != target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_ne_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source != target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_ne_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source != target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_uge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source >= target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_uge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source >= target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_oge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source >= target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_oge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source >= target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_ugt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source > target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_ugt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source > target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_ogt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source > target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
  }

  private void cmp_ogt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source > target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
  }

  private void cmp_sat_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord(0xffffffff);

    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sat_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword(0xffffffffffffffffL);

    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sor_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = !unordered ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sor_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = !unordered ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sune_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source != target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sune_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source != target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sne_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source != target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sne_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source != target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_suge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source >= target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_suge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source >= target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_soge_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source >= target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_soge_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source >= target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sugt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    boolean unordered = Float.isNaN(source) || Float.isNaN(target);

    int result = (unordered || source > target) ? 0xffffffff : 0;
    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sugt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    boolean unordered = Double.isNaN(source) || Double.isNaN(target);

    long result = (unordered || source > target) ? 0xffffffffffffffffL : 0;
    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (unordered) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sogt_s(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    float target = fpuRegisterFileArray.getFile(ft).readSingle();
    int result = (source > target) ? 0xffffffff : 0;

    fpuRegisterFileArray.getFile(fd).writeWord(result);
    if (Float.isNaN(source) || Float.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cmp_sogt_d(int instruction) {
    int ft = (instruction >> 16) & 0x1f;
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    double target = fpuRegisterFileArray.getFile(ft).readDouble();
    long result = (source > target) ? 0xffffffffffffffffL : 0;

    fpuRegisterFileArray.getFile(fd).writeDword(result);
    if (Double.isNaN(source) || Double.isNaN(target)) {
      throw new InvalidOperationException();
    }
  }

  private void cvt_d_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeDouble(source);
  }

  private void cvt_d_w(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    int source = fpuRegisterFileArray.getFile(fs).readWord();
    fpuRegisterFileArray.getFile(fd).writeDouble(source);
  }

  private void cvt_d_l(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    long source = fpuRegisterFileArray.getFile(fs).readDword();
    fpuRegisterFileArray.getFile(fd).writeDouble((double) source);
  }

  private void cvt_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeDword((long) source);
  }

  private void cvt_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword((long) source);
  }

  private void cvt_s_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeSingle((float) source);
  }

  private void cvt_s_w(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    int source = fpuRegisterFileArray.getFile(fs).readWord();
    fpuRegisterFileArray.getFile(fd).writeSingle((float) source);
  }

  private void cvt_s_l(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    long source = fpuRegisterFileArray.getFile(fs).readDword();
    fpuRegisterFileArray.getFile(fd).writeSingle((float) source);
  }

  private void cvt_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord((int) source);
  }

  private void cvt_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeWord((int) source);
  }

  private void ceil_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeDword((long) Math.ceil(source));
  }

  private void ceil_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword((long) Math.ceil(source));
  }

  private void ceil_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord((int) Math.ceil(source));
  }

  private void ceil_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeWord((int) Math.ceil(source));
  }

  private void floor_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeDword((long) Math.floor(source));
  }

  private void floor_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword((long) Math.floor(source));
  }

  private void floor_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord((int) Math.floor(source));
  }

  private void floor_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeWord((int) Math.floor(source));
  }

  private void round_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeDword(Math.round((double) source));
  }

  private void round_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword(Math.round(source));
  }

  private void round_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord(Math.round(source));
  }

  private void round_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeWord((int) Math.round(source));
  }

  private void trunc_l_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeDword((long) source);
  }

  private void trunc_l_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeDword((long) source);
  }

  private void trunc_w_s(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    float source = fpuRegisterFileArray.getFile(fs).readSingle();
    fpuRegisterFileArray.getFile(fd).writeWord((int) source);
  }

  private void trunc_w_d(int instruction) {
    int fs = (instruction >> 11) & 0x1f;
    int fd = (instruction >> 6) & 0x1f;

    double source = fpuRegisterFileArray.getFile(fs).readDouble();
    fpuRegisterFileArray.getFile(fd).writeWord((int) source);
  }

  private void crc32b(int instruction) {
    int rd = (instruction >> 21) & 0x1f;
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int crc = crc32(target, source & 0xff, 8);
    gprFileArray.getFile(rt).writeWord(crc);
  }

  private void crc32h(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int crc = crc32(target, source & 0xffff, 16);
    gprFileArray.getFile(rt).writeWord(crc);
  }

  private void crc32w(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int crc = crc32(target, source, 32);
    gprFileArray.getFile(rt).writeWord(crc);
  }

  private void crc32cb(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int crc = crc32c(target, source & 0xff, 8);
    gprFileArray.getFile(rt).writeWord(crc);
  }

  private void crc32ch(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int crc = crc32c(target, source & 0xffff, 16);
    gprFileArray.getFile(rt).writeWord(crc);
  }

  private void crc32cw(int instruction) {
    int rs = (instruction >> 21) & 0x1f;
    int rt = (instruction >> 16) & 0x1f;

    int source = gprFileArray.getFile(rs).readWord();
    int target = gprFileArray.getFile(rt).readWord();
    int crc = crc32c(target, source, 32);
    gprFileArray.getFile(rt).writeWord(crc);
  }

  private int crc32(int crc, int data, int bits) {
    int polynomial = 0xEDB88320;
    crc ^= data;
    for (int i = 0; i < bits; i++) {
      if ((crc & 1) != 0) {
        crc = (crc >>> 1) ^ polynomial;
      } else {
        crc = crc >>> 1;
      }
    }
    return crc;
  }

  private int crc32c(int crc, int data, int bits) {
    int polynomial = 0x82F63B78;
    crc ^= data;
    for (int i = 0; i < bits; i++) {
      if ((crc & 1) != 0) {
        crc = (crc >>> 1) ^ polynomial;
      } else {
        crc = crc >>> 1;
      }
    }
    return crc;
  }

  private int extractBits(int source, int pos, int size) {
    int mask = (1 << size) - 1;
    mask <<= pos; // align mask with bits to extract
    return (source & mask) >>> pos;
  }

  private byte reverseByte(int target) {
    byte out = 0;
    for (int n = 0; n < 8; n++, target >>= 1) {
      out <<= 1;
      out |= (byte) (target & 1);
    }

    return out;
  }

  private int signExtend(int target, int size) {
    int mask = 1 << (size - 1);
    if ((target & mask) != 0) {
      return target | -mask;
    }
    return target;
  }
}
