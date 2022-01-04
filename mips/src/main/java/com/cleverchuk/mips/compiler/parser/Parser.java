package com.cleverchuk.mips.compiler.parser;

import com.cleverchuk.mips.simulator.Instruction;
import com.cleverchuk.mips.simulator.Opcode;
import com.cleverchuk.mips.simulator.storage.SpaceStorage;
import com.cleverchuk.mips.simulator.storage.Storage;
import com.cleverchuk.mips.simulator.storage.StorageType;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public final class Parser {
    private Parser() {
    }

    private static final Pattern DATA_PATTERN = Pattern.compile("\\s*(\\w+:)\\s+\\.(\\w+\\b)\\s+(.+)\\n?");

    private static final Pattern R_TYPE_INSTRUCTION_PATTERN = Pattern.compile(
            "\\s*([a-z]+\\b)\\s+(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$[sg]p)\\s*,\\s*(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$zero|\\$[sg]p)\\s*,\\s*(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$zero|\\$[sg]p)\\s*");

    private static final Pattern I_TYPE_INSTRUCTION_PATTERN = Pattern.compile(
            "\\s*([a-z]+\\b)\\s+(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$[sg]p)\\s*,\\s*(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$zero|\\$[sg]p)\\s*,\\s*(\\w+\\b|-?\\d+)");

    private static final Pattern M_TYPE_INSTRUCTION_PATTERN = Pattern.compile(
            "\\s*([a-z]+\\b)\\s+(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$[sg]p)\\s*,\\s*(\\d+\\s*\\(\\s*\\$[0-9]{1,2}\\s*\\)|\\d+\\s*\\(\\s*\\$[sg]p\\s*\\)|\\d+\\s*\\(\\s*\\$[astv][0-9]{1,2}\\s*\\))");

    private static final Pattern J_TYPE_INSTRUCTION_PATTERN = Pattern.compile("\\s*([a-z]+\\b)\\s+(\\$?[^#\"]+\\b)");

    private static final Pattern TWO_OPERAND_INSTRUCTION_PATTERN =
            Pattern.compile(
                    "\\s*([a-z]+\\b)\\s+(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$zero|\\$[sg]p)\\s*,\\s*(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$zero|\\$[sg]p|\\w+\\b|-?\\d+)");

    private static final Pattern SYSCALL_PATTERN = Pattern.compile("\\s*(syscall)");

    private static final Pattern NOP_PATTERN = Pattern.compile("\\s*(nop)");

    private static final Pattern FOUR_OPERAND_INSTRUCTION = Pattern.compile(
            "\\s*([a-z]+\\b)\\s+(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$[sg]p)\\s*," +
                    "\\s*(\\$ra|\\$[astv][0-9]{1,2}|\\$[0-9]{1,2}|\\$zero|\\$[sg]p)\\s*," +
                    "\\s*([0-9]{1,2})\\s*," +
                    "\\s*([1-9]{1,2})\\s*");

    public static Instruction parseToInstruction(String line, int pos) throws Exception {
        line = stripLabel(line);
        Matcher rMatcher = R_TYPE_INSTRUCTION_PATTERN.matcher(line),
                iMatcher = I_TYPE_INSTRUCTION_PATTERN.matcher(line),
                mMatcher = M_TYPE_INSTRUCTION_PATTERN.matcher(line),
                twoOpMatcher = TWO_OPERAND_INSTRUCTION_PATTERN.matcher(line),
                jMatcher = J_TYPE_INSTRUCTION_PATTERN.matcher(line),
                sMatcher = SYSCALL_PATTERN.matcher(line),
                nopMatcher = NOP_PATTERN.matcher(line),
                fMatcher = FOUR_OPERAND_INSTRUCTION.matcher(line);

        Instruction.InstructionBuilder instructionBuilder = Instruction.builder();
        if (line.isEmpty() || line.startsWith("#") || nopMatcher.find()) {
            instructionBuilder.opcode(Opcode.NOP);

        } else if (sMatcher.find()) {
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(sMatcher.group(1))));

        } else if (fMatcher.find()) {
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(fMatcher.group(1))));
            instructionBuilder.rd(fMatcher.group(2));
            instructionBuilder.rs(fMatcher.group(3));
            instructionBuilder.pos(Integer.parseInt(Objects.requireNonNull(fMatcher.group(4))));
            instructionBuilder.size(Integer.parseInt(Objects.requireNonNull(fMatcher.group(5))));

        } else if (rMatcher.find()) {
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(rMatcher.group(1))));
            instructionBuilder.rd(rMatcher.group(2));
            instructionBuilder.rs(rMatcher.group(3));
            instructionBuilder.rt(rMatcher.group(4));

        } else if (iMatcher.find()) {// register instruction
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(iMatcher.group(1))));
            instructionBuilder.rd(iMatcher.group(2));
            instructionBuilder.rs(iMatcher.group(3));
            String immediateValue = iMatcher.group(4);

            if (immediateValue != null) // Arithmetic immediate / shift instruction
            {
                try {

                    instructionBuilder.immediateValue(Integer.parseInt(immediateValue));

                } catch (NumberFormatException e) {
                    instructionBuilder.label(immediateValue); // branch instruction
                    instructionBuilder.rt(iMatcher.group(3));
                    instructionBuilder.rs(iMatcher.group(2));
                    instructionBuilder.rd(null);
                }
            }


        } else if (mMatcher.find()) {
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(mMatcher.group(1))));
            instructionBuilder.rd(mMatcher.group(2));
            String token = mMatcher.group(3);

            if (token != null) {
                if (token.contains("(")) { // sw, lw with offset
                    int indexOfOpenParen = token.indexOf('(');
                    try {
                        instructionBuilder.offset(Integer.parseInt(token.substring(0, indexOfOpenParen)
                                .trim()));
                        indexOfOpenParen++;

                    } catch (NumberFormatException e) {
                        throw new Exception("illFormed instruction: " + line);
                    }

                    while (token.charAt(indexOfOpenParen) == ' ') {
                        indexOfOpenParen++; // remove spaces
                    }
                    instructionBuilder.rs(token.substring(indexOfOpenParen, indexOfOpenParen + 3));

                } else {// lw
                    instructionBuilder.label(token);
                }
            }
        } else if (twoOpMatcher.find()) {
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(twoOpMatcher.group(1))));
            instructionBuilder.rd(twoOpMatcher.group(2));
            String token = twoOpMatcher.group(3);
            try {
                instructionBuilder.immediateValue(Integer.parseInt(Objects.requireNonNull(token)));
            } catch (NumberFormatException e) {
                if (Objects.requireNonNull(token)
                        .charAt(0) == '$' && token.length() == 3) {
                    instructionBuilder.rs(token);
                } else {
                    instructionBuilder.label(token);
                }
            }

        } else if (jMatcher.find()) { // jump instructions
            instructionBuilder.opcode(Opcode.parse(Objects.requireNonNull(jMatcher.group(1))));
            instructionBuilder.label(jMatcher.group(2));

        } else {
            throw new Exception("Unknown instruction/unrecognized character on line: " + pos);
        }

        return instructionBuilder.build();
    }

    @Deprecated
    public static Object parseToData(String line) throws Exception {
        Matcher matcher = DATA_PATTERN.matcher(line);
        if (matcher.find()) {
            String dataType = matcher.group(2);
            String value = matcher.group(3);

            if (dataType != null && value != null) {
                switch (dataType) {
                    case "asciiz":
                        return value.substring(1, value.length() - 1)
                                .toCharArray();

                    case "word":
                        return value.trim()
                                .split("[, ]");

                    default:
                        throw new Exception("unknown data type: " + dataType);
                }
            }
        }
        return null;
    }


    public static Storage parseStringToData(String line) throws Exception {
        Matcher matcher = DATA_PATTERN.matcher(line);

        if (line.isEmpty()) {
            return new Storage(line, StorageType.EMPTY);
        }
        if (matcher.find()) {
            String dataType = matcher.group(2);
            String value = matcher.group(3);

            if (dataType != null && value != null) {
                switch (dataType) {
                    case "asciiz":
                        int start = value.indexOf('"') + 1, end = value.lastIndexOf('"');
                        return new Storage(value.substring(start, end), StorageType.STRING);

                    case "word": {
                        String[] array = Arrays.stream(value.trim()
                                .split("[, ]"))
                                .filter(str -> !str.isEmpty())
                                .toArray(String[]::new);
                        return new Storage(array, StorageType.WORD);
                    }
                    case "space":
                        return new SpaceStorage(new byte[Integer.parseInt(value.trim())], StorageType.SPACE);
                    default:
                        throw new Exception(String.format("unknown storage class: %s", dataType));
                }
            }
        }
        throw new Exception("unknown token");
    }

    private static String stripLabel(String line) {
        if (line.contains(":")) {
            int indexOfColon = line.indexOf(':') + 1;
            return line.substring(indexOfColon)
                    .trim();
        }

        return line.trim();
    }

}
