package com.iwhalecloud.bote.service.skill.security;

/**
 * 为启发式安全规则剥离 JavaScript/TypeScript 风格源码中的注释，保留字符串字面量内容（含模板字符串），
 * 行为对齐技能扫描前对源码的轻量预处理需求。
 */
public final class HeuristicStripper {

  private static final char NO_QUOTE = '\0';

  private final String source;

  private final int len;

  private final StringBuilder stripped;

  /** 当前字符串定界符；{@value #NO_QUOTE} 表示不在字符串内。 */
  private char quote = NO_QUOTE;

  private boolean escaped;

  private boolean inBlockComment;

  private HeuristicStripper(String source) {
    this.source = source;
    this.len = source.length();
    this.stripped = new StringBuilder(len);
  }

  /**
   * 返回剥离行注释与块注释后的源码，便于基于「去注释」视图的规则匹配。
   *
   * @param source 原始源码（非 {@code null}）
   * @return 剥离注释后的文本；空串入参原样返回
   */
  public static String strip(String source) {
    if (source.isEmpty()) {
      return source;
    }
    return new HeuristicStripper(source).stripInternal();
  }

  private String stripInternal() {
    int i = 0;
    while (i < len) {
      char ch = source.charAt(i);
      char next = peek(i + 1);
      if (inBlockComment) {
        i = consumeBlockComment(i, ch, next) + 1;
        continue;
      }
      if (quote != NO_QUOTE) {
        consumeQuotedChar(ch);
        i++;
        continue;
      }
      if (isStringDelimiter(ch)) {
        enterQuote(ch);
        i++;
        continue;
      }
      if (ch == '/' && next == '/') {
        i = skipLineComment(i) + 1;
        continue;
      }
      if (ch == '/' && next == '*') {
        inBlockComment = true;
        i += 2;
        continue;
      }
      stripped.append(ch);
      i++;
    }
    return stripped.toString();
  }

  private char peek(int index) {
    return index < len ? source.charAt(index) : NO_QUOTE;
  }

  private int consumeBlockComment(int i, char ch, char next) {
    if (ch == '*' && next == '/') {
      inBlockComment = false;
      return i + 1;
    }
    if (ch == '\n') {
      stripped.append('\n');
    }
    return i;
  }

  private void consumeQuotedChar(char ch) {
    stripped.append(ch);
    if (escaped) {
      escaped = false;
      return;
    }
    if (ch == '\\') {
      escaped = true;
      return;
    }
    if (ch == quote) {
      quote = NO_QUOTE;
    }
  }

  private boolean isStringDelimiter(char ch) {
    return ch == '\'' || ch == '"' || ch == '`';
  }

  private void enterQuote(char ch) {
    quote = ch;
    stripped.append(ch);
  }

  private int skipLineComment(int i) {
    while (i < len && source.charAt(i) != '\n') {
      i++;
    }
    if (i < len && source.charAt(i) == '\n') {
      stripped.append('\n');
    }
    return i;
  }
}
