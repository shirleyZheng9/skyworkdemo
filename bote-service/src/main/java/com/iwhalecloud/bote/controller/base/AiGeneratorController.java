package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.query.AiGenerateParams;
import com.iwhalecloud.bote.service.base.IAiGeneratorService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI生成服务
 *
 * @author chen.linfa
 * @since 2025-01-06
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/aiGenerate", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：AI生成服务")
public class AiGeneratorController {
  private final IAiGeneratorService generatorService;

  @PostMapping("perfectScenePrompt")
  @Operation(summary = "优化场景提示词")
  public ResultVO<String> perfectScenePrompt(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    return ResultVO.success(generatorService.perfectScenePrompt(params.getTenantId(), params.getRequest()));
  }

  @PostMapping("generateScript")
  @Operation(summary = "生成代码块")
  public ResultVO<String> generateScript(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    Assert.hasText(params.getType(), "类型不能为空");
    return ResultVO.success(
      generatorService.generateScript(params.getTenantId(), params.getRequest(), params.getNodeType(), params.getType()));
  }

  @PostMapping("generatePrompt")
  @Operation(summary = "生成提示词")
  public ResultVO<String> generatePrompt(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    return ResultVO.success(generatorService.generatePrompt(params.getRequest()));
  }

  @PostMapping("generatePublishRemark")
  @Operation(summary = "生成能力描述")
  public ResultVO<Map<String, Object>> generatePublishRemark(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    Assert.notNull(params.getSceneId(), "应用 ID 不能为空");
    return ResultVO.success(generatorService.generatePublishRemark(params.getTenantId(), params.getSceneId(), params.getRequest()));
  }

  @PostMapping("generatePrologue")
  @Operation(summary = "生成智能体开场白")
  public ResultVO<String> generatePrologue(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    Assert.hasText(params.getSceneName(), "智能体名称不能为空");
    Assert.hasText(params.getSceneDesc(), "智能体描述不能为空");
    return ResultVO.success(
      generatorService.generatePrologue(params.getTenantId(), params.getSceneName(), params.getSceneDesc(), params.getRequest()));
  }

  @PostMapping("generateLLMNodePrompt")
  @Operation(summary = "生成工作流节点系统提示词")
  public ResultVO<String> generateLLMNodePrompt(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    return ResultVO.success(generatorService.generateLLMNodePrompt(params.getTenantId(), params.getRequest()));
  }

  @PostMapping("generateSceneDescription")
  @Operation(summary = "生成智能体描述")
  public ResultVO<String> generateSceneDescription(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    Assert.hasText(params.getSceneName(), "智能体名称不能为空");
    return ResultVO.success(generatorService.generateBotDescription(params.getTenantId(), params.getSceneName(), params.getRequest()));
  }

  @PostMapping("generateKnowledgeQaNodePrompt")
  @Operation(summary = "生成知识问答节点系统提示词")
  public ResultVO<String> generateKnowledgeQaNodePrompt(@RequestBody AiGenerateParams params) {
    Assert.notNull(params.getKnowledgeIds(), "知识库 ID 不能为空");
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    return ResultVO.success(generatorService.generateKnowledgeQaNodePrompt(
        params.getTenantId(), params.getKnowledgeIds(), params.getRequest()));
  }

  @PostMapping("generateAgentNodePrompt")
  @Operation(summary = "生成 Agent 节点系统提示词")
  public ResultVO<String> generateAgentNodePrompt(@RequestBody AiGenerateParams params) {
    Assert.hasText(params.getRequest(), "请求内容不能为空");
    return ResultVO.success(generatorService.generateAgentNodePrompt(params.getTenantId(), params.getRequest(),
      params.getSkills(), params.getSupportVision()));
  }
}
