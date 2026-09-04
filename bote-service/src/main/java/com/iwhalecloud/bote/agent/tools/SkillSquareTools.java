package com.iwhalecloud.bote.agent.tools;

import com.iwhalecloud.bote.agent.annotation.Tool;
import com.iwhalecloud.bote.agent.annotation.ToolParam;
import com.iwhalecloud.bote.agent.tool.context.ToolContext;
import com.iwhalecloud.bote.agent.tool.exception.ToolExecutionException;
import com.iwhalecloud.bote.dto.skill.query.SkillSquareQueryParams;
import com.iwhalecloud.bote.service.skill.ISkillSquareService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 技能广场对话工具：search_skill 按关键词检索广场技能；install_skill 按技能编码安装
 *
 * @author skill-square
 * @since 2026-03-18
 */
public final class SkillSquareTools {
  /** 工具名称: 搜索技能 */
  public static final String TOOL_NAME_SEARCH_SKILL = "search_skill";
  /** 工具名称: 安装技能 */
  public static final String TOOL_NAME_INSTALL_SKILL = "install_skill";
  /** 工具名称列表 */
  public static final List<String> TOOL_NAMES = List.of(TOOL_NAME_SEARCH_SKILL, TOOL_NAME_INSTALL_SKILL);
  private static final ISkillSquareService skillSquareService = SpringUtil.getBean(ISkillSquareService.class);

  private SkillSquareTools() {
  }

  @Tool(
    name = TOOL_NAME_SEARCH_SKILL,
    description = "Search skills in the platform skill square by keyword (name, description). " +
      "Keyword is required. Returns skillCode and skillName list. Use install_skill with skillCode to install."
  )
  public static String searchSkill(@ToolParam(description = "Search keyword (required, non-empty)") String keyword,
                                   @ToolParam(description = "Max results, default 20, max 50") @Nullable Integer limit) {
    if (StringUtils.isBlank(keyword)) {
      throw new ToolExecutionException("Error: 请输入搜索关键词（可匹配技能名称、描述等）");
    }

    int maxResults = limit != null && limit > 0 ? Math.min(limit, 50) : 20;
    SkillSquareQueryParams params = new SkillSquareQueryParams();
    params.setKeyword(keyword.trim());
    params.setPageNum(1);
    params.setPageSize(maxResults);
    params.setType("all");

    var page = skillSquareService.queryPage(params);
    List<Map<String, Object>> results = new ArrayList<>();
    for (var item : page.getList()) {
      Map<String, Object> m = new HashMap<>(2);
      m.put("skillCode", item.getSkillCode());
      m.put("skillName", item.getSkillName());
      results.add(m);
    }

    Map<String, Object> out = new HashMap<>();
    out.put("success", true);
    out.put("list", results);
    out.put("total", results.size());
    return JsonUtil.toJsonString(out);
  }

  @Tool(
    name = TOOL_NAME_INSTALL_SKILL,
    description = "Install a skill from the platform skill square to the current dialog agent. " +
      "Provide skillCode from search_skill. Looks up the skill in the database and installs if online."
  )
  public static String installSkill(
    @ToolParam(description = "Skill code from search_skill") String skillCode,
    @Nullable ToolContext toolContext
  ) {
    Assert.notNull(toolContext, "toolContext is required");
    Assert.notNull(toolContext.tenantId(), "tenantId is required");
    Assert.notNull(toolContext.botId(), "botId is required");

    if (StringUtils.isBlank(skillCode)) {
      throw new ToolExecutionException("Error: 请提供技能编码 skillCode（可先调用 search_skill 获取）");
    }

    var result = skillSquareService.installBySkillCode(
      skillCode.trim(), toolContext.tenantId(), toolContext.botId(), "dialogue");
    if (!result.isSuccess()) {
      throw new ToolExecutionException("Error: " + result.getResultMsg());
    }
    var resp = result.getResultObject();
    return JsonUtil.toJsonString(Map.of(
      "success", true,
      "skillCode", skillCode.trim(),
      "skillName", resp.getSkillName(),
      "agentSkillId", resp.getAgentSkillId()
    ));
  }
}
