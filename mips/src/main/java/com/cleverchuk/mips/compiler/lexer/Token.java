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

package com.cleverchuk.mips.compiler.lexer;

public class Token {
  private final TokenType tokenType;

  private final Object value;

  private final int line;

  private final int pos;

  private final String code;

  public Token(TokenType tokenType, Object value, int line) {
    this.tokenType = tokenType;
    this.value = value;
    this.line = line;
    this.pos = -1;
    this.code = null;
  }

  public Token(TokenType tokenType, Object value, int line, int pos, String code) {
    this.tokenType = tokenType;
    this.value = value;
    this.line = line;
    this.pos = pos;
    this.code = code;
  }

  public TokenType getTokenType() {
    return tokenType;
  }

  public Object getValue() {
    return value;
  }

  public int getLine() {
    return line;
  }

  public int getPos() {
    return pos;
  }

  public String getCode() {
    return code;
  }

  public static TokenBuilder builder() {
    return new TokenBuilder();
  }

  public static class TokenBuilder {
    private TokenType tokenType;

    private Object value;

    private int line;

    private int pos;

    private String code;

    public TokenBuilder tokenType(TokenType tokenType) {
      this.tokenType = tokenType;
      return this;
    }

    public TokenBuilder value(Object value) {
      this.value = value;
      return this;
    }

    public TokenBuilder line(int line) {
      this.line = line;
      return this;
    }

    public TokenBuilder pos(int pos) {
      this.pos = pos;
      return this;
    }

    public Token build() {
      return new Token(tokenType, value, line, pos, code);
    }
  }
}
