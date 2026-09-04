package com.iwhalecloud.bote.agent.skill;

import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.mapper.mcp.McpServerManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillQueryMapper;
import com.iwhalecloud.bote.sandbox.api.SandboxClient;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileReadResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileSearchResult;
import com.iwhalecloud.bote.sandbox.dto.SandboxFileWriteResult;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * Agent Skill 槽位替换辅助类
 *
 * @author chen.linfa
 * @since 2026-04-30
 */
@SuppressWarnings("PMD.GuardLogStatement")
@SuppressFBWarnings("REDOS")
public final class AgentSkillSlotReplaceHelper {
  private static final Logger logger = LoggerFactory.getLogger(AgentSkillSlotReplaceHelper.class);
  private static final SkillQueryMapper skillQueryMapper = SpringUtil.getBean(SkillQueryMapper.class);

  private static final McpServerManageMapper mcpServerManageMapper = SpringUtil.getBean(McpServerManageMapper.class);

  /** 技能槽位占位符： ${bote.skillType.123} */
  private static final Pattern SKILL_SLOT_PATTERN = Pattern.compile("\\$\\{bote\\.(api|service|workflow|page|pagefunc|pageFunc|sql|mcp)\\.(\\d+)(?:\\.[^}]+)?}");

  /** 本体平台槽位占位符：${ontology.object|rule|action.xxx} */
  private static final Pattern ONTOLOGY_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{ontology\\.(object|rule|action)\\.([A-Za-z0-9_]+)}");

  private static final String ONTOLOGY_RULE_TOOL_NAME = "ontology_rule_execute";

  private static final String ONTOLOGY_ACTION_TOOL_NAME = "ontology_action_execute";

  private static final Map<String, Consumer<AgentSkillSlotIdGroup>> slotValueConsumerMap = buildSlotValueConsumerMap();

  private AgentSkillSlotReplaceHelper() {
  }

  /**
   * 替换技能槽位，并回写到客户端/沙箱环境中的 SKILL.md。
   */
  public static void replaceSkillSlotsAndPersist(Long userId, Long tenantId, List<AgentSkillSpec> skills, String skillsDir,
                                                 boolean useSandbox, @Nullable SandboxClient sandboxClient) {
    if (skills.isEmpty()) {
      return;
    }
    Map<String, String> slotValueMap = buildSkillSlotValueMap(tenantId, skills);
    if (slotValueMap.isEmpty()) {
      return;
    }
    replaceSkillSlots(skills, slotValueMap);
    if (useSandbox) {
      replaceSkillSlotsInSandboxFiles(userId, sandboxClient, skillsDir, slotValueMap);
    }
    else {
      replaceSkillSlotsInLocalFiles(skillsDir, slotValueMap);
    }
  }

  /**
   * 构建技能槽位值映射。
   */
  public static Map<String, String> buildSkillSlotValueMap(Long tenantId, List<AgentSkillSpec> skills) {
    if (skills.isEmpty()) {
      return Map.of();
    }
    Map<String, String> slotValueMap = buildSlotValueMap(tenantId, skills);
    if (slotValueMap.isEmpty()) {
      return Map.of();
    }
    return Collections.unmodifiableMap(slotValueMap);
  }

  /**
   * 使用已有槽位映射替换技能内容。
   */
  public static void replaceSkillSlots(List<AgentSkillSpec> skills, Map<String, String> slotValueMap) {
    if (skills.isEmpty() || slotValueMap.isEmpty()) {
      return;
    }
    for (AgentSkillSpec skill : skills) {
      skill.setContent(replaceSlotsInText(skill.getContent(), slotValueMap));
    }
  }

  /**
   * 使用已有槽位映射替换文本中的槽位占位符。
   */
  public static String replaceSlotsInText(String text, Map<String, String> slotValueMap) {
    String afterBote = replaceSlots(text, slotValueMap);
    return replaceOntologySlots(afterBote, slotValueMap);
  }

  /**
   * 替换本地目录下全部 SKILL.md 的槽位。
   */
  private static void replaceSkillSlotsInLocalFiles(String skillsDir, Map<String, String> slotValueMap) {
    for (File file : FileUtils.listFiles(new File(skillsDir), new NameFileFilter("SKILL.md"), TrueFileFilter.TRUE)) {
      try {
        String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String replaced = replaceSlotsInText(markdown, slotValueMap);
        if (!markdown.equals(replaced)) {
          FileUtils.writeStringToFile(file, replaced, StandardCharsets.UTF_8);
        }
      }
      catch (Exception e) {
        logger.warn("Failed to replace slots in local SKILL.md: path={}", file.getAbsolutePath(), e);
      }
    }
  }

  /**
   * 替换远程沙箱目录下全部 SKILL.md 的槽位。
   */
  private static void replaceSkillSlotsInSandboxFiles(Long userId, @Nullable SandboxClient sandboxClient, String skillsDir, Map<String, String> slotValueMap) {
    assert sandboxClient != null;
    SandboxFileSearchResult searchResult = sandboxClient.searchFiles(skillsDir, "SKILL.md");
    if (!searchResult.isSuccess()) {
      logger.warn("Failed to search sandbox SKILL.md for slot replacement: userId={}, path={}, error={}", userId, skillsDir,
        searchResult.getErrorMessage());
      return;
    }
    for (String path : ListUtils.emptyIfNull(searchResult.getEntries())) {
      SandboxFileReadResult readResult = sandboxClient.readFile(path);
      if (!readResult.isSuccess()) {
        logger.warn("Failed to read sandbox SKILL.md for slot replacement: userId={}, path={}, error={}", userId, path,
          readResult.getErrorMessage());
        continue;
      }
      String content = StringUtils.defaultString(readResult.getContent());
      String replaced = replaceSlotsInText(content, slotValueMap);
      if (content.equals(replaced)) {
        continue;
      }
      SandboxFileWriteResult writeResult = sandboxClient.writeFile(path, replaced);
      if (!writeResult.isSuccess()) {
        logger.warn("Failed to write sandbox SKILL.md after slot replacement: userId={}, path={}, error={}", userId, path,
          writeResult.getErrorMessage());
      }
    }
  }

  /**
   * 收集并查询槽位对应的技能编码。
   */
  private static Map<String, String> buildSlotValueMap(Long tenantId, List<AgentSkillSpec> skills) {
    Map<String, List<Long>> slotIdGroups = new HashMap<>();
    for (AgentSkillSpec skill : skills) {
      collectSlotIds(skill.getContent(), slotIdGroups);
    }
    Map<String, String> slotValueMap = new HashMap<>();
    collectOntologySlotValues(skills, slotValueMap);

    if (slotIdGroups.isEmpty()) {
      return slotValueMap;
    }
    AgentSkillSlotIdGroup group = new AgentSkillSlotIdGroup();
    group.setTenantId(tenantId);
    group.setSlotIdGroups(slotIdGroups);
    group.setSlotValueMap(slotValueMap);
    slotValueConsumerMap.values().forEach(consumer -> consumer.accept(group));
    return group.getSlotValueMap();
  }

  private static Map<String, Consumer<AgentSkillSlotIdGroup>> buildSlotValueConsumerMap() {
    return ImmutableMap.<String, Consumer<AgentSkillSlotIdGroup>>builder()
      .put(StepType.SERVICE, AgentSkillSlotReplaceHelper::fillServiceSlotValues)
      .put(StepType.MCP, AgentSkillSlotReplaceHelper::fillMcpSlotValues)
      .put(StepType.PAGE, AgentSkillSlotReplaceHelper::fillPageSlotValues)
      .put(StepType.PAGE_FUNC, AgentSkillSlotReplaceHelper::fillPageFuncSlotValues)
      .put(StepType.SQL, AgentSkillSlotReplaceHelper::fillSqlSlotValues)
      .put(StepType.WORKFLOW, AgentSkillSlotReplaceHelper::fillWorkflowSlotValues)
      .build();
  }

  private static <T> void processIfPresent(AgentSkillSlotIdGroup group, String slotType, Supplier<List<T>> query,
                                           Consumer<T> itemConsumer) {
    if (!group.getSlotIdGroups().containsKey(slotType)) {
      return;
    }
    for (T item : ListUtils.emptyIfNull(query.get())) {
      itemConsumer.accept(item);
    }
  }

  private static void fillServiceSlotValues(AgentSkillSlotIdGroup group) {
    processIfPresent(group, StepType.SERVICE,
      () -> skillQueryMapper.selectApiServicesByIds(group.getTenantId(), group.getSlotIdGroups().get(StepType.SERVICE)),
      (api) -> putSlotValue(group.getSlotValueMap(), "bote." + StepType.SERVICE + ".", api.getServiceId(), api.getServiceCode()));
  }

  private static void fillMcpSlotValues(AgentSkillSlotIdGroup group) {
    processIfPresent(group, StepType.MCP,
      () -> mcpServerManageMapper.getMcpServersByIds(group.getSlotIdGroups().get(StepType.MCP), group.getTenantId()),
      (mcp) -> putSlotValue(group.getSlotValueMap(), "bote." + StepType.MCP + ".", mcp.getServerId(), mcp.getServerId().toString()));
  }

  private static void fillPageSlotValues(AgentSkillSlotIdGroup group) {
    processIfPresent(group, StepType.PAGE,
      () -> skillQueryMapper.selectPagesByIds(group.getTenantId(), group.getSlotIdGroups().get(StepType.PAGE)),
      (page) -> putSlotValue(group.getSlotValueMap(), "bote." + StepType.PAGE + ".", page.getPageId(), page.getPageCode()));
  }

  private static void fillPageFuncSlotValues(AgentSkillSlotIdGroup group) {
    processIfPresent(group, StepType.PAGE_FUNC,
      () -> skillQueryMapper.selectPageFuncListByIds(group.getTenantId(), group.getSlotIdGroups().get(StepType.PAGE_FUNC)),
      (pageFunc) -> putSlotValue(group.getSlotValueMap(), "bote." + StepType.PAGE_FUNC + ".", pageFunc.getPageFuncId(),
        pageFunc.getFuncCode()));
  }

  private static void fillSqlSlotValues(AgentSkillSlotIdGroup group) {
    processIfPresent(group, StepType.SQL,
      () -> skillQueryMapper.selectSqlServicesByIds(group.getTenantId(), group.getSlotIdGroups().get(StepType.SQL)),
      (sql) -> putSlotValue(group.getSlotValueMap(), "bote." + StepType.SQL + ".", sql.getServiceId(), sql.getServiceCode()));
  }

  private static void fillWorkflowSlotValues(AgentSkillSlotIdGroup group) {
    processIfPresent(group, StepType.WORKFLOW,
      () -> skillQueryMapper.selectFlowsByIds(group.getTenantId(), group.getSlotIdGroups().get(StepType.WORKFLOW)),
      (flow) -> putSlotValue(group.getSlotValueMap(), "bote." + StepType.WORKFLOW + ".", flow.getFlowId(), flow.getFlowCode()));
  }

  private static void putSlotValue(Map<String, String> slotValueMap, String slotPrefix, Long id, String code) {
    slotValueMap.put(slotPrefix + id, "`" + code + "`");
  }

  /**
   * 收集本体平台槽位替换值（规则/动作，object 当前无使用场景，替换为空串）。
   */
  private static void collectOntologySlotValues(List<AgentSkillSpec> skills, Map<String, String> slotValueMap) {
    for (AgentSkillSpec skill : skills) {
      if (StringUtils.isEmpty(skill.getContent())) {
        continue;
      }
      Matcher matcher = ONTOLOGY_PLACEHOLDER_PATTERN.matcher(skill.getContent());
      while (matcher.find()) {
        String slotType = matcher.group(1);
        String slotCode = matcher.group(2);
        String key = "ontology." + slotType + "." + slotCode;
        slotValueMap.put(key, buildOntologySlotReplacement(slotType, slotCode));
      }
    }
  }

  private static String buildOntologySlotReplacement(String slotType, String slotCode) {
    if ("rule".equals(slotType)) {
      return "(调用工具`" + ONTOLOGY_RULE_TOOL_NAME + "`，规则编码为`" + slotCode + "`，从本体信息获取对应 rule 定义)";
    }
    if ("action".equals(slotType)) {
      return "(调用工具`" + ONTOLOGY_ACTION_TOOL_NAME + "`，动作编码为`" + slotCode + "`，从本体信息获取对应 action 定义)";
    }
    return "";
  }

  private static String replaceOntologySlots(String text, Map<String, String> slotValueMap) {
    if (StringUtils.isEmpty(text)) {
      return text;
    }
    Matcher matcher = ONTOLOGY_PLACEHOLDER_PATTERN.matcher(text);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String key = "ontology." + matcher.group(1) + "." + matcher.group(2);
      String replacement = slotValueMap.get(key);
      if (replacement == null) {
        replacement = buildOntologySlotReplacement(matcher.group(1), matcher.group(2));
      }
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private static void collectSlotIds(String text, Map<String, List<Long>> slotIdGroups) {
    if (StringUtils.isEmpty(text)) {
      return;
    }
    Matcher matcher = SKILL_SLOT_PATTERN.matcher(text);
    while (matcher.find()) {
      String slotType = matcher.group(1);
      Long id = Long.parseLong(matcher.group(2));
      if (slotIdGroups.containsKey(slotType)) {
        slotIdGroups.get(slotType).add(id);
      }
      else {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        slotIdGroups.put(slotType, ids);
      }
    }
  }

  private static String replaceSlots(String text, Map<String, String> slotValueMap) {
    if (StringUtils.isEmpty(text)) {
      return text;
    }
    Matcher matcher = SKILL_SLOT_PATTERN.matcher(text);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String key = "bote." + matcher.group(1) + "." + matcher.group(2);
      String replacement = slotValueMap.getOrDefault(key, matcher.group(0));
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
