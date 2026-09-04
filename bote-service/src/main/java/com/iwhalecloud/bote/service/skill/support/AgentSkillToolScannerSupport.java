package com.iwhalecloud.bote.service.skill.support;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * 扫描技能文本中的 bote 槽位，用于写入 bt_agent_skill_tool
 *
 * @author qian.sisheng
 * @since 2026-04-28
 */
public final class AgentSkillToolScannerSupport {

  private static final Pattern SLOT_PATTERN = Pattern.compile(
    "\\$\\{bote\\.(agent|skill|service|mcp|knowledgeRetrieval|knowledge\\.docchain|page|workflow|pageFunc|sql)\\.([0-9]+)\\.([^}]+)}");

  private AgentSkillToolScannerSupport() {
  }

  /**
   * 从文本中解析槽位，按 slotType + targetId 去重
   *
   * @param content SKILL.md 文本
   * @return slotType -> (targetId -> 槽位详情)
   */
  public static Map<String, Map<Long, String>> parseTools(String content) {
    Map<String, Map<Long, String>> out = new LinkedHashMap<>();
    if (StringUtils.isBlank(content) || !content.contains("${bote.")) {
      return out;
    }
    Matcher matcher = SLOT_PATTERN.matcher(content);
    while (matcher.find()) {
      String toolType = normalizeToolType(matcher.group(1));
      Long toolId = Long.parseLong(matcher.group(2));
      String decodedName = decodeToolName(matcher.group(3));
      out.computeIfAbsent(toolType, k -> new LinkedHashMap<>()).putIfAbsent(toolId, decodedName);
    }
    return out;
  }

  /**
   * URL 解码槽位中的工具名称。
   */
  private static String decodeToolName(String encodedName) {
    if (StringUtils.isBlank(encodedName)) {
      return "";
    }
    return URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
  }

  /**
   * 将正则分组映射为统一槽位类型编码（与文档一致）
   */
  private static String normalizeToolType(String group) {
    if ("knowledge.docchain".equals(group)) {
      return "knowledge.docchain";
    }
    return group;
  }
}
