package com.iwhalecloud.bote.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.dto.model.SkillToolDTO;
import com.iwhalecloud.bote.dto.orchestration.context.SceneOrchestrationContext;
import com.iwhalecloud.bote.dto.scene.SimpleScenePromptDTO;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bote.mapper.skill.PromptManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 场景提示词辅助类
 *
 * @author bianjp
 * @since 2024-08-02
 */
public final class ScenePromptUtil {
  private static final SceneQueryMapper sceneQueryMapper = SpringUtil.getBean(SceneQueryMapper.class);
  private static final PromptManageMapper promptManageMapper = SpringUtil.getBean(PromptManageMapper.class);

  private ScenePromptUtil() {
  }

  /**
   * 解析场景提示词
   *
   * @param sceneId 场景 ID
   * @return 提示词
   */
  @Nullable
  public static String resolveScenePrompt(SceneOrchestrationContext context, Long tenantId, Long sceneId, @Nullable Long modelId, List<SkillToolDTO> sceneTools) {
    SimpleScenePromptDTO scenePrompt = sceneQueryMapper.selectPromptBySceneId(tenantId, sceneId);
    if (scenePrompt == null) {
      return null;
    }

    try {
      SceneContextUtil.setContext(context);
      String prompt;
      if (scenePrompt.getPromptId() != null) {
        Assert.notNull(modelId, "大模型 ID 不能为空");
        prompt = resolvePrompt(tenantId, modelId, scenePrompt.getPromptId(), expression -> resolveScenePromptParam(expression, sceneTools));
      }
      else {
        prompt = TemplateUtil.resolveTemplate(scenePrompt.getScenePrompt(), expression -> resolveScenePromptParam(expression, sceneTools));
      }
      return StringUtils.isEmpty(prompt) ? null : prompt;
    }
    finally {
      SceneContextUtil.removeContext();
    }
  }

  /**
   * 解析场景提示词中的参数
   */
  @Nullable
  private static Object resolveScenePromptParam(String expression, List<SkillToolDTO> sceneTools) {
    // 支持引用系统参数和登录信息
    if (expression.startsWith("system.") || expression.startsWith("session.")) {
      return SceneParamUtil.getParamValue("$." + expression);
    }
    // 解析技能名称
    if (expression.startsWith("skill.")) {
      String skillName = resolveSkillName(expression, sceneTools);
      if (skillName != null) {
        return skillName;
      }
    }
    // 未知参数原样返回
    return "${" + expression + "}";
  }

  /**
   * 解析技能名称（传给大模型的 function 名称）
   */
  @Nullable
  private static String resolveSkillName(String expression, List<SkillToolDTO> sceneTools) {
    String[] pieces = StringUtils.split(expression, '.');
    if (!NumberUtils.isCreatable(pieces[2])) {
      return null;
    }
    String skillType = pieces[1];
    // MCP 技能需要指定工具，表达式为 skill.mcp.SKILL_ID.MCP_TOOL_NAME.SKILL_NAME
    if (StepType.MCP.equals(skillType) && pieces.length == 5) {
      return pieces[3];
    }
    // 普通技能表达式为 skill.SKILL_TYPE.SKILL_ID.SKILL_NAME
    else if (!StepType.MCP.equals(skillType) && pieces.length == 4) {
      Long skillId = Long.parseLong(pieces[2]);
      SkillToolDTO skillTool = IterableUtils.find(sceneTools, t -> skillId.equals(t.getSkillId()) && skillType.equals(t.getSkillType()));
      if (skillTool != null) {
        return skillTool.getTool().getFunction().getName();
      }
      else {
        // 找不到技能时，使用技能名称
        return URLDecoder.decode(pieces[3], StandardCharsets.UTF_8);
      }
    }
    return null;
  }

  /**
   * 解析提示词引用
   *
   * @param modelId 大模型 ID
   * @param promptId 提示词 ID
   * @param paramResolver 参数值解析器
   * @return 提示词
   */
  @Nullable
  public static String resolvePrompt(Long tenantId, Long modelId, Long promptId, Function<String, Object> paramResolver) {
    // TODO 增加提示词缓存
    String content = promptManageMapper.selectPromptContentByPromptIdAndModelId(tenantId, promptId, modelId);
    if (StringUtils.isEmpty(content)) {
      return null;
    }
    // 解析提示词中的参数
    List<Message> messageList = JsonUtil.parseJsonRequired(content, new TypeReference<List<Message>>() {
    });
    //取第一个system系统提示词
    for (Message message : messageList) {
      if (message instanceof SystemMessage systemMessage) {
        return TemplateUtil.resolveTemplate(systemMessage.getContent(), paramResolver);
      }
    }
    return null;
  }

}
