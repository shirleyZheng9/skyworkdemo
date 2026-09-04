package com.iwhalecloud.bote.agent.skill;

import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.yaml.snakeyaml.Yaml;

/**
 * Agent Skill 的 Markdown 解析器
 *
 * @author bianjp
 * @since 2026-02-03
 */
@SuppressWarnings("PMD.GuardLogStatement")
public final class AgentSkillMarkdownParser {
  private static final Logger logger = LoggerFactory.getLogger(AgentSkillMarkdownParser.class);
  private static final Pattern FRONT_MATTER_ITEM_PATTERN = Pattern.compile("^([\\w-]+):\\s*(.*)$");
  private static final Pattern YAML_BLOCK_SCALAR_PATTERN = Pattern.compile("^[|>][+-]?\\d*[+-]?$");

  private AgentSkillMarkdownParser() {
  }

  /**
   * 解析 SKILL.md 文件内容，并返回解析结果
   *
   * @param markdown 原始 SKILL.md 文本
   * @return SkillMdParseResult 解析结果
   */
  public static SkillMdParseResult parseSkill(@Nullable String markdown) {
    String normalizedMarkdown = normalizedMarkdown(markdown);
    // 一次规范化后同时得到元数据与正文
    Map<String, String> metadata = doParseFrontMatter(normalizedMarkdown);
    String bodyContent = extractContent(normalizedMarkdown);
    return new SkillMdParseResult(metadata, bodyContent, normalizedMarkdown);
  }

  /**
   * 解析 Agent Skill
   *
   * @param baseDir 技能的基础目录
   * @param file markdown 文件
   * @return Agent Skill
   */
  public static AgentSkillSpec parseSkill(String baseDir, File file) {
    Pair<Map<String, String>, String> result = parse(file);
    return new AgentSkillSpec(baseDir, result.getLeft(), result.getRight());
  }

  /**
   * 解析 Agent Skill
   *
   * @param baseDir 技能的基础目录
   * @param markdown markdown 文本
   * @return Agent Skill
   */
  public static AgentSkillSpec parseSkill(String baseDir, String markdown) {
    Pair<Map<String, String>, String> result = parse(markdown);
    return new AgentSkillSpec(baseDir, result.getLeft(), result.getRight());
  }

  /**
   * 解析 SKILL.md 文件
   *
   * @param file SKILL.md 文件
   * @return (元数据, 内容)
   */
  public static Pair<Map<String, String>, String> parse(File file) {
    String markdown;
    try {
      markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      logger.warn("Failed to read SKILL.md: file={}", file.getAbsolutePath(), e);
      throw new BssException("读取 SKILL.md 失败: " + file.getAbsolutePath(), e);
    }
    return parse(markdown);
  }

  /**
   * 解析 Markdown
   *
   * @param markdown markdown 文本
   * @return (元数据, 内容)
   */
  public static Pair<Map<String, String>, String> parse(@Nullable String markdown) {
    String normalizedMarkdown = normalizedMarkdown(markdown);
    if (normalizedMarkdown.isEmpty()) {
      return Pair.of(Map.of(), "");
    }
    Map<String, String> frontMatter = doParseFrontMatter(normalizedMarkdown);
    // 使用完整文档内容作为内容，因为有些 skill 的元数据也需要提供给大模型使用
    return Pair.of(frontMatter, normalizedMarkdown);
  }

  /**
   * 解析元数据
   *
   * @param markdown markdown 文本
   * @return 元数据
   */
  public static Map<String, String> parseFrontMatter(@Nullable String markdown) {
    String normalizedMarkdown = normalizedMarkdown(markdown);
    if (normalizedMarkdown.isEmpty()) {
      return Map.of();
    }
    return doParseFrontMatter(normalizedMarkdown);
  }

  /**
   * 解析正文
   *
   * @param markdown markdown 文本
   * @return 正文
   */
  public static String parseContent(@Nullable String markdown) {
    String normalizedMarkdown = normalizedMarkdown(markdown);
    if (normalizedMarkdown.isEmpty()) {
      return "";
    }
    return extractContent(normalizedMarkdown);
  }

  /**
   * 规范 Markdown 中的换行符
   */
  private static String normalizedMarkdown(@Nullable String markdown) {
    if (StringUtils.isBlank(markdown)) {
      return "";
    }
    return markdown.trim().replace("\r\n", "\n").replace("\r", "\n");
  }

  /**
   * 解析元数据
   */
  private static Map<String, String> doParseFrontMatter(String normalizedMarkdown) {
    String block = extractFrontMatterBlock(normalizedMarkdown);
    if (StringUtils.isEmpty(block)) {
      return Map.of();
    }

    // 参考 https://github.com/openclaw/openclaw/blob/main/src/markdown/frontmatter.ts

    // 按行解析
    Map<String, LineEntry> lineParsed = parseLineFrontMatter(block);
    // 用 yaml 解析
    Map<String, YamlValue> yamlParsed = parseYamlFrontMatter(block);

    // 合并解析结果
    Map<String, String> frontMatter;
    if (yamlParsed == null) {
      // YAML 解析失败，使用行解析结果
      frontMatter = new LinkedHashMap<>();
      for (Map.Entry<String, LineEntry> entry : lineParsed.entrySet()) {
        frontMatter.put(entry.getKey(), entry.getValue().value);
      }
    }
    else {
      // 合并 YAML 解析结果和行解析结果
      frontMatter = mergeFrontMatter(lineParsed, yamlParsed);
    }
    return frontMatter;
  }

  /**
   * 提取 frontMatter 块
   */
  @Nullable
  private static String extractFrontMatterBlock(String markdown) {
    if (!markdown.startsWith("---\n")) {
      return null;
    }
    int endIndex = markdown.indexOf("\n---", 3);
    if (endIndex == -1) {
      return null;
    }
    // Handle empty frontmatter (---\n---)
    if (endIndex < 4) {
      return "";
    }
    return markdown.substring(4, endIndex);
  }

  /**
   * 提取正文内容
   */
  private static String extractContent(String markdown) {
    if (!markdown.startsWith("---\n")) {
      return markdown;
    }
    int blockStart = markdown.indexOf("\n---", 3);
    if (blockStart == -1) {
      return markdown;
    }
    return markdown.substring(blockStart + 4).trim();
  }

  /**
   * 解析 YAML frontmatter
   */
  @SuppressWarnings("unchecked")
  @Nullable
  private static Map<String, YamlValue> parseYamlFrontMatter(String block) {
    try {
      Yaml yaml = new Yaml();
      Object parsed = yaml.load(block);
      if (!(parsed instanceof Map)) {
        return null;
      }
      Map<String, Object> map = (Map<String, Object>) parsed;
      Map<String, YamlValue> result = new LinkedHashMap<>();
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey().trim();
        if (key.isEmpty()) {
          continue;
        }
        YamlValue coerced = coerceYamlValue(entry.getValue());
        if (coerced != null) {
          result.put(key, coerced);
        }
      }
      return result;
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * 将 YAML 值转换为标准格式
   */
  @Nullable
  private static YamlValue coerceYamlValue(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return new YamlValue(((String) value).trim(), true);
    }
    if (value instanceof Number || value instanceof Boolean) {
      return new YamlValue(String.valueOf(value), true);
    }
    if (value instanceof Map || value instanceof List) {
      try {
        return new YamlValue(JsonUtil.toJsonString(value), false);
      }
      catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  /**
   * 按行手工解析 frontMatter
   */
  private static Map<String, LineEntry> parseLineFrontMatter(String block) {
    Map<String, LineEntry> result = new LinkedHashMap<>();
    String[] lines = block.split("\n");
    int i = 0;

    while (i < lines.length) {
      String line = lines[i];
      Matcher matcher = FRONT_MATTER_ITEM_PATTERN.matcher(line);
      if (!matcher.find()) {
        i++;
        continue;
      }

      String key = matcher.group(1);
      String inlineValue = matcher.group(2).trim();
      if (key.isEmpty()) {
        i++;
        continue;
      }

      // 检查是否是多行值
      if (inlineValue.isEmpty() && i + 1 < lines.length) {
        String nextLine = lines[i + 1];
        if (nextLine.startsWith(" ") || nextLine.startsWith("\t")) {
          MultiLineResult mlr = extractMultiLineValue(lines, i);
          if (!mlr.value.isEmpty()) {
            result.put(key, new LineEntry(mlr.value, true, inlineValue));
          }
          i += mlr.linesConsumed;
          continue;
        }
      }

      String value = stripQuotes(inlineValue);
      if (!value.isEmpty()) {
        result.put(key, new LineEntry(value, false, inlineValue));
      }
      i++;
    }

    return result;
  }

  /**
   * 提取多行值
   */
  private static MultiLineResult extractMultiLineValue(String[] lines, int startIndex) {
    StringBuilder valueLines = new StringBuilder();
    int i = startIndex + 1;

    while (i < lines.length) {
      String line = lines[i];
      if (!line.isEmpty() && !line.startsWith(" ") && !line.startsWith("\t")) {
        break;
      }
      if (!valueLines.isEmpty()) {
        valueLines.append("\n");
      }
      valueLines.append(line);
      i++;
    }

    return new MultiLineResult(valueLines.toString().trim(), i - startIndex);
  }

  /**
   * 合并行解析和 YAML 解析结果
   */
  private static Map<String, String> mergeFrontMatter(Map<String, LineEntry> lineParsed, Map<String, YamlValue> yamlParsed) {
    Map<String, String> merged = new LinkedHashMap<>();

    // 首先添加 YAML 解析的结果
    for (Map.Entry<String, YamlValue> entry : yamlParsed.entrySet()) {
      String key = entry.getKey();
      YamlValue yamlValue = entry.getValue();
      merged.put(key, yamlValue.value);

      LineEntry lineEntry = lineParsed.get(key);
      if (lineEntry == null) {
        continue;
      }

      // 对于结构化值且行解析为内联且包含冒号的情况，优先使用行解析值
      if (shouldPreferInlineLineValue(lineEntry, yamlValue)) {
        merged.put(key, lineEntry.value);
      }
    }

    // 添加只在行解析中存在的键
    for (Map.Entry<String, LineEntry> entry : lineParsed.entrySet()) {
      String key = entry.getKey();
      if (!merged.containsKey(key)) {
        merged.put(key, entry.getValue().value);
      }
    }

    return merged;
  }

  /**
   * 判断是否应优先使用行解析的内联值
   */
  private static boolean shouldPreferInlineLineValue(LineEntry lineEntry, YamlValue yamlValue) {
    if (yamlValue.scalar) {
      return false;
    }
    if (lineEntry.multiline) {
      return false;
    }
    if (YAML_BLOCK_SCALAR_PATTERN.matcher(lineEntry.rawInline).matches()) {
      return false;
    }
    return lineEntry.value.contains(":");
  }

  /**
   * 去除引号
   */
  private static String stripQuotes(String value) {
    if (value.length() >= 2) {
      if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
        return value.substring(1, value.length() - 1);
      }
    }
    return value;
  }

  /**
   * 行解析条目
   */
  private record LineEntry(String value, boolean multiline, String rawInline) {
  }

  /**
   * YAML 解析值
   */
  private record YamlValue(String value, boolean scalar) {
  }

  /**
   * 多行值提取结果
   */
  private record MultiLineResult(String value, int linesConsumed) {
  }

  /**
   * SKILL.md 解析结果
   */
  public record SkillMdParseResult(Map<String, String> metadata, String bodyContent, String normalizedMarkdown) {
  }
}
