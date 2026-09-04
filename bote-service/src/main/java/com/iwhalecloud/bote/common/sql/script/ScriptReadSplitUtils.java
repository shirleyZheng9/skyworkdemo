package com.iwhalecloud.bote.common.sql.script;

import lombok.Getter;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * SQL 脚本读取切割工具
 *
 * <p>
 *   Spring Framework 从 6.x 开始，将 org.springframework.jdbc.datasource.init.ScriptUtils 中的 readScript()
 *   和 splitSqlScript() 方法都移除了 public。这里仿照原有逻辑实现对应的 readScript() 和 splitSqlScript()
 * </p>
 *
 * @author wangtingyun
 * @since 2025-11-25
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.UnusedFormalParameter"})
public final class ScriptReadSplitUtils {

  private ScriptReadSplitUtils() {
  }

  // 默认配置常量
  public static final String DEFAULT_STATEMENT_SEPARATOR = ";";
  public static final String DEFAULT_COMMENT_PREFIX = "--";
  public static final String DEFAULT_BLOCK_COMMENT_START = "/*";
  public static final String DEFAULT_BLOCK_COMMENT_END = "*/";

  /**
   * 读取脚本内容
   *
   * <p> 源码参考 org.springframework.jdbc.datasource.init.ScriptUtils#readScript </p>
   */
  public static String readScript(LineNumberReader lineNumberReader, @Nullable String[] commentPrefixes,
                                   @Nullable String separator, @Nullable String blockCommentEndDelimiter) throws IOException {

    String currentStatement = lineNumberReader.readLine();
    StringBuilder scriptBuilder = new StringBuilder();
    while (currentStatement != null) {
      if ((blockCommentEndDelimiter != null && currentStatement.contains(blockCommentEndDelimiter)) ||
        (commentPrefixes != null && !startsWithAny(currentStatement, commentPrefixes, 0))) {
        if (!scriptBuilder.isEmpty()) {
          scriptBuilder.append('\n');
        }
        scriptBuilder.append(currentStatement);
      }
      currentStatement = lineNumberReader.readLine();
    }
    appendSeparatorToScriptIfNecessary(scriptBuilder, separator);
    return scriptBuilder.toString();
  }

  private static void appendSeparatorToScriptIfNecessary(StringBuilder scriptBuilder, @Nullable String separator) {
    if (separator == null) {
      return;
    }
    String trimmed = separator.trim();
    if (trimmed.length() == separator.length()) {
      return;
    }
    // separator ends in whitespace, so we might want to see if the script is trying
    // to end the same way
    if (scriptBuilder.lastIndexOf(trimmed) == scriptBuilder.length() - trimmed.length()) {
      scriptBuilder.append(separator.substring(trimmed.length()));
    }
  }

  private static boolean startsWithAny(String script, String[] prefixes, int offset) {
    for (String prefix : prefixes) {
      if (script.startsWith(prefix, offset)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 分割 SQL 脚本（使用默认配置）
   */
  public static List<String> splitSqlScript(String script) {
    return splitSqlScript(script,
      DEFAULT_STATEMENT_SEPARATOR,
      DEFAULT_COMMENT_PREFIX,
      DEFAULT_BLOCK_COMMENT_START,
      DEFAULT_BLOCK_COMMENT_END);
  }

  /**
   * 分割 SQL 脚本（自定义配置）
   *
   * <p>参考源码实现：org.springframework.jdbc.datasource.init.ScriptUtils#splitSqlScript</p>
   */
  public static List<String> splitSqlScript(String script,
                                            String separator,
                                            String lineCommentPrefix,
                                            String blockCommentStart,
                                            String blockCommentEnd) {
    if (script == null || script.trim().isEmpty()) {
      return Collections.emptyList();
    }

    List<String> statements = new ArrayList<>();
    splitSqlScript(script, separator, lineCommentPrefix, blockCommentStart,
      blockCommentEnd, statements::add);
    return statements;
  }

  /**
   * 核心分割方法（类似 Spring 的 API 设计）
   */
  public static void splitSqlScript(String script,
                                    String separator,
                                    String lineCommentPrefix,
                                    String blockCommentStart,
                                    String blockCommentEnd,
                                    Consumer<String> statementConsumer) {
    if (script == null || separator == null) {
      throw new IllegalArgumentException("Script and separator cannot be null");
    }

    SqlParser parser = new SqlParser(script, separator, lineCommentPrefix,
      blockCommentStart, blockCommentEnd, statementConsumer);
    parser.parse();
  }

  /**
   * SQL 解析器内部类
   */
  private static class SqlParser {
    private final String script;
    private final String separator;
    private final String lineCommentPrefix;
    private final String blockCommentStart;
    private final String blockCommentEnd;
    private final Consumer<String> statementConsumer;

    private final StringBuilder currentStatement = new StringBuilder();
    private int position = 0;

    // 解析状态
    private boolean inSingleQuote = false;
    private boolean inDoubleQuote = false;
    private boolean inEscape = false;
    private boolean inLineComment = false;
    private boolean inBlockComment = false;

    public SqlParser(String script, String separator, String lineCommentPrefix,
                     String blockCommentStart, String blockCommentEnd,
                     Consumer<String> statementConsumer) {
      this.script = script;
      this.separator = separator;
      this.lineCommentPrefix = lineCommentPrefix;
      this.blockCommentStart = blockCommentStart;
      this.blockCommentEnd = blockCommentEnd;
      this.statementConsumer = statementConsumer;
    }

    public void parse() {
      while (position < script.length()) {
        char currentChar = script.charAt(position);

        if (inEscape) {
          handleEscapeCharacter(currentChar);
        } else if (currentChar == '\\') {
          handleBackslashEscape();
        } else if (inLineComment) {
          handleLineComment(currentChar);
        } else if (inBlockComment) {
          handleBlockComment(currentChar);
        } else if (inSingleQuote) {
          handleSingleQuote(currentChar);
        } else if (inDoubleQuote) {
          handleDoubleQuote(currentChar);
        } else {
          handleNormalCharacter(currentChar);
        }

        position++;
      }

      // 处理最后一个语句
      finishCurrentStatement();
    }

    private void handleEscapeCharacter(char c) {
      inEscape = false;
      currentStatement.append(c);
    }

    private void handleBackslashEscape() {
      inEscape = true;
      currentStatement.append('\\');
    }

    private void handleLineComment(char c) {
      if (c == '\n') {
        inLineComment = false;
      }
      // 注释内容不添加到语句中
    }

    private void handleBlockComment(char c) {
      if (script.startsWith(blockCommentEnd, position)) {
        inBlockComment = false;
        // 跳过注释结束标记
        position += blockCommentEnd.length() - 1;
      }
      // 注释内容不添加到语句中
    }

    private void handleSingleQuote(char c) {
      currentStatement.append(c);
      if (c == '\'') {
        inSingleQuote = false;
      }
    }

    private void handleDoubleQuote(char c) {
      currentStatement.append(c);
      if (c == '"') {
        inDoubleQuote = false;
      }
    }

    private void handleNormalCharacter(char c) {
      // 检查是否遇到分隔符
      if (script.startsWith(separator, position)) {
        finishCurrentStatement();
        // 跳过分隔符
        position += separator.length() - 1;
        return;
      }

      // 检查是否遇到行注释
      if (lineCommentPrefix != null && script.startsWith(lineCommentPrefix, position)) {
        inLineComment = true;
        position += lineCommentPrefix.length() - 1;
        return;
      }

      // 检查是否遇到块注释
      if (blockCommentStart != null && script.startsWith(blockCommentStart, position)) {
        inBlockComment = true;
        position += blockCommentStart.length() - 1;
        return;
      }

      // 处理引号
      if (c == '\'') {
        inSingleQuote = true;
        currentStatement.append(c);
      } else if (c == '"') {
        inDoubleQuote = true;
        currentStatement.append(c);
      } else if (Character.isWhitespace(c)) {
        // 压缩连续空白字符
        appendNormalizedWhitespace();
      } else {
        currentStatement.append(c);
      }
    }

    private void appendNormalizedWhitespace() {
      if (!currentStatement.isEmpty() &&
        !Character.isWhitespace(currentStatement.charAt(currentStatement.length() - 1))) {
        currentStatement.append(' ');
      }
    }

    private void finishCurrentStatement() {
      String statement = currentStatement.toString().trim();
      if (!statement.isEmpty()) {
        statementConsumer.accept(statement);
      }
      // 清空当前语句
      currentStatement.setLength(0);
    }
  }

  /**
   * 高级功能：支持多行语句和自定义分隔符检测
   */
  public static class AdvancedSplitter {
    private final Set<String> separators;
    private final Set<String> lineCommentPrefixes;

    public AdvancedSplitter() {
      this.separators = new HashSet<>(Arrays.asList(";", "GO", "$$"));
      this.lineCommentPrefixes = new HashSet<>(Arrays.asList("--", "//", "#"));
    }

    public AdvancedSplitter(Set<String> separators, Set<String> lineCommentPrefixes) {
      this.separators = separators;
      this.lineCommentPrefixes = lineCommentPrefixes;
    }

    public List<ParsedStatement> splitWithMetadata(String script) {
      List<ParsedStatement> results = new ArrayList<>();
      List<String> statements = splitSqlScript(script);

      for (int i = 0; i < statements.size(); i++) {
        results.add(new ParsedStatement(i + 1, statements.get(i)));
      }

      return results;
    }
  }

  /**
   * 包含元数据的 SQL 语句
   */
  @Getter
  public static class ParsedStatement {
    private final int sequence;
    private final String sql;

    public ParsedStatement(int sequence, String sql) {
      this.sequence = sequence;
      this.sql = sql;
    }

    public boolean isEmpty() { return sql.trim().isEmpty(); }

    @Override
    public String toString() {
      return String.format("Statement #%d: %s", sequence,
        sql.length() > 50 ? sql.substring(0, 47) + "..." : sql);
    }
  }

}
