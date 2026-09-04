package com.iwhalecloud.bote.agent.skill;

import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Agent Skill 的 Markdown 解析器单元测试
 *
 * @author bianjp
 * @since 2026-03-27
 */
class AgentSkillMarkdownParserTest {

  @Test
  void parseEmptyMarkdown() {
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse("");
    assertTrue(result.getLeft().isEmpty());
    assertEquals("", result.getRight());
  }

  @Test
  void parseBlankMarkdown() {
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse("   \n\t  ");
    assertTrue(result.getLeft().isEmpty());
    assertEquals("", result.getRight());
  }

  @Test
  void parseNoFrontMatter() {
    String markdown = "# Hello World\n\nThis is content.";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertTrue(result.getLeft().isEmpty());
    assertEquals(markdown, result.getRight());
  }

  @Test
  void parseSimpleFrontMatter() {
    String markdown = """
      ---
      name: test-skill
      description: A test skill
      ---

      # Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("test-skill", result.getLeft().get("name"));
    assertEquals("A test skill", result.getLeft().get("description"));
    // parse 返回完整 markdown（含 front matter），正文紧随其后
    assertTrue(result.getRight().endsWith("# Content"));
  }

  @Test
  void parseFrontMatterWithQuotes() {
    String markdown = """
      ---
      name: "quoted-skill"
      description: 'single quoted'
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("quoted-skill", result.getLeft().get("name"));
    assertEquals("single quoted", result.getLeft().get("description"));
  }

  @Test
  void parseMultilineValue() {
    String markdown = """
      ---
      name: test
      description: |
        First line
        Second line
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("test", result.getLeft().get("name"));
    assertEquals("First line\nSecond line", result.getLeft().get("description"));
  }

  @Test
  void parseMultilineWithIndentation() {
    String markdown = """
      ---
      name: find-platform-skills
      description: 引导用户在平台技能广场用
        search_skill 按关键词检索技能
      metadata: { "emoji": "🔍" }
      ---

      # Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("find-platform-skills", result.getLeft().get("name"));
    // YAML folded scalar (> or no indicator) joins lines with space
    assertEquals("引导用户在平台技能广场用 search_skill 按关键词检索技能", result.getLeft().get("description"));
  }

  @Test
  void parseStructuredMetadata() {
    String markdown = """
      ---
      name: cron
      description: 定时任务
      metadata: { "emoji": "⏰" }
      ---

      # 定时任务""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("cron", result.getLeft().get("name"));
    assertEquals("定时任务", result.getLeft().get("description"));
    // metadata should be parsed as JSON string (preserving original formatting)
    assertEquals("{ \"emoji\": \"⏰\" }", result.getLeft().get("metadata"));
  }

  @Test
  void parseNumberAndBooleanValues() {
    String markdown = """
      ---
      name: test
      count: 42
      enabled: true
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("test", result.getLeft().get("name"));
    assertEquals("42", result.getLeft().get("count"));
    assertEquals("true", result.getLeft().get("enabled"));
  }

  @Test
  void parseListValue() {
    String markdown = """
      ---
      name: test
      tags:
        - tag1
        - tag2
        - tag3
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("test", result.getLeft().get("name"));
    assertEquals("[\"tag1\",\"tag2\",\"tag3\"]", result.getLeft().get("tags"));
  }

  @Test
  void parseNestedObject() {
    String markdown = """
      ---
      name: test
      config:
        timeout: 30
        retries: 3
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);

    assertEquals("test", result.getLeft().get("name"));
    String config = result.getLeft().get("config");
    assertTrue(config.contains("\"timeout\":30"));
    assertTrue(config.contains("\"retries\":3"));
  }

  @Test
  void parseWithCRLF() {
    String markdown = """
      ---\r
      name: test\r
      description: CRLF test\r
      ---\r
      \r
      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("test", result.getLeft().get("name"));
    assertEquals("CRLF test", result.getLeft().get("description"));
    // parse 返回完整 markdown，正文紧随 front matter 之后
    assertTrue(result.getRight().endsWith("Content"));
  }

  @Test
  void parseWithMixedLineEndings() {
    String markdown = """
      ---\r
      name: test
      description: mixed\r
      ---\r

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("test", result.getLeft().get("name"));
    assertEquals("mixed", result.getLeft().get("description"));
  }

  @Test
  void parseEmptyFrontMatter() {
    String markdown = """
      ---
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertTrue(result.getLeft().isEmpty());
    // parse 返回完整 markdown，空 front matter 后紧接正文
    assertTrue(result.getRight().endsWith("Content"));
  }

  @Test
  void parseIncompleteFrontMatter() {
    // Missing closing ---
    String markdown = """
      ---
      name: test

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    // Should treat as no front matter
    assertTrue(result.getLeft().isEmpty());
    assertEquals(markdown, result.getRight());
  }

  @Test
  void parseKeyWithHyphen() {
    String markdown = """
      ---
      my-key: value-with-hyphen
      another-key: another-value
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("value-with-hyphen", result.getLeft().get("my-key"));
    assertEquals("another-value", result.getLeft().get("another-key"));
  }

  @Test
  void parseValueWithColon() {
    // When value contains colon, the inline value should be preferred over YAML parsed value
    String markdown = """
      ---
      url: https://example.com:8080/path
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("https://example.com:8080/path", result.getLeft().get("url"));
  }

  @Test
  void parseComplexMarkdownContent() {
    String markdown = """
      ---
      name: complex-skill
      description: A complex skill with various features
      version: 1.0.0
      ---

      # Complex Skill

      ## Features

      - Feature 1
      - Feature 2

      ```python
      print("hello, world")
      ```

      > Blockquote here
      """;

    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("complex-skill", result.getLeft().get("name"));
    assertEquals("A complex skill with various features", result.getLeft().get("description"));
    assertEquals("1.0.0", result.getLeft().get("version"));
    assertTrue(result.getRight().contains("# Complex Skill"));
    assertTrue(result.getRight().contains("```python"));
  }

  @Test
  void preservesOrderOfKeys() {
    String markdown = """
      ---
      z-key: z-value
      a-key: a-value
      m-key: m-value
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    String[] keys = result.getLeft().keySet().toArray(new String[0]);
    assertEquals("z-key", keys[0]);
    assertEquals("a-key", keys[1]);
    assertEquals("m-key", keys[2]);
  }

  @Test
  void parseYamlBlockScalar() {
    String markdown = """
      ---
      name: test
      script: |
        line 1
        line 2
        line 3
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("test", result.getLeft().get("name"));
    String script = result.getLeft().get("script");
    assertTrue(script.contains("line 1"));
    assertTrue(script.contains("line 2"));
    assertTrue(script.contains("line 3"));
  }

  @Test
  void parseFoldedBlockScalar() {
    String markdown = """
      ---
      name: test
      description: >
        This is a long description
        that spans multiple lines
        but should be folded.
      ---

      Content""";
    Pair<Map<String, String>, String> result = AgentSkillMarkdownParser.parse(markdown);
    assertEquals("test", result.getLeft().get("name"));
    String desc = result.getLeft().get("description");
    // Folded scalar should be joined with spaces
    assertTrue(desc.contains("This is a long description"));
  }
}
