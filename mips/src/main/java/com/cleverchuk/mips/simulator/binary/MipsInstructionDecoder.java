package com.cleverchuk.mips.simulator.binary;

import java.util.*;

/**
 * Robust decoder for MIPS32 Release 6 instructions.
 * Uses a multi-level hierarchical map: opcode -> partialEncoding -> operand flags
 * Optimized for memory efficiency while maintaining fast O(1) average lookup.
 */
public class MipsInstructionDecoder {

  // Bit masks for instruction fields
  private static final int OPCODE_MASK = 0xFC000000;      // bits 31-26
  private static final int RS_MASK = 0x03E00000;          // bits 25-21
  private static final int RT_MASK = 0x001F0000;          // bits 20-16
  private static final int RD_MASK = 0x0000F800;          // bits 15-11
  private static final int SHAMT_MASK = 0x000007C0;       // bits 10-6
  private static final int FUNCT_MASK = 0x0000003F;       // bits 5-0

  /**
   * Node in the decoding tree
   * Each node either contains an opcode or points to more specific nodes
   */
  private static class DecoderNode {
    Opcode opcode;
    Map<Integer, DecoderNode> children;

    DecoderNode(Opcode opcode) {
      this.opcode = opcode;
    }

    DecoderNode() {
      this.children = new HashMap<>();
    }

    boolean isLeaf() {
      return opcode != null;
    }
  }

  /**
   * Three-level hierarchical map structure:
   * Level 1: Primary opcode (bits 31-26) -> DecoderNode
   * Level 2: Partial encoding (funct, RS, RT fields) -> DecoderNode
   * Level 3: Operand disambiguation (rs/rt/rd flags) -> Opcode
   */
  private static final Map<Integer, DecoderNode> DECODER_TREE = new HashMap<>();

  static {
    buildDecoderTree();
  }

  /**
   * Builds the three-level hierarchical decoding tree
   */
  private static void buildDecoderTree() {
    for (Opcode op : Opcode.values()) {
      // Skip IDIOM format - assembler pseudo-instructions
      if (op.format == InstructionFormat.IDIOM) {
        continue;
      }

      insertIntoTree(op);
    }
  }

  /**
   * Inserts an opcode into the hierarchical tree
   */
  private static void insertIntoTree(Opcode opcode) {
    // Level 1: Get or create node for primary opcode
    DecoderNode level1 = DECODER_TREE.computeIfAbsent(
        opcode.opcode,
        k -> new DecoderNode()
    );

    // Level 2: Use partialEncoding as key
    if (level1.children == null) {
      level1.children = new HashMap<>();
    }

    DecoderNode level2 = level1.children.computeIfAbsent(
        opcode.partialEncoding,
        k -> new DecoderNode()
    );

    // Level 3: Check if we need operand disambiguation
    if (level2.isLeaf()) {
      // Conflict - need to disambiguate by operands
      resolveConflict(level2, opcode);
    } else if (level2.children == null || level2.children.isEmpty()) {
      level2.opcode = opcode;
    } else {
      // There are already children, add with operand key
      int operandKey = makeOperandKey(opcode.rs, opcode.rt, opcode.rd);
      level2.children.put(operandKey, new DecoderNode(opcode));
    }
  }

  /**
   * Resolves conflict when two opcodes map to same node
   * Creates operand-based disambiguation
   */
  private static void resolveConflict(DecoderNode node, Opcode newOpcode) {
    Opcode existingOpcode = node.opcode;
    node.opcode = null;

    if (node.children == null) {
      node.children = new HashMap<>();
    }

    // Add both opcodes with operand keys
    int existingKey = makeOperandKey(existingOpcode.rs, existingOpcode.rt, existingOpcode.rd);
    int newKey = makeOperandKey(newOpcode.rs, newOpcode.rt, newOpcode.rd);

    node.children.put(existingKey, new DecoderNode(existingOpcode));
    node.children.put(newKey, new DecoderNode(newOpcode));
  }

  /**
   * Creates a compact key from operand flags
   * Uses 3 bits: bit 0 = rs, bit 1 = rt, bit 2 = rd
   */
  private static int makeOperandKey(boolean rs, boolean rt, boolean rd) {
    return (rs ? 1 : 0) | (rt ? 2 : 0) | (rd ? 4 : 0);
  }

  /**
   * Main decode method
   *
   * ALGORITHM:
   * 1. Extract primary opcode (bits 31-26) -> Level 1 lookup
   * 2. Extract partial encoding (funct/RS/RT) -> Level 2 lookup
   * 3. If needed, use operand presence -> Level 3 lookup
   */
  public static Opcode decode(int instruction) {
    // Level 1: Primary opcode lookup
    int primaryOpcode = instruction & OPCODE_MASK;
    DecoderNode level1 = DECODER_TREE.get(primaryOpcode);

    if (level1 == null) {
      return null;
    }

    // If it's a leaf, we found it
    if (level1.isLeaf()) {
      return level1.opcode;
    }

    // Level 2: Partial encoding lookup
    int partialEncoding = extractPartialEncoding(instruction, primaryOpcode);

    // Try exact match first
    DecoderNode level2 = level1.children.get(partialEncoding);

    // If no exact match, try the default (partialEncoding = 0)
    if (level2 == null) {
      level2 = level1.children.get(0);
    }

    if (level2 == null) {
      return null;
    }

    // If it's a leaf, we found it
    if (level2.isLeaf()) {
      return level2.opcode;
    }

    // Level 3: Operand-based disambiguation
    if (level2.children != null && !level2.children.isEmpty()) {
      // Try to find best match based on which operands are present
      return disambiguateByOperands(instruction, level2.children);
    }

    return level2.opcode;
  }

  /**
   * Extracts the partial encoding from instruction based on format
   * This is the "function field" or other disambiguating bits
   */
  private static int extractPartialEncoding(int instruction, int primaryOpcode) {
    // For R-Type (opcode = 0x0), use function field
    if (primaryOpcode == 0x0) {
      // Check bits 10-6 for shift amount variations (like ROTR)
      int functExtended = instruction & 0x000007FF; // bits 10-0
      int funct = instruction & FUNCT_MASK; // bits 5-0

      // Some instructions use extended function field
      // Check if any bits in 10-6 are set
      if ((instruction & 0x000007C0) != 0) {
        return functExtended;
      }
      return funct;
    }

    // For SPECIAL3 (opcode = 0x7C000000), use function field
    if (primaryOpcode == 0x7C000000) {
      return instruction & FUNCT_MASK;
    }

    // For SPECIAL2 (opcode = 0x70000000)
    if (primaryOpcode == 0x70000000) {
      return instruction & FUNCT_MASK;
    }

    // For REGIMM (opcode = 0x04000000), use RT field
    if (primaryOpcode == 0x04000000) {
      return instruction & RT_MASK;
    }

    // For COP0 (opcode = 0x40000000)
    if (primaryOpcode == 0x40000000) {
      // COP0 uses lower 26 bits (RS field + rest)
      return instruction & 0x03FFFFFF;
    }

    // For COP1 (opcode = 0x44000000) - FPU instructions
    // Format: opcode|format|ft|fs|fd|funct
    // We need format (bits 25-21) + funct (bits 5-0)
    if (primaryOpcode == 0x44000000) {
      int format = instruction & RS_MASK; // bits 25-21 (RS position is format field)
      int funct = instruction & FUNCT_MASK; // bits 5-0
      return format | funct;
    }

    // For COP2 (opcode = 0x48000000)
    if (primaryOpcode == 0x48000000) {
      // Similar to COP1, use format + function
      int format = instruction & RS_MASK;
      int funct = instruction & FUNCT_MASK;
      return format | funct;
    }

    // For I-Type with RT/RS disambiguation
    if ((instruction & RT_MASK) != 0 || (instruction & RS_MASK) != 0) {
      // Some I-Type instructions use RT or RS for disambiguation
      return (instruction & RT_MASK) | (instruction & RS_MASK);
    }

    return 0;
  }

  /**
   * Disambiguates by checking which operands are present in instruction
   */
  private static Opcode disambiguateByOperands(int instruction, Map<Integer, DecoderNode> children) {
    // Extract which registers are actually used (non-zero)
    int rs = (instruction & RS_MASK) >> 21;
    int rt = (instruction & RT_MASK) >> 16;
    int rd = (instruction & RD_MASK) >> 11;

    // Try all possible operand combinations, prioritizing more specific ones
    for (int rdFlag = 1; rdFlag >= 0; rdFlag--) {
      for (int rtFlag = 1; rtFlag >= 0; rtFlag--) {
        for (int rsFlag = 1; rsFlag >= 0; rsFlag--) {
          int key = rsFlag | (rtFlag << 1) | (rdFlag << 2);
          DecoderNode node = children.get(key);
          if (node != null && node.isLeaf()) {
            return node.opcode;
          }
        }
      }
    }

    // Fallback: return any available
    for (DecoderNode node : children.values()) {
      if (node.isLeaf()) {
        return node.opcode;
      }
    }

    return null;
  }

  /**
   * Helper method to extract and format instruction fields
   */
  public static String disassemble(int instruction, Opcode opcode) {
    if (opcode == null) {
      return String.format("UNKNOWN [0x%08X]", instruction);
    }

    int rs = (instruction & RS_MASK) >> 21;
    int rt = (instruction & RT_MASK) >> 16;
    int rd = (instruction & RD_MASK) >> 11;
    int shamt = (instruction & SHAMT_MASK) >> 6;
    short imm = (short)(instruction & 0x0000FFFF); // Sign-extended
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
        if (shamt != 0 && !opcode.rs) {
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
   * Prints the decoder tree structure for analysis
   */
  public static void printTreeStructure() {
    System.out.println("=== DECODER TREE STRUCTURE ===\n");

    List<Integer> opcodes = new ArrayList<>(DECODER_TREE.keySet());
    opcodes.sort(Integer::compareTo);

    for (Integer opcode : opcodes) {
      System.out.printf("Opcode 0x%08X:%n", opcode);
      DecoderNode node = DECODER_TREE.get(opcode);
      printNode(node, "  ");
      System.out.println();
    }

    // Memory footprint estimate
    int level1Nodes = DECODER_TREE.size();
    int level2Nodes = 0;
    int level3Nodes = 0;

    for (DecoderNode node : DECODER_TREE.values()) {
      if (node.children != null) {
        level2Nodes += node.children.size();
        for (DecoderNode child : node.children.values()) {
          if (child.children != null) {
            level3Nodes += child.children.size();
          }
        }
      }
    }

    System.out.printf("Memory footprint: %d level-1 nodes, %d level-2 nodes, %d level-3 nodes%n",
        level1Nodes, level2Nodes, level3Nodes);
    System.out.printf("Total nodes: %d%n", level1Nodes + level2Nodes + level3Nodes);
  }

  private static void printNode(DecoderNode node, String indent) {
    if (node.isLeaf()) {
      System.out.printf("%s-> %s (rs=%b, rt=%b, rd=%b)%n",
          indent, node.opcode.name, node.opcode.rs, node.opcode.rt, node.opcode.rd);
    } else if (node.children != null) {
      for (Map.Entry<Integer, DecoderNode> entry : node.children.entrySet()) {
        System.out.printf("%sKey 0x%08X:%n", indent, entry.getKey());
        printNode(entry.getValue(), indent + "  ");
      }
    }
  }
}