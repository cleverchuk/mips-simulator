package com.cleverchuk.mips;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import com.cleverchuk.mips.compiler.MipsCompiler;
import com.cleverchuk.mips.compiler.codegen.CodeGenerator;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.parser.RecursiveDescentParser;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.FourOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.InstructionAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.OneOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ThreeOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.TwoOpAnalyzer;
import com.cleverchuk.mips.compiler.semantic.instruction.ZeroOpAnalyzer;
import com.cleverchuk.mips.simulator.MipsSimulator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("All")
@RunWith(AndroidJUnit4.class)
public class MipsSimulatorTest {

    private MipsSimulator mipsSimulator;

    private Context context;

    private final String[] instructions = {
            ".text",
            "add $t0, $t1, $t2 # comment",
            "addi $t0, $t1, 400",
            "beq $t0, $t1, label",
            "lw $t0, 2($t1   )",
            "sw $t0, 67 (   $sp )",
            "li $t0, 300",
            "la $t0, label # comment",
            "jal label",
            "return:jr $ra",
            "addi $t0, $zero, 300",
            "add $t0, $t1,             $zero",
            "li $v0,                       1",
            "syscall",
            "             "
    };

    @Before
    public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        RecursiveDescentParser parser = new RecursiveDescentParser(new MipsLexer(), new SemanticAnalyzer(new InstructionAnalyzer(
                new ZeroOpAnalyzer(),
                new OneOpAnalyzer(),
                new TwoOpAnalyzer(new TwoOpAnalyzer.LoadStoreAnalyzer(), new TwoOpAnalyzer.TwoRegOpcodeAnalyzer(), new TwoOpAnalyzer.BranchOpcodeAnalyzer()),
                new ThreeOpAnalyzer(new ThreeOpAnalyzer.ShiftRotateAnalyzer(), new ThreeOpAnalyzer.ConditionalTestingAndMoveAnalyzer(),
                        new ThreeOpAnalyzer.ArithmeticAndLogicalOpcodeAnalyzer()),
                new FourOpAnalyzer()
        )));
        mipsSimulator = new MipsSimulator(new Handler(context.getMainLooper()), new MipsCompiler(parser, new CodeGenerator()));
        mipsSimulator.start();
    }

    @After
    public void tearDown() {
        if (mipsSimulator != null) {
            mipsSimulator.shutDown();
        }
    }


    public String toLineDelimited(String[] array) {

        StringBuilder builder = new StringBuilder();
        for (String ins :
                array) {
            builder.append(ins)
                    .append('\n');
        }

        return builder.toString();
    }

    @Test
    public void testLw() {
        String[] instructions = {
                ".data",
                "label: .word 5,6,7",
                ".text",
                "li $t0, 300",
                "la $s1, label",
                "lw $s1, 0($s1)"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(300, val);
        val = mipsSimulator.registerFile.readWord("$s0");
        assertEquals(0, val);
        val = mipsSimulator.registerFile.readWord("$s1");
        assertEquals(5, val);
    }

    @Test
    public void testCodeWithGlobl() {
        String[] instructions = {
                ".data",
                "label: .word 5,6,7",
                ".text",
                ".globl main",
                "main: ",
                "li $t0, 300",
                "la $s1, label",
                "lw $s1, 0($s1)"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(300, val);
        val = mipsSimulator.registerFile.readWord("$s0");
        assertEquals(0, val);
        val = mipsSimulator.registerFile.readWord("$s1");
        assertEquals(5, val);
    }

    @Test
    public void testSw() {
        String[] instructions = {
                ".data",
                "label: .word 30, 50,10",
                ".text",
                "li $t0, 300",
                "la $t1, label",
                "lw $t2, 4($t1)",
                "add $t0, $t0, $t2",
                "sw $t0, 4($t1)"

        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(350, val);
        val = mipsSimulator.registerFile.readWord("$t2");
        assertEquals(50, val);
    }

    @Test
    public void testMul() {
        String[] instructions = {
                ".text",
                "li $t0, 3",
                "li $t1, 4",
                "mul $s0, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(12, mipsSimulator.registerFile.readWord("$s0"), 0.0);
    }

    @Test
    public void testDiv() {
        String[] instructions = {
                ".text",
                "li $t0, 12",
                "li $t1, 4",
                "div $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(3, mipsSimulator.registerFile.accLO());
    }


    @Test
    public void testAdd() {
        String[] instructions = {
                ".text",
                "li $t1, 500",
                "li $t0, 300",
                "add $s1, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(800, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testAddi() {
        String[] instructions = {
                ".text",
                "addi $t0, $t0, 300",
                "addi $s1, $t0, 54"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(300, mipsSimulator.registerFile.readWord("$t0"), 0.0);
        assertEquals(354, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }


    @Test
    public void testSub() {
        String[] instructions = {
                ".text",
                "li $t0, 3",
                "li $t1, 4",
                "sub $s0, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-1, mipsSimulator.registerFile.readWord("$s0"), 0.0);
    }

    @Test
    public void testSubu() {
        String[] instructions = {
                ".text",
                "li $t0, 3",
                "li $t1, 4",
                "subu $s0, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-1, mipsSimulator.registerFile.readWord("$s0"), 0.0);
    }

    @Test
    public void testLi() {
        String[] instructions = {
                ".text",
                "li $t1, 500",
                "li $t0, 300",
                "add $s1, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(800, mipsSimulator.registerFile.readWord("$s1"), 0.0);

    }

    @Test
    public void testLa() {
        String[] instructions = {
                ".text",
                "label: li $t1, 500",
                "la $t0, label",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(0, mipsSimulator.registerFile.readWord("$t0"), 0.0);

    }


    @Test
    public void testAddiu() {
        String[] instructions = {
                ".text",
                "addiu $t0, $t0, 300",
                "addiu $s1, $t0, 54"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(300, mipsSimulator.registerFile.readWord("$t0"), 0.0);
        assertEquals(354, mipsSimulator.registerFile.readWord("$s1"), 0.0);

    }

    @Test
    public void testAddu() {
        String[] instructions = {
                ".text",
                "li $t1, 500",
                "li $t0, 300",
                "addu $s1, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(800, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testClo() {
        String[] instructions = {
                ".text",
                "addiu $t0, $t0, 10",
                "clo $s1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(1, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testClz() {
        String[] instructions = {
                ".text",
                "addiu $t0, $t0, 1",
                "clz $s1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(31, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testLui() {
        String[] instructions = {
                ".text",
                "lui $s1, 300"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(300 << 16, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testMove() {
        String[] instructions = {
                ".text",
                "li $s1, 300",
                "move $t0, $s1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(300, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testNegu() {
        String[] instructions = {
                ".text",
                "li $s1, 300",
                "negu $t0, $s1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-300, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSll() {
        String[] instructions = {
                ".text",
                "li $t0, 300",
                "sll $t0, $t0, 3"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(300 << 3, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSrl() {
        String[] instructions = {
                ".text",
                "li $t0, -300",
                "srl $t0, $t0, 3"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertTrue(mipsSimulator.registerFile.readWord("$t0") > 0);
    }

    @Test
    public void testBne() {
        String[] instructions = {
                ".text",
                "li $t1, 1",
                "bne $t1, $t0, 1",
                "li $t0, 300",
                "addi $s1, $t0, 54"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(54, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testBeq() {
        String[] instructions = {
                ".text",
                "beq $t1, $t0, 1",
                "li $t0, 300",
                "addi $s1, $t0, 54"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(54, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testJ() {
        String[] instructions = {
                ".text",
                "j label",
                "li $t0, 300",
                "label: addi $s1, $t0, 54"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(54, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testJr() {
        String[] instructions = {
                ".text",
                "jal label",
                "li $v0, 10",
                "syscall",
                "label: addi $s1, $t0, 54",
                "jr $ra"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(54, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testJal() {
        String[] instructions = {
                ".text",
                "jal label",
                "li $v0, 10",
                "syscall",
                "label: addi $s1, $t0, 54",
                "jr $ra"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(54, mipsSimulator.registerFile.readWord("$s1"), 0.0);
    }

    @Test
    public void testSllv() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 5",
                "sllv $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(20, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testMovn() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 5",
                "movn $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(5, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testMovz() {
        String[] instructions = {
                ".text",
                "li $t0, 0",
                "li $t1, 5",
                "movz $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(5, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSlt() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 5",
                "slt $t0, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(1, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSltu() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, -5",
                "sltu $t0, $t0, $t1"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(1, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSlti() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 5",
                "slti $t0, $t1, 17"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(1, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSltiu() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, -15",
                "sltiu $t0, $t1, 10"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(0, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testAnd() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 0",
                "and $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(0, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testAndi() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "andi $t0, $t0, 3"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testNor() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 3",
                "nor $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-4, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testNot() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "not $t0, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-3, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testOr() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 3",
                "or $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(3, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testOri() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "ori $t0, $t0, 10"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(10, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testXor() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "li $t1, 3",
                "xor $t0, $t1, $t0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(1, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testXori() {
        String[] instructions = {
                ".text",
                "li $t0, 2",
                "xori $t0, $t0, 0"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testExt() {
        Log.i("-15 bit string", Integer.toBinaryString(-15));
        String[] instructions = {
                ".text",
                "li $t0, -15",
                "ext $t0, $t0, 0, 5"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(17, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testIns() {
        String[] instructions = {
                ".text",
                "li $t0, -15",
                "ins $t0, $t0, 0, 2"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-15, mipsSimulator.registerFile.readWord("$t0"), 0.0);

        instructions = new String[]{
                ".text",
                "li $t0, 15",
                "li $t1, 10",
                "ins $t0, $t1, 1, 3"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(11, mipsSimulator.registerFile.readWord("$t0"), 0.0);
    }

    @Test
    public void testSpaceStorage() {
        String[] instructions = {
                ".data",
                "bytes: .space 12",
                ".text",
                "la $t4, bytes",
                "li $t3, 20",
                "sw $t3, 0($t4)",
                "li $t3, 21",
                "sw $t3, 4($t4)",
                "li $t3, 22",
                "sw $t3, 8($t4)",
                "lw $t0, 0($t4)",
                "lw $t1, 4($t4)",
                "lw $t2, 8($t4)",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(20, val);
        val = mipsSimulator.registerFile.readWord("$t1");

        assertEquals(21, val);
        val = mipsSimulator.registerFile.readWord("$t2");
        assertEquals(22, val);
    }

    @Test
    public void testlb() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "lb $t0, 0($t4)",
                "lb $t1, 4($t4)"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(1, val);
        val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(5, val);
    }

    @Test
    public void testlbu() {
        String[] instructions = {
                ".data",
                "bytes: .byte -1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "lbu $t0, 0($t4)",
                "lbu $t1, 4($t4)"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");

        assertTrue(val > 0);
        val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(5, val);
    }

    @Test
    public void testlh() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "lh $t0, 0($t4)",
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(258, val);
    }

    @Test
    public void testlhu() {
        String[] instructions = {
                ".data",
                "bytes: .byte -1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "lhu $t0, 0($t4)"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(65282, val);
    }

    @Test
    public void testlwl() {
        String[] instructions = {
                ".data",
                "bytes: .byte 0, 0, 0, 4, 5",
                ".text",
                "la $t4, bytes",
                "lwl $t0, 0($t4)",
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(4, val);
    }

    @Test
    public void testlwr() {
        String[] instructions = {
                ".data",
                "bytes: .byte 0, 0, 0, 4, 5",
                ".text",
                "la $t4, bytes",
                "lwr $t0, 0($t4)",
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(4, val);
    }

    @Test
    public void testsb() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "li $t0, 10",
                "sb $t0, 0($t4)",
                "lb $t1, 0($t4)"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(10, val);
    }

    @Test
    public void testsh() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "li $t0, 10",
                "sh $t0, 0($t4)",
                "lh $t1, 0($t4)"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(10, val);
    }

    @Test
    public void testswl() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "li $t0, 10",
                "swl $t0, 0($t4)",
                "lwl $t1, 0($t4)"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(10, val);
    }

    @Test
    public void testswr() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "li $t0, 10",
                "swr $t0, 0($t4)",
                "lwr $t1, 0($t4)"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;

        long val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(10, val);
    }

    @Test
    public void testulw() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 0, 0, 0, 5",
                ".text",
                "la $t4, bytes",
                "ulw $t0, 1($t4)",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(5, val);
    }

    @Test
    public void testusw() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "la $t4, bytes",
                "li $t0, 10",
                "usw $t0, 0($t4)",
                "ulw $t1, 0($t4)"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t1");
        assertEquals(10, val);
    }

    @Test
    public void testb() {
        String[] instructions = {
                ".data",
                "bytes: .byte 1, 2, 3, 4, 5",
                ".text",
                "b 1",
                "la $t4, bytes",
                "li $t0, 10"
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(10, mipsSimulator.registerFile.readWord("$t0"));
        assertEquals(0, mipsSimulator.registerFile.readWord("$t4"));
    }

    @Test
    public void testbal() {
        String[] instructions = {
                ".text",
                "bal 2"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(3, mipsSimulator.getPC());
        long val = mipsSimulator.registerFile.readWord("$ra");
        assertEquals(2, val);

    }

    @Test
    public void testbeqz() {
        String[] instructions = {
                ".text",
                "li $t0, 0",
                "beqz $t0, 4"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(6, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 1",
                "beqz $t0, 4"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testbgez() {
        String[] instructions = {
                ".text",
                "li $t0, 10",
                "bgez $t0, 10"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(12, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, -10",
                "bgez $t0, 10"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testbgezal() {
        String[] instructions = {
                ".text",
                "li $t0, 10",
                "bgezal $t0, 10"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$ra");
        assertEquals(3, val);
        assertEquals(12


                , mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, -10",
                "bgezal $t0, 10"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        val = mipsSimulator.registerFile.readWord("$ra");
        assertEquals(3, val);
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testbgtz() {
        String[] instructions = {
                ".text",
                "li $t0, 10",
                "bgtz $t0, 20"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(22, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 0",
                "bgtz $t0, 20"
        };

        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testblez() {
        String[] instructions = {
                ".text",
                "li $t0, 0",
                "blez $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(12, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 10",
                "blez $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testbltz() {
        String[] instructions = {
                ".text",
                "li $t0, -10",
                "blez $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(12, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 0",
                "bltz $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testbnez() {
        String[] instructions = {
                ".text",
                "li $t0, -10",
                "bnez $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(12, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 10",
                "bnez $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(12, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 0",
                "bnez $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testbltzal() {
        String[] instructions = {
                ".text",
                "li $t0, -10",
                "bltzal $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$ra");
        assertEquals(3, val);
        assertEquals(12, mipsSimulator.getPC());

        instructions = new String[]{
                ".text",
                "li $t0, 10",
                "bltzal $t0, 10",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        val = mipsSimulator.registerFile.readWord("$ra");
        assertEquals(3, val);
        assertEquals(2, mipsSimulator.getPC());
    }

    @Test
    public void testjalr() {
        String[] instructions = {
                ".text",
                "li $t0, 10",
                "jalr $t1, $t0",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(10, mipsSimulator.getPC());
        assertEquals(3, mipsSimulator.registerFile.readWord("$t1"));
    }

    @Test
    public void testdivu() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "divu $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(Integer.toUnsignedLong(-2) % 2, mipsSimulator.registerFile.accHI());
        assertEquals(Integer.toUnsignedLong(-2) / 2, mipsSimulator.registerFile.accLO());
    }

    @Test
    public void testmadd() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "madd $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-4, mipsSimulator.registerFile.getAccumulator());
    }

    @Test
    public void testmaddu() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "maddu $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(Integer.toUnsignedLong(-2) * 2, mipsSimulator.registerFile.getAccumulator());
    }

    @Test
    public void testmsub() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "msub $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(4, mipsSimulator.registerFile.getAccumulator());
    }

    @Test
    public void testmsubu() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "msubu $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(Integer.toUnsignedLong(-2) * -2, mipsSimulator.registerFile.getAccumulator());
    }

    @Test
    public void testmult() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "mult $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-4, mipsSimulator.registerFile.getAccumulator());
    }

    @Test
    public void testmultu() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "li $t1, 2",
                "multu $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(Integer.toUnsignedLong(-2) * 2, mipsSimulator.registerFile.getAccumulator());
    }

    @Test
    public void testmfhi() {
        String[] instructions = {
                ".text",
                "li $t0, 3",
                "li $t1, 2",
                "div $t0, $t1",
                "mfhi $t0",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(1, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testmflo() {
        String[] instructions = {
                ".text",
                "li $t0, 4",
                "li $t1, 2",
                "div $t0, $t1",
                "mflo $t0",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(2, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testmthi() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "mthi $t0",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-2, mipsSimulator.registerFile.accHI());
    }

    @Test
    public void testmtlo() {
        String[] instructions = {
                ".text",
                "li $t0, -2",
                "mtlo $t0",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-2, mipsSimulator.registerFile.accLO());
    }

    @Test
    public void testrotr() {
        String[] instructions = {
                ".text",
                "li $t1, -2147483646",
                "rotr $t0, $t1, 2",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-1610612736, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testrotrv() {
        String[] instructions = {
                ".text",
                "li $t1, 2",
                "li $t2, 2",
                "rotrv $t0, $t1, $t2",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(Integer.MIN_VALUE, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testsra() {
        String[] instructions = {
                ".text",
                "li $t1, -2",
                "sra $t0, $t1, 2",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-1, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testsrav() {
        String[] instructions = {
                ".text",
                "li $t1, -2",
                "li $t2, 2",
                "srav $t0, $t1, $t2",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-1, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testsrlv() {
        String[] instructions = {
                ".text",
                "li $t1, -2",
                "li $t2, 2",
                "srlv $t0, $t1, $t2",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertTrue(mipsSimulator.registerFile.readWord("$t0") > 0);
    }

    @Test
    public void testseb() {
        String[] instructions = {
                ".text",
                "li $t1, -3",
                "seb $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-3, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testseh() {
        String[] instructions = {
                ".text",
                "li $t1, -3",
                "seh $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(-3, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testwsbh() {
        String[] instructions = {
                ".text",
                "li $t1, 128",
                "wsbh $t0, $t1",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        assertEquals(32768, mipsSimulator.registerFile.readWord("$t0"));
    }

    @Test
    public void testll() {
        String[] instructions = {
                ".data",
                "bytes: .byte 0, 0, 0, 1, 5",
                ".text",
                "la $t4, bytes",
                "ll $t0, 0($t4)",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(1, val);
    }

    @Test
    public void testsc() {
        String[] instructions = {
                ".data",
                "bytes: .byte 0, 0, 0, 1, 5",
                ".text",
                "la $t4, bytes",
                "li $t1, 2",
                "sc $t1, 0($t4)",
                "ll $t0, 0($t4)",
        };
        mipsSimulator.loadInstructions(toLineDelimited(instructions), new SparseIntArray());
        mipsSimulator.running();
        while (mipsSimulator.isRunning()) ;
        long val = mipsSimulator.registerFile.readWord("$t0");
        assertEquals(2, val);
    }
}
