package com.cleverchuk.mips.simulator.binary;

import java.util.*;

/**
 * MIPS instruction decoder using hierarchical lookup tables.
 *
 * STRATEGY:
 * Level 1: opcode (bits 31-26)
 * Level 2: function field (bits 5-0) from partialEncoding
 * Level 3: extended bits high (bits 10-8) from partialEncoding
 * Level 4: extended bits low (bits 7-6) from partialEncoding
 * Level 5: format/RS field (bits 25-21) from partialEncoding
 * Level 6: RT field (bits 20-16) from partialEncoding
 * Level 7: full partialEncoding match
 * Level 8: disambiguate by operand flags (rs/rt/rd)
 */
public class MipsInstructionDecoder {

  // Bit masks
  private static final int OPCODE_MASK = 0xFC000000;     // bits 31-26
  private static final int FUNCT_MASK = 0x0000003F;      // bits 5-0
  private static final int EXT_HIGH_MASK = 0x00000700;   // bits 10-8
  private static final int EXT_LOW_MASK = 0x000000C0;    // bits 7-6
  private static final int RS_MASK = 0x03E00000;         // bits 25-21
  private static final int RT_MASK = 0x001F0000;         // bits 20-16

  /**
   * Hierarchical lookup: opcode -> funct -> extHigh -> extLow -> rs -> rt -> partialEncoding -> [opcodes]
   */
  private static final Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>>>>> LOOKUP_TABLE = new HashMap<>();

  static {
    buildLookupTable();
  }

  /**
   * Builds the hierarchical lookup table
   */
  private static void buildLookupTable() {
    for (Opcode op : Opcode.values()) {
      if (op.format == InstructionFormat.IDIOM) {
        continue;
      }

      // Level 1: Primary opcode
      int primaryOpcode = op.opcode;

      // Level 2: Function field (bits 5-0 of partialEncoding)
      int funct = op.partialEncoding & FUNCT_MASK;

      // Level 3: Extended bits high (bits 10-8 of partialEncoding, shifted to 0-based)
      int extHigh = (op.partialEncoding & EXT_HIGH_MASK) >> 8;

      // Level 4: Extended bits low (bits 7-6 of partialEncoding, shifted to 0-based)
      int extLow = (op.partialEncoding & EXT_LOW_MASK) >> 6;

      // Level 5: RS/format field (bits 25-21 of partialEncoding, shifted to 0-based)
      int rs = (op.partialEncoding & RS_MASK) >> 21;

      // Level 6: RT field (bits 20-16 of partialEncoding, shifted to 0-based)
      int rt = (op.partialEncoding & RT_MASK) >> 16;

      // Level 7: Full partialEncoding
      int partial = op.partialEncoding;

      LOOKUP_TABLE
          .computeIfAbsent(primaryOpcode, k -> new HashMap<>())
          .computeIfAbsent(funct, k -> new HashMap<>())
          .computeIfAbsent(extHigh, k -> new HashMap<>())
          .computeIfAbsent(extLow, k -> new HashMap<>())
          .computeIfAbsent(rs, k -> new HashMap<>())
          .computeIfAbsent(rt, k -> new HashMap<>())
          .computeIfAbsent(partial, k -> new ArrayList<>())
          .add(op);
    }
  }

  /**
   * Main decode method
   */
  public static Opcode decode(int instruction) {
    // Level 1: Extract primary opcode
    int primaryOpcode = instruction & OPCODE_MASK;
    Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>>>> level2 = LOOKUP_TABLE.get(primaryOpcode);

    if (level2 == null) {
      return null;
    }

    // Level 2: Extract function field from instruction (bits 5-0)
    int funct = instruction & FUNCT_MASK;
    Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>>> level3 = level2.get(funct);

    if (level3 == null) {
      // Try wildcard (function = 0)
      level3 = level2.get(0);
      if (level3 == null) {
        return enumerateAndMatch(instruction, level2);
      }
    }

    // Level 3: Extract extended bits high (bits 10-8)
    int extHigh = (instruction & EXT_HIGH_MASK) >> 8;
    Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>> level4 = level3.get(extHigh);

    if (level4 == null) {
      // Try wildcard (extHigh = 0)
      level4 = level3.get(0);
      if (level4 == null) {
        return enumerateAndMatch(instruction, level3);
      }
    }

    // Level 4: Extract extended bits low (bits 7-6)
    int extLow = (instruction & EXT_LOW_MASK) >> 6;
    Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>> level5 = level4.get(extLow);

    if (level5 == null) {
      // Try wildcard (extLow = 0)
      level5 = level4.get(0);
      if (level5 == null) {
        return enumerateAndMatch(instruction, level4);
      }
    }

    // Level 5: Extract RS/format field from instruction
    int rs = (instruction & RS_MASK) >> 21;
    Map<Integer, Map<Integer, List<Opcode>>> level6 = level5.get(rs);

    if (level6 == null) {
      // Try wildcard (rs = 0)
      level6 = level5.get(0);
      if (level6 == null) {
        return enumerateAndMatch(instruction, level5);
      }
    }

    // Level 6: Extract RT field from instruction
    int rt = (instruction & RT_MASK) >> 16;
    Map<Integer, List<Opcode>> level7 = level6.get(rt);

    if (level7 == null) {
      // Try wildcard (rt = 0)
      level7 = level6.get(0);
      if (level7 == null) {
        return enumerateAndMatch(instruction, level6);
      }
    }

    // No exact match, enumerate all possibilities
    return enumerateAndMatch(instruction, level7);
  }

  /**
   * Enumerates all opcodes in a map structure to find matches
   * Used when exact lookup fails
   */
  private static Opcode enumerateAndMatch(int instruction, Map<?, ?> map) {
    List<Opcode> allCandidates = new ArrayList<>();
    collectAllOpcodes(map, allCandidates);

    if (allCandidates.isEmpty()) {
      return null;
    }

    // Filter candidates by partialEncoding match
    List<Opcode> matches = new ArrayList<>();
    for (Opcode candidate : allCandidates) {
      if (matchesPartialEncoding(instruction, candidate)) {
        matches.add(candidate);
      }
    }

    if (matches.isEmpty()) {
      return null;
    }

    if (matches.size() == 1) {
      return matches.get(0);
    }

    return disambiguateByOperands(instruction, matches);
  }

  /**
   * Recursively collects all Opcode objects from nested maps
   */
  private static void collectAllOpcodes(Map<?, ?> map, List<Opcode> result) {
    for (Object value : map.values()) {
      if (value instanceof List) {
        @SuppressWarnings("unchecked")
        List<Opcode> opcodes = (List<Opcode>) value;
        result.addAll(opcodes);
      } else if (value instanceof Map) {
        collectAllOpcodes((Map<?, ?>) value, result);
      }
    }
  }

  /**
   * Checks if instruction matches opcode's partialEncoding
   */
  private static boolean matchesPartialEncoding(int instruction, Opcode opcode) {
    if (opcode.partialEncoding == 0) {
      return true;
    }

    // All bits set in partialEncoding must be set in instruction
    return (instruction & opcode.partialEncoding) == opcode.partialEncoding;
  }

  /**
   * Disambiguates multiple candidates using operand flags and instruction-specific rules
   */
  private static Opcode disambiguateByOperands(int instruction, List<Opcode> candidates) {
    if (candidates.isEmpty()) {
      return null;
    }

    if (candidates.size() == 1) {
      return candidates.get(0);
    }

    // Extract register fields
    int rs = (instruction >> 21) & 0x1F;
    int rt = (instruction >> 16) & 0x1F;
    int rd = (instruction >> 11) & 0x1F;

    // Apply instruction-specific disambiguation rules
    for (Opcode candidate : candidates) {
      if (matchesInstructionRules(candidate, rs, rt, rd)) {
        return candidate;
      }
    }

    // Fallback: Check which registers are non-zero (actually used)
    boolean hasRs = rs != 0;
    boolean hasRt = rt != 0;
    boolean hasRd = rd != 0;

    // Try exact match on operand flags
    for (Opcode candidate : candidates) {
      boolean rsMatch = !candidate.rs || hasRs;
      boolean rtMatch = !candidate.rt || hasRt;
      boolean rdMatch = !candidate.rd || hasRd;

      if (rsMatch && rtMatch && rdMatch) {
        return candidate;
      }
    }

    // No perfect match, return the first candidate
    // Sort by specificity (more operands = more specific)
    candidates.sort((a, b) -> {
      int aScore = (a.rs ? 1 : 0) + (a.rt ? 1 : 0) + (a.rd ? 1 : 0);
      int bScore = (b.rs ? 1 : 0) + (b.rt ? 1 : 0) + (b.rd ? 1 : 0);
      return Integer.compare(bScore, aScore); // descending
    });

    return candidates.get(0);
  }

  /**
   * Applies instruction-specific disambiguation rules
   */
  private static boolean matchesInstructionRules(Opcode opcode, int rs, int rt, int rd) {
    switch (opcode) {
      case BGEUC:
      case BLTUC:
      case BGEC:
      case BLTC:
        return rs != rt && rt != 0 && rs != 0;
      case BGEZC:
      case BLTZALC:
      case BGEZALC:
        return rs == rt && rs != 0;
      case BLTZC:
        return rs == rt && rt != 0;
      case BGTZC:
      case BGTZALC:
      case BLEZALC:
      case BLEZC:
        return rs == 0 && rt != 0;
      case BEQC:
      case BNEC:
        return rt != 0 && rs != 0 && rs < rt;
      case BOVC:
        return rs >= rt && rt != 0;
      case BNVC:
        return rs >= rt;
      case BEQZC:
      case BNEZC:
        return rs != 0 && rt == 0;
      case JIC:
      case JIALC:
        return rt != 0;
      case BEQZALC:
      case BNEZALC:
        return rs < rt && rt != 0 && rs == 0;
      case BGEZAL:
        return rs != 0;
      case BAL:
        return rs == 0;
      default:
        return false;
    }
  }

  /**
   * Disassembles an instruction to assembly syntax
   */
  public static String disassemble(int instruction, Opcode opcode) {
    if (opcode == null) {
      return String.format("UNKNOWN [0x%08X]", instruction);
    }

    int rs = (instruction >> 21) & 0x1F;
    int rt = (instruction >> 16) & 0x1F;
    int rd = (instruction >> 11) & 0x1F;
    int shamt = (instruction >> 6) & 0x1F;
    short imm = (short)(instruction & 0xFFFF);
    int target = instruction & 0x03FFFFFF;

    StringBuilder sb = new StringBuilder(opcode.name);
    sb.append(" ");

    switch (opcode.format) {
      case R_TYPE:
        if (opcode.rd) sb.append("$").append(rd);
        if (opcode.rs) {
          if (opcode.rd) sb.append(", ");
          sb.append("$").append(rs);
        }
        if (opcode.rt) {
          if (opcode.rd || opcode.rs) sb.append(", ");
          sb.append("$").append(rt);
        }
        if (shamt != 0 && !opcode.rs && !opcode.rt) {
          sb.append(", ").append(shamt);
        }
        break;

      case I_TYPE:
        if (opcode.rt) sb.append("$").append(rt);
        if (opcode.rs) {
          if (opcode.rt) sb.append(", ");
          sb.append("$").append(rs);
        }
        if (opcode.rt || opcode.rs) sb.append(", ");
        sb.append(imm);
        break;

      case J_TYPE:
        sb.append("0x").append(Integer.toHexString(target << 2));
        break;
    }

    return sb.toString();
  }

  /**
   * Debug utility
   */
  public static void debugDecode(int instruction) {
    System.out.printf("\n=== Decoding 0x%08X ===%n", instruction);

    int opcode = instruction & OPCODE_MASK;
    int funct = instruction & FUNCT_MASK;
    int extHigh = (instruction & EXT_HIGH_MASK) >> 8;
    int extLow = (instruction & EXT_LOW_MASK) >> 6;
    int rs = (instruction >> 21) & 0x1F;
    int rt = (instruction >> 16) & 0x1F;
    int rd = (instruction >> 11) & 0x1F;

    System.out.printf("Opcode: 0x%08X, Funct: 0x%02X%n", opcode, funct);
    System.out.printf("Ext[10-8]: 0x%X, Ext[7-6]: 0x%X%n", extHigh, extLow);
    System.out.printf("RS: %d, RT: %d, RD: %d%n", rs, rt, rd);

    Opcode result = decode(instruction);
    System.out.printf("Result: %s%n", result != null ? result.name : "NULL");

    if (result != null) {
      System.out.printf("Disassembly: %s%n", disassemble(instruction, result));
    }
  }

  /**
   * Prints statistics about the lookup table
   */
  public static void printStats() {
    int totalInstructions = 0;
    int level1Keys = LOOKUP_TABLE.size();
    int level2Keys = 0;
    int level3Keys = 0;
    int level4Keys = 0;
    int level5Keys = 0;
    int level6Keys = 0;
    int level7Keys = 0;

    for (Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>>>> l2 : LOOKUP_TABLE.values()) {
      level2Keys += l2.size();
      for (Map<Integer, Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>>> l3 : l2.values()) {
        level3Keys += l3.size();
        for (Map<Integer, Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>>> l4 : l3.values()) {
          level4Keys += l4.size();
          for (Map<Integer, Map<Integer, Map<Integer, List<Opcode>>>> l5 : l4.values()) {
            level5Keys += l5.size();
            for (Map<Integer, Map<Integer, List<Opcode>>> l6 : l5.values()) {
              level6Keys += l6.size();
              for (Map<Integer, List<Opcode>> l7 : l6.values()) {
                level7Keys += l7.size();
                for (List<Opcode> opcodes : l7.values()) {
                  totalInstructions += opcodes.size();
                }
              }
            }
          }
        }
      }
    }

    System.out.println("=== LOOKUP TABLE STATISTICS ===");
    System.out.printf("Level 1 (opcode) keys: %d%n", level1Keys);
    System.out.printf("Level 2 (funct 5-0) keys: %d%n", level2Keys);
    System.out.printf("Level 3 (ext 10-8) keys: %d%n", level3Keys);
    System.out.printf("Level 4 (ext 7-6) keys: %d%n", level4Keys);
    System.out.printf("Level 5 (RS/format) keys: %d%n", level5Keys);
    System.out.printf("Level 6 (RT) keys: %d%n", level6Keys);
    System.out.printf("Level 7 (partial) keys: %d%n", level7Keys);
    System.out.printf("Total instructions: %d%n", totalInstructions);
  }

  /**
   * Test harness
   */
  public static void main(String[] args) {
    printStats();

    System.out.println("\n" + "=".repeat(60));

    int[] tests = {
        0x00851020,  // ADD
        0x20420005,  // ADDI
        0x46204A05,  // ABS.D
        0x00021040,  // SLL
    };

    for (int instr : tests) {
      debugDecode(instr);
    }
  }
}