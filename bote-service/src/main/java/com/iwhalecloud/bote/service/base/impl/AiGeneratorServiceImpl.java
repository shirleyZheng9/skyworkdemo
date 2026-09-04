package com.iwhalecloud.bote.service.base.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.StepType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FreemarkerUtil;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeBaseManageService;
import com.iwhalecloud.bote.dto.base.BotDescriptionResultDTO;
import com.iwhalecloud.bote.dto.bot.BotDTO;
import com.iwhalecloud.bote.dto.generator.flow.SkillBasicInfoDTO;
import com.iwhalecloud.bote.service.base.IAiGeneratorService;
import com.iwhalecloud.bote.service.bot.IBotManageService;
import com.iwhalecloud.bote.service.model.helper.LargeModelAnswerHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AI 生成器服务实现
 *
 * @author chen.linfa
 * @since 2025-01-03
 */
@Service
@RequiredArgsConstructor
public class AiGeneratorServiceImpl implements IAiGeneratorService {
  private final LargeModelAnswerHelper modelAnswerHelper;
  private final IBotManageService botManageService;
  private final IKnowledgeBaseManageService knowledgeBaseManageService;

  @Override
  public String perfectScenePrompt(Long tenantId, String request) {
    String prompt = SystemParameter.SCENE_PERFECT_PROMPT.getValueFromDb();
    Map<String, Object> map = modelAnswerHelper.chat(tenantId, prompt, request,
      new TypeReference<Map<String, Object>>() {
      });
    return MapUtils.getString(map, "content", "");
  }

  @Override
  public String generateScript(Long tenantId, String request, @Nullable String nodeType, String type) {
    // PlayWright 节点
    if (StepType.PLAYWRIGHT.equals(nodeType)) {
      String prompt = SystemParameter.GENERATE_PLAYWRIGHT_PYTHON_SCRIPT.getValueFromDb();
      return modelAnswerHelper.generateCode(tenantId, prompt, request);
    }
    String prompt = BaseConsts.SCRIPT_TYPE_GROOVY.equalsIgnoreCase(type)
      ? SystemParameter.GENERATE_GROOVY_SCRIPT.getValueFromDb()
      : SystemParameter.GENERATE_PYTHON3_SCRIPT.getValueFromDb();
    Map<String, Object> map = modelAnswerHelper.chat(tenantId, prompt, request,
      new TypeReference<Map<String, Object>>() {
      });
    return MapUtils.getString(map, "content", "");
  }

  @Override
  public String generatePrompt(String request) {
    String template = SystemParameter.GENERATE_PROMPT.getValueFromDb();
    Map<String, Object> data = new HashMap<>(4);
    data.put("content", request);
    return FreemarkerUtil.process(template, data);
  }

  @Override
  public Map<String, Object> aiGenerateScene(Long tenantId, String request) {
    String prompt = SystemParameter.AI_GENERATE_PROMPT.getValueFromDb();
    Map<String, Object> result = modelAnswerHelper.chat(tenantId, prompt, request,
      new TypeReference<Map<String, Object>>() {
      });
    return MapUtils.emptyIfNull(result);
  }

  @Override
  public Map<String, Object> generatePublishRemark(Long tenantId, Long botId, String request) {
    BotDTO bot = botManageService.findBot(tenantId, botId);
    Assert.notNull(bot, "智能应用不存在");
    String prompt = SystemParameter.GENERATE_PUBLISH_REMARK_PROMPT.getValueFromDb();
    StringBuilder requestBuilder = new StringBuilder();
    requestBuilder.append("应用名称：").append(bot.getBotName()).append("\n");
    requestBuilder.append("应用描述：").append(bot.getBotUse()).append("\n");
    requestBuilder.append("用户补充描述：").append(request);
    String finalRequest = requestBuilder.toString();
    Map<String, Object> result = modelAnswerHelper.chat(tenantId, prompt, finalRequest, new TypeReference<>() {
    });
    return MapUtils.emptyIfNull(result);
  }

  @Override
  public String generatePrologue(Long tenantId, String botName, String botDescription, String request) {
    String prompt = SystemParameter.GENERATE_PROLOGUE_PROMPT.getValueFromDb();
    StringBuilder requestBuilder = new StringBuilder();
    requestBuilder.append("智能体名称：").append(botName).append("\n");
    requestBuilder.append("智能体描述：").append(botDescription).append("\n");
    requestBuilder.append("用户补充描述：").append(request);
    String finalRequest = requestBuilder.toString();
    Map<String, Object> map = modelAnswerHelper.chat(prompt, finalRequest, new TypeReference<>() {
    });
    return MapUtils.getString(map, "content", "");
  }

  @Override
  public String generateLLMNodePrompt(Long tenantId, String request) {
    String prompt = SystemParameter.GENERATE_LLM_NODE_PROMPT.getValueFromDb();
    Map<String, Object> map = modelAnswerHelper.chat(tenantId, prompt, request,
      new TypeReference<Map<String, Object>>() {
      });
    return MapUtils.getString(map, "content", "");
  }

  @Override
  public String generateBotDescription(Long tenantId, String botName, String request) {
    String prompt = SystemParameter.GENERATE_BOT_DESCRIPTION_PROMPT.getValueFromDb();
    StringBuilder requestBuilder = new StringBuilder();
    requestBuilder.append("智能体名称：").append(botName);
    requestBuilder.append("\n").append("用户补充描述：").append(request);
    String finalRequest = requestBuilder.toString();
    BotDescriptionResultDTO result = modelAnswerHelper.chat(tenantId, prompt, finalRequest,
      new TypeReference<BotDescriptionResultDTO>() {
      });
    if (result == null) {
      return "";
    }
    StringBuilder markdown = new StringBuilder();
    if (StringUtils.isNotEmpty(result.getCoreCapabilities())) {
      markdown.append("#核心能力（必填）：\n").append(result.getCoreCapabilities()).append("\n\n");
    }
    if (StringUtils.isNotEmpty(result.getCapabilityBoundary())) {
      markdown.append("#能力边界（必填）：\n").append(result.getCapabilityBoundary()).append("\n\n");
    }
    if (StringUtils.isNotEmpty(result.getExampleQuestions())) {
      markdown.append("#示例问法（必填）：\n").append(result.getExampleQuestions()).append("\n\n");
    }
    return markdown.toString().trim();
  }

  @Override
  public String generateKnowledgeQaNodePrompt(Long tenantId, List<Long> knowledgeIds, String request) {
    Assert.notEmpty(knowledgeIds, "知识库 ID 列表不能为空");

    StringBuilder requestBuilder = new StringBuilder();
    for (int i = 0; i < knowledgeIds.size(); i++) {
      Long knowledgeId = knowledgeIds.get(i);
      KnowledgeBaseDTO knowledgeBase = knowledgeBaseManageService.findKnowledgeBase(tenantId, knowledgeId);
      Assert.notNull(knowledgeBase, "知识库不存在: " + knowledgeId);

      String knowledgeDesc = StringUtils.isEmpty(knowledgeBase.getKnowledgeDesc())
        ? knowledgeBase.getKnowledgeName()
        : knowledgeBase.getKnowledgeDesc();

      if (i > 0) {
        requestBuilder.append("\n");
      }
      requestBuilder.append("知识库").append(i + 1).append("描述：").append(knowledgeDesc);
    }

    requestBuilder.append("\n").append("用户请求：").append(request);

    String prompt = SystemParameter.GENERATE_KNOWLEDGE_QA_NODE_PROMPT.getValueFromDb();
    String finalRequest = requestBuilder.toString();

    Map<String, Object> map = modelAnswerHelper.chat(tenantId, prompt, finalRequest,
      new TypeReference<Map<String, Object>>() {
      });

    return MapUtils.getString(map, "content", "");
  }

  @Override
  public String generateAgentNodePrompt(Long tenantId, String request, List<SkillBasicInfoDTO> skills,
                                         Boolean supportVision) {
    String prompt = SystemParameter.GENERATE_AGENT_NODE_PROMPT.getValueFromDb();
    StringBuilder requestBuilder = new StringBuilder();
    requestBuilder.append("功能大意：").append(request).append("\n\n");

    if (CollectionUtils.isNotEmpty(skills)) {
      requestBuilder.append("节点技能信息列表：\n");
      for (SkillBasicInfoDTO skill : skills) {
        if (skill != null) {
          String skillName = StringUtils.isNotEmpty(skill.getSkillName()) ? skill.getSkillName() : "";
          String skillCode = StringUtils.isNotEmpty(skill.getSkillCode()) ? skill.getSkillCode() : "";
          if (StringUtils.isNotEmpty(skillName) || StringUtils.isNotEmpty(skillCode)) {
            requestBuilder.append("- 技能名称：").append(skillName);
            if (StringUtils.isNotEmpty(skillCode)) {
              requestBuilder.append("，技能编码：").append(skillCode);
            }
            requestBuilder.append("\n");
          }
        }
      }
      requestBuilder.append("\n");
    }

    if (Boolean.TRUE.equals(supportVision)) {
      requestBuilder.append("是否支持视觉：是\n");
    }

    String finalRequest = requestBuilder.toString();
    Map<String, Object> map = modelAnswerHelper.chat(tenantId, prompt, finalRequest, new TypeReference<Map<String, Object>>() {
    });
    return MapUtils.getString(map, "content", "");
  }
}
