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

import androidx.annotation.NonNull;
import com.cleverchuk.mips.compiler.lexer.MipsLexer;
import com.cleverchuk.mips.compiler.lexer.Token;
import com.cleverchuk.mips.compiler.lexer.TokenType;
import com.cleverchuk.mips.compiler.semantic.SemanticAnalyzer;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;

import static com.cleverchuk.mips.compiler.parser.NodeType.NONTERMINAL;
import static com.cleverchuk.mips.compiler.parser.NodeType.TERMINAL;


public final class RecursiveDescentParser {
    private final MipsLexer lexer;

    private final SemanticAnalyzer semanticAnalyzer;


    private final List<NodeVisitor> visitors = new LinkedList<>();

    private Token ll1;

    public RecursiveDescentParser(MipsLexer lexer, SemanticAnalyzer semanticAnalyzer) {
        this(lexer, semanticAnalyzer, (node) -> {
        });
    }

    @Inject
    public RecursiveDescentParser(MipsLexer lexer, SemanticAnalyzer semanticAnalyzer, NodeVisitor visitor) {
        this.lexer = lexer;
        this.semanticAnalyzer = semanticAnalyzer;
        visitors.add(visitor);
        visitors.add(new PseudoTransformer());
    }

    @NonNull
    public Node parse(String source) {
        SymbolTable.clear();
        ErrorRecorder.clear();
        lexer.tokenize(source.toCharArray());
        return program();
    }

    private void visit(Node node) {
        visitors.forEach(visitor -> {
            try {
                visitor.visit(node);
            } catch (Exception ignore) {

            }
        });
    }

    public void addVisitor(NodeVisitor visitor) {
        visitors.add(visitor);
    }

    public boolean removeVisitor(NodeVisitor visitor) {
        return visitors.remove(visitor);
    }

    private Node program() {
        Node program = Node.builder()
                .construct(Construct.PROGRAM)
                .nodeType(NONTERMINAL)
                .build();

        Node segment = segment();
        program.addChild(segment);
        visit(program);
        return program;
    }

    private Node segment() {
        Node segment = Node.builder()
                .nodeType(NONTERMINAL)
                .construct(Construct.SEGMENT)
                .build();

        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.DOT) {
            ll1 = lexer.getNextToken();
            if (ll1.getTokenType() == TokenType.DATA) {
                lexer.reset(0);
                segment.addChild(dataSeg());
                if (ll1.getTokenType() == TokenType.EOF) {
                    visit(segment);
                    return segment;
                }
                segment.addChild(textSeg());
            }

            if (ll1.getTokenType() == TokenType.TEXT) {
                lexer.reset(0);
                segment.addChild(textSeg());
                if (ll1.getTokenType() == TokenType.EOF) {
                    visit(segment);
                    return segment;
                }
                segment.addChild(dataSeg());
            }
        } else {
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .msg("Malformed program")
                    .build());
        }

        if (ll1.getTokenType() == TokenType.EOF) {
            visit(segment);
            return segment;
        }

        ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                .line(ll1.getLine())
                .msg("Incorrect declaration")
                .build());
        return errorRecovery();
    }

    private Node dataSeg() {
        Node dataSeg = Node.builder()
                .nodeType(NONTERMINAL)
                .construct(Construct.DATASEG)
                .build();

        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.EOF) {
            visit(dataSeg);
            return dataSeg;
        }

        if (ll1.getTokenType() == TokenType.DOT &&
                lexer.getNextToken().getTokenType() != TokenType.DATA) {
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Missing .data segment")
                    .build());
            return errorRecovery();
        }

        dataSeg.setLine(ll1.getLine());
        dataSeg.addChild(greedyDataDecl());
        visit(dataSeg);

        return dataSeg;
    }

    private Node greedyDataDecl() {
        Node greedyRoot = Node.builder()
                .nodeType(NONTERMINAL)
                .construct(Construct.DATADECLS)
                .build();

        Node dataDecl = dataDecl();
        if (dataDecl != null) {
            greedyRoot.addChild(dataDecl);
            Node greedyDataDecl = greedyDataDecl();
            greedyRoot.addChild(greedyDataDecl);
        }

        visit(greedyRoot);
        return greedyRoot;
    }

    private Node dataDecl() {
        Node dataDecl = Node.builder()
                .nodeType(NONTERMINAL)
                .construct(Construct.DATADECL)
                .build();

        Node label = label();
        if (label != null) {
            dataDecl.addChild(label);
            Node data = data();
            dataDecl.addChild(data);

            visit(dataDecl);
            return dataDecl;
        }
        return null;
    }

    private Node label() {
        Node label = Node.builder()
                .nodeType(NONTERMINAL)
                .construct(Construct.LABEL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.ID || ll1.getTokenType() == TokenType.DECI) {
            Token currentToken = ll1;
            ll1 = lexer.getNextToken();
            if (ll1.getTokenType() == TokenType.COLON) {
                label.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .line(currentToken.getLine())
                        .value(currentToken.getValue())
                        .build());

                String id = currentToken.getValue().toString();
                if (SymbolTable.lookup(id) != -1) {
                    ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                            .line(ll1.getLine())
                            .msg("Redeclaration of label: " + id)
                            .build());
                    return errorRecovery(); // Figure out a way to notify user
                }

                SymbolTable.insert(id, currentToken.getLine());
                visit(label);
                return label;
            }

            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Incorrect declaration")
                    .build());
            return errorRecovery();
        }

        if (ll1.getTokenType() != TokenType.EOF &&
                ll1.getTokenType() != TokenType.CPU_OPCODE &&
                ll1.getTokenType() != TokenType.FPU_OPCODE &&
                ll1.getTokenType() != TokenType.DOT &&
                lexer.getNextToken().getTokenType() != TokenType.TEXT) {
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Incorrect declaration")
                    .build());
            return errorRecovery();
        }

        if (ll1.getTokenType() != TokenType.EOF) {
            lexer.reset(resetPos);
        }
        return null;
    }

    private Node data() {
        Node data = Node.builder()
                .construct(Construct.DATA)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.DOT) {
            ll1 = lexer.getNextToken();
            Node curr = Node.builder()
                    .nodeType(TERMINAL)
                    .line(ll1.getLine())
                    .value(ll1.getValue())
                    .build();

            if (ll1.getTokenType() == TokenType.ASCII) {
                data.addChild(curr);
                ll1 = lexer.getNextToken();
                if (ll1.getTokenType() == TokenType.STRING) {
                    Node node = Node.builder()
                            .nodeType(TERMINAL)
                            .line(ll1.getLine())
                            .value(ll1.getValue())
                            .build();

                    data.addChild(node);
                    visit(data);
                    return data;
                }
                ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                        .line(ll1.getLine())
                        .msg("Incorrect usage of storage type .ascii")
                        .build());
                return errorRecovery();
            }

            if (ll1.getTokenType() == TokenType.ASCIIZ) {
                data.addChild(curr);
                ll1 = lexer.getNextToken();
                if (ll1.getTokenType() == TokenType.STRING) {
                    Node node = Node.builder()
                            .nodeType(TERMINAL)
                            .line(ll1.getLine())
                            .value(ll1.getValue())
                            .build();

                    data.addChild(node);
                    visit(data);
                    return data;
                }
                ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                        .line(ll1.getLine())
                        .msg("Incorrect usage of storage type .asciiz")
                        .build());
                return errorRecovery();
            }

            if (ll1.getTokenType() == TokenType.SPACE_STORAGE) {
                data.addChild(curr);
                Node expr = expr();
                if (expr == null) {
                    ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                            .line(ll1.getLine())
                            .msg("Incorrect usage of storage type .space")
                            .build());
                    return errorRecovery();
                }

                data.addChild(expr);
                visit(data);
                return data;
            }
        }

        lexer.reset(resetPos);
        Node dataMode = dataMode();
        if (dataMode != null) {
            data.addChild(dataMode);
            Node dataList = dataList();
            if (dataList != null) {
                data.addChild(dataList);
            }
        }

        visit(data);
        return data;
    }

    private Node dataMode() {
        Node dataMode = Node.builder()
                .construct(Construct.DATAMODE)
                .nodeType(NONTERMINAL)
                .build();
        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();

        if (ll1.getTokenType() == TokenType.DOT) {
            ll1 = lexer.getNextToken();
            switch (ll1.getTokenType()) {
                case BYTE_STORAGE:
                case WORD_STORAGE:
                case HALF_STORAGE:
                case FLOAT_STORAGE:
                case DOUBLE_STORAGE:
                    dataMode.addChild(Node.builder()
                            .nodeType(TERMINAL)
                            .line(ll1.getLine())
                            .value(ll1.getValue())
                            .build());
                    visit(dataMode);
                    return dataMode;
            }

            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("No valid storage type was provided")
                    .build());
            return errorRecovery();
        }
        lexer.reset(resetPos);
        return null;
    }

    private Node dataList() {
        Node dataList = Node.builder()
                .construct(Construct.DATALIST)
                .nodeType(NONTERMINAL)
                .build();

        Node dataExpr = dataExpr();
        if (dataExpr != null) {
            dataList.addChild(dataExpr);
            Node greedyDataList = greedyDataList();
            dataList.addChild(greedyDataList);

            visit(dataList);
            return dataList;
        }
        return null;
    }

    private Node greedyDataList() {
        Node greedyRoot = Node.builder()
                .construct(Construct.DATALISTS)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        int errorLine = ll1.getLine();

        if (ll1.getTokenType() == TokenType.COMMA) {
            Node dataList = dataList();
            if (dataList != null) {
                greedyRoot.addChild(dataList);
                visit(greedyRoot);
                return greedyRoot;
            }

            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(errorLine)
                    .msg("Incorrect data declaration")
                    .build());
            return errorRecovery();
        }
        lexer.reset(resetPos);
        return greedyRoot;
    }

    private Node dataExpr() {
        Node dataExpr = Node.builder()
                .construct(Construct.DATAEXPR)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.FLOATING_POINT) {
            Node node = Node.builder()
                    .nodeType(TERMINAL)
                    .line(ll1.getLine())
                    .value(ll1.getValue())
                    .build();

            dataExpr.addChild(node);
            visit(dataExpr);
            return dataExpr;
        }

        lexer.reset(resetPos);
        Node expr = expr();
        if (expr != null) {
            dataExpr.addChild(expr);
            visit(dataExpr);
            return dataExpr;
        }
        return null;
    }

    private Node expr() {
        Node expr = Node.builder()
                .construct(Construct.EXPR)
                .nodeType(NONTERMINAL)
                .build();

        Node term = term();
        if (term != null) {
            expr.addChild(term);
            Node greedyExpr = greedyExpr();
            expr.addChild(greedyExpr);

            visit(expr);
            return expr;
        }
        return null;
    }

    private Node term() {
        Node greedyRoot = Node.builder()
                .construct(Construct.TERM)
                .nodeType(NONTERMINAL)
                .build();

        Node factor = factor();
        if (factor != null) {
            greedyRoot.addChild(factor);
            Node greedyTerm = greedyTerm();
            greedyRoot.addChild(greedyTerm);

            visit(greedyRoot);
            return greedyRoot;
        }
        return null;
    }

    private Node greedyTerm() {
        Node terms = Node.builder()
                .construct(Construct.TERMS)
                .nodeType(NONTERMINAL)
                .build();

        Node binOp = binOp();
        if (binOp != null) {
            Node factor = factor();
            if (factor != null) {
                terms.addChild(binOp);
                terms.addChild(factor);
                Node greedyTerm = greedyTerm();
                terms.addChild(greedyTerm);

                visit(terms);
                return terms;
            }
            return null;
        }
        return terms;
    }

    private Node factor() {
        Node factor = Node.builder()
                .construct(Construct.FACTOR)
                .nodeType(NONTERMINAL)
                .build();

        Node constant = constant();
        if (constant == null) {
            return null;
        }

        factor.addChild(constant);
        visit(factor);
        return factor;
    }

    private Node constant() {
        Node constant = Node.builder()
                .construct(Construct.CONSTANT)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        Node node = Node.builder()
                .nodeType(TERMINAL)
                .line(ll1.getLine())
                .value(ll1.getValue())
                .build();

        constant.addChild(node);
        switch (ll1.getTokenType()) {
            case DECI:
                visit(constant);
                return constant;
            case HEX:
                node.setValue(Long.decode(node.getValue().toString()));
                visit(constant);
                return constant;
            case OCTAL:
                node.setValue(Long.parseLong(node.getValue().toString(), 8));
                visit(constant);
                return constant;
        }

        lexer.reset(resetPos);
        return negConstant();
    }

    private Node negConstant() {
        Node negConstant = Node.builder()
                .construct(Construct.NEGCONSTANT)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();

        if (ll1.getTokenType() != TokenType.MINUS) {
            lexer.reset(resetPos);
            return null;
        }

        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.DECI) {
            negConstant.addChild(Node.builder()
                    .nodeType(TERMINAL)
                    .line(ll1.getLine())
                    .value(-1 * Integer.parseInt(ll1.getValue().toString()))
                    .build());

            visit(negConstant);
            return negConstant;
        }

        if (ll1.getTokenType() == TokenType.FLOATING_POINT) {
            negConstant.addChild(Node.builder()
                    .nodeType(TERMINAL)
                    .line(ll1.getLine())
                    .value(-1 * Double.parseDouble(ll1.getValue().toString()))
                    .build());

            visit(negConstant);
            return negConstant;
        }

        lexer.reset(resetPos);
        return null;
    }


    private Node unOp() {
        Node unOp = Node.builder()
                .nodeType(NONTERMINAL)
                .construct(Construct.UNOP)
                .build();
        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        switch (ll1.getTokenType()) {
            case PLUS:
            case MINUS:
                unOp.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .line(ll1.getLine())
                        .value(ll1.getValue())
                        .build());

                visit(unOp);
                return unOp;
        }
        lexer.reset(resetPos);
        return null;
    }

    private Node greedyExpr() {
        Node greedyRoot = Node.builder()
                .construct(Construct.EXPRS)
                .nodeType(NONTERMINAL)
                .build();

        Node unOp = unOp();
        if (unOp != null) {
            Node term = term();
            if (term != null) {
                greedyRoot.addChild(unOp);
                greedyRoot.addChild(term);
                Node greedyExpr = greedyExpr();

                greedyRoot.addChild(greedyExpr);
                visit(greedyRoot);
                return greedyRoot;
            }
            return null;
        }
        return greedyRoot;
    }

    private Node binOp() {
        Node binOp = Node.builder()
                .construct(Construct.BINOP)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        switch (ll1.getTokenType()) {
            case TIMES:
            case DIV:
                binOp.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .line(ll1.getLine())
                        .value(ll1.getValue())
                        .build());

                visit(binOp);
                return binOp;
        }
        lexer.reset(resetPos);
        return null;
    }

    private Node textSeg() {
        Node textSeg = Node.builder()
                .construct(Construct.TEXTSEG)
                .nodeType(NONTERMINAL)
                .build();

        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.EOF) {
            return textSeg;
        }
        if (ll1.getTokenType() == TokenType.DOT && lexer.getNextToken().getTokenType() != TokenType.TEXT) {
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Missing .text segment")
                    .build());
            return errorRecovery();
        }

        textSeg.setLine(ll1.getLine());
        Node greedyTextDecl = greedyTextDecl();
        textSeg.addChild(greedyTextDecl);

        visit(textSeg);
        return textSeg;
    }

    private Node greedyTextDecl() {
        Node greedyRoot = Node.builder()
                .construct(Construct.TEXTDECLS)
                .nodeType(NONTERMINAL)
                .build();

        Node textDecl = textDecl();
        if (textDecl != null) {
            greedyRoot.addChild(textDecl);
            Node greedyTextDecl = greedyTextDecl();
            greedyRoot.addChild(greedyTextDecl);
        }

        visit(greedyRoot);
        return greedyRoot;
    }

    private Node textDecl() {
        Node textDecl = Node.builder()
                .construct(Construct.TEXTDECL)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        Node label = label();
        if (label != null) {
            int resetPos0 = lexer.getTokenPos();
            ll1 = lexer.getNextToken();

            if (ll1.getTokenType() == TokenType.CPU_OPCODE || ll1.getTokenType() == TokenType.FPU_OPCODE) {
                lexer.reset(resetPos0);
                textDecl.addChild(label);
                Node instruction = instruction();

                if (instruction != null && !semanticAnalyzer.analyze(instruction)) {
                    ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                            .line(ll1.getLine())
                            .msg("Invalid use of " + semanticAnalyzer.findNode(instruction, Construct.OPCODE).orElse(instruction).getValue())
                            .build());
                }

                textDecl.addChild(instruction);
                visit(textDecl);
                return textDecl;
            }

            if (ll1.getTokenType() != TokenType.EOF) {
                lexer.reset(resetPos);
            }
        }

        Node instruction = instruction();
        if (instruction != null) {
            if (!semanticAnalyzer.analyze(instruction)) {
                ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                        .line(ll1.getLine())
                        .msg("Invalid use of " + semanticAnalyzer.findNode(instruction, Construct.OPCODE).orElse(instruction).getValue())
                        .build());
            }

            textDecl.addChild(instruction);
            visit(textDecl);
            return textDecl;
        }

        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.DOT) {
            ll1 = lexer.getNextToken();
            if (ll1.getTokenType() == TokenType.GLOBL) { // recognize .globl but do nothing
                ll1 = lexer.getNextToken();
                textDecl.addChild(Node.builder()
                        .line(ll1.getLine())
                        .value(ll1.getValue())
                        .build());

                visit(textDecl);
                return textDecl;
            } else {
                lexer.reset(resetPos);
            }
        } else {
            lexer.reset(resetPos);
        }
        return null;
    }

    private Node instruction() {
        Node instruction = Node.builder()
                .construct(Construct.INSTRUCTION)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.CPU_OPCODE && ll1.getTokenType() != TokenType.FPU_OPCODE) {
            if (ll1.getTokenType() != TokenType.EOF && ll1.getTokenType() != TokenType.DOT) {
                ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                        .line(ll1.getLine())
                        .msg("Incorrect instruction declaration")
                        .build());
                return errorRecovery();
            }
            if (ll1.getTokenType() != TokenType.EOF) {
                lexer.reset(resetPos);
            }
            return null;
        }

        instruction.setLine(ll1.getLine());
        lexer.reset(resetPos);
        Node fourOp = fourOp();
        if (fourOp != null) {
            instruction.addChild(fourOp);
            visit(instruction);
            return instruction;
        }


        Node threeOp = threeOp();
        if (threeOp != null) {
            instruction.addChild(threeOp);
            visit(instruction);
            return instruction;
        }

        Node twoOp = twoOp();
        if (twoOp != null) {
            instruction.addChild(twoOp);
            visit(instruction);
            return instruction;
        }

        Node oneOp = oneOp();
        if (oneOp != null) {
            instruction.addChild(oneOp);
            visit(instruction);
            return instruction;
        }

        Node zeroOp = zeroOp();
        if (zeroOp != null) {
            instruction.addChild(zeroOp);
            visit(instruction);
            return instruction;
        }

        if (ll1.getTokenType() != TokenType.EOF) {
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Incorrect instruction declaration")
                    .build());
        }
        return errorRecovery();
    }

    private Node fourOp() {
        Node fourOp = Node.builder()
                .construct(Construct.FOUROP)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken(); // Opcode
        fourOp.addChild(Node.builder()
                .construct(Construct.OPCODE)
                .nodeType(TERMINAL)
                .line(ll1.getLine())
                .value(ll1.getValue())
                .build());

        Node operand = register();
        if (operand == null) {
            lexer.reset(resetPos);
            return null;
        }

        fourOp.addChild(operand);
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.COMMA) {
            lexer.reset(resetPos);
            return null;
        }

        operand = register();
        if (operand == null) {
            lexer.reset(resetPos);
            return null;
        }

        fourOp.addChild(operand);
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.COMMA) {
            lexer.reset(resetPos);
            return null;
        }

        operand = constant();
        if (operand == null) {
            lexer.reset(resetPos);
            return null;
        }

        fourOp.addChild(operand);
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.COMMA) {
            lexer.reset(resetPos);
            return null;
        }

        operand = constant();
        if (operand != null) {
            fourOp.addChild(operand);
            visit(fourOp);
            return fourOp;
        }

        lexer.reset(resetPos);
        return null;
    }

    private Node threeOp() {
        Node threeOp = Node.builder()
                .construct(Construct.THREEOP)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken(); // Opcode
        threeOp.addChild(Node.builder()
                .construct(Construct.OPCODE)
                .nodeType(TERMINAL)
                .line(ll1.getLine())
                .value(ll1.getValue())
                .build());

        Node operand = register();
        if (operand == null) {
            lexer.reset(resetPos);
            return null;
        }

        threeOp.addChild(operand);
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.COMMA) {
            lexer.reset(resetPos);
            return null;
        }

        operand = register();
        if (operand == null) {
            lexer.reset(resetPos);
            return null;
        }

        threeOp.addChild(operand);
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.COMMA) {
            lexer.reset(resetPos);
            return null;
        }

        operand = operand();
        if (operand != null) {
            threeOp.addChild(operand);
            visit(threeOp);
            return threeOp;
        }

        lexer.reset(resetPos);
        return null;
    }

    private Node twoOp() {
        Node twoOp = Node.builder()
                .construct(Construct.TWOOP)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken(); // Opcode
        twoOp.addChild(Node.builder()
                .construct(Construct.OPCODE)
                .nodeType(TERMINAL)
                .line(ll1.getLine())
                .value(ll1.getValue())
                .build());

        Node operand = register();
        if (operand == null) {
            lexer.reset(resetPos);
            return null;
        }

        twoOp.addChild(operand);
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() != TokenType.COMMA) {
            lexer.reset(resetPos);
            return null;
        }

        operand = operand();
        if (operand != null) {
            twoOp.addChild(operand);
            visit(twoOp);
            return twoOp;
        }

        lexer.reset(resetPos);
        return null;
    }

    private Node oneOp() {
        Node oneOp = Node.builder()
                .construct(Construct.ONEOP)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken(); // Opcode
        oneOp.addChild(Node.builder()
                .construct(Construct.OPCODE)
                .nodeType(TERMINAL)
                .line(ll1.getLine())
                .value(ll1.getValue())
                .build());

        Node operand = operand();
        if (operand != null) {
            oneOp.addChild(operand);
            visit(oneOp);
            return oneOp;
        }

        lexer.reset(resetPos);
        return null;
    }

    private Node zeroOp() {
        ll1 = lexer.getNextToken();
        Node zeroOp = Node.builder()
                .construct(Construct.ZEROOP)
                .nodeType(NONTERMINAL)
                .build();

        zeroOp.addChild(Node.builder()
                .construct(Construct.OPCODE)
                .nodeType(TERMINAL)
                .line(ll1.getLine())
                .value(ll1.getValue())
                .build());

        if (ll1.getTokenType() != TokenType.CPU_OPCODE && ll1.getTokenType() != TokenType.FPU_OPCODE) {
            return null;
        }

        visit(zeroOp);
        return zeroOp;
    }

    private Node operand() {
        Node operand = Node.builder()
                .construct(Construct.OPERAND)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        Node expr = expr();
        if (expr != null) {
            Node parenRegister = parenRegister();
            if (parenRegister != null) {
                operand.addChild(expr);
                operand.addChild(parenRegister);

                visit(operand);
                return operand;
            }
            lexer.reset(resetPos); // backoff because matched a constant
        }

        Node constant = constant();
        if (constant != null) {
            operand.addChild(constant);
            visit(operand);
            return operand;
        }

        Node register = register();
        if (register != null) {
            operand.addChild(register);
            visit(operand);
            return operand;
        }

        Node parenRegister = parenRegister();
        if (parenRegister != null) {
            operand.addChild(parenRegister);
            visit(operand);
            return operand;
        }

        Token nextToken = lexer.getNextToken();
        if (nextToken.getTokenType() == TokenType.ID) {
            int reset = lexer.getTokenPos();
            Token lookAhead = lexer.getNextToken();
            if (lookAhead.getTokenType() != TokenType.COLON) { // recognize labels used as operand
                lexer.reset(reset);
                operand.addChild(Node.builder()
                        .construct(Construct.LABEL)
                        .nodeType(TERMINAL)
                        .line(nextToken.getLine())
                        .value(nextToken.getValue())
                        .build());

                visit(operand);
                return operand;
            }
        }
        lexer.reset(resetPos);
        return null;
    }

    private Node register() {
        Node register = Node.builder()
                .construct(Construct.REGISTER)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.DOLLAR_SIGN) {
            ll1 = lexer.getNextToken();
            if (ll1.getTokenType() == TokenType.REG) {
                register.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .line(ll1.getLine())
                        .value(ll1.getValue())
                        .build());

                visit(register);
                return register;
            }

            if (ll1.getTokenType() == TokenType.DECI) {
                register.addChild(Node.builder()
                        .nodeType(TERMINAL)
                        .line(ll1.getLine())
                        .value(MipsLexer.registerNumberToName(ll1.getValue().toString()))
                        .build());

                visit(register);
                return register;
            }
            lexer.reset(resetPos);
            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Incorrect register declaration")
                    .build());
        }
        lexer.reset(resetPos);
        return null;
    }

    private Node parenRegister() {
        Node parenRegister = Node.builder()
                .construct(Construct.PARENREG)
                .nodeType(NONTERMINAL)
                .build();

        int resetPos = lexer.getTokenPos();
        ll1 = lexer.getNextToken();
        if (ll1.getTokenType() == TokenType.L_PAREN) {
            Node register = register();
            if (register != null) {
                ll1 = lexer.getNextToken();
                if (ll1.getTokenType() == TokenType.R_PAREN) {
                    parenRegister.addChild(register);
                    visit(parenRegister);
                    return parenRegister;
                }
                lexer.reset(resetPos);
            }

            ErrorRecorder.recordError(ErrorRecorder.Error.builder()
                    .line(ll1.getLine())
                    .msg("Incorrect register declaration")
                    .build());
        }

        lexer.reset(resetPos);
        return null;
    }

    private Node errorRecovery() {
        int resetPos;
        TokenType currTokenType, nextTokenType;
        while (lexer.hasNextToken()) {
            resetPos = lexer.getTokenPos();
            currTokenType = ll1.getTokenType();
            ll1 = lexer.getNextToken();
            nextTokenType = ll1.getTokenType();

            if (currTokenType == TokenType.CPU_OPCODE || currTokenType == TokenType.FPU_OPCODE) {
                lexer.reset(resetPos - 1);
                return greedyTextDecl();
            }

            if ((currTokenType == TokenType.ID || currTokenType == TokenType.DECI) && nextTokenType == TokenType.COLON) {
                ll1 = lexer.getNextToken();
                nextTokenType = ll1.getTokenType();
                if (nextTokenType == TokenType.CPU_OPCODE || nextTokenType == TokenType.FPU_OPCODE) {
                    lexer.reset(lexer.getTokenPos() - 1);
                    return greedyTextDecl();
                } else {
                    lexer.reset(resetPos - 1);
                    return dataDecl();
                }
            }
        }
        return null;
    }
}
