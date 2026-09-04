package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.generator.flow.SkillBasicInfoDTO;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * AI 生成器服务
 *
 * @author chen.linfa
 * @since 2025-01-03
 */
public interface IAiGeneratorService {

  /**
   * 优化场景提示词
   *
   * @param tenantId 租户 ID
   * @param request 请求
   * @return 结果
   */
  String perfectScenePrompt(Long tenantId, String request);

  /**
   * 生成代码块
   *
   * @param tenantId 租户 ID
   * @param request 请求
   * @param nodeType 工作流节点类型
   * @param type 类型
   * @return 结果
   */
  String generateScript(Long tenantId, String request, @Nullable String nodeType, String type);

  /**
   * 生成提示词
   *
   * @param request 请求
   * @return 结果
   */
  String generatePrompt(String request);

  /**
   * 一句话生成智能体场景
   *
   * @param request 请求
   * @return 结果
   */
  Map<String, Object> aiGenerateScene(Long tenantId, String request);

  /**
   * 生成发布说明
   *
   * @param tenantId 租户 ID
   * @param botId 应用 ID
   * @param request 用户请求
   * @return 生成的发布说明
   */
  Map<String, Object> generatePublishRemark(Long tenantId, Long botId, String request);

  /**
   * 生成智能体开场白
   *
   * @param tenantId 租户 ID
   * @param botName 智能体名称
   * @param botDescription 智能体描述
   * @param request 用户请求
   * @return 生成的开场白
   */
  String generatePrologue(Long tenantId, String botName, String botDescription, String request);

  /**
   * 生成工作流节点系统提示词
   *
   * @param tenantId 租户 ID
   * @param request 用户输入内容
   * @return 生成的系统提示词
   */
  String generateLLMNodePrompt(Long tenantId, String request);

  /**
   * 生成智能体描述
   *
   * @param tenantId 租户 ID
   * @param botName 智能体名称
   * @param request 用户请求
   * @return 生成的智能体描述（Markdown格式），包含核心能力、能力边界、示例问法
   */
  String generateBotDescription(Long tenantId, String botName, String request);

  /**
   * 生成知识问答节点系统提示词
   *
   * @param tenantId 租户 ID
   * @param knowledgeIds 知识库 ID 列表
   * @param request 用户请求
   * @return 生成的系统提示词（包含 {content} 和 {question} 占位符）
   */
  String generateKnowledgeQaNodePrompt(Long tenantId, List<Long> knowledgeIds, String request);

  /**
   * 生成 Agent 节点系统提示词
   *
   * @param tenantId 租户 ID
   * @param request 请求内容（功能大意）
   * @param skills 节点技能信息列表（包含技能编码和技能名称）
   * @param supportVision 是否支持视觉
   * @return 生成的系统提示词
   */
  String generateAgentNodePrompt(Long tenantId, String request, List<SkillBasicInfoDTO> skills, Boolean supportVision);

}
