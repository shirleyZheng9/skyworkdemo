package com.iwhalecloud.bote.loop.prompt.application;

import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.OverridePromptParamsDTO;
import com.iwhalecloud.bote.loop.client.prompt.execute.PromptExecuteService;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalRequest;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalResponse;
import com.iwhalecloud.bote.loop.prompt.application.convertor.ManageConvertor;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Reply;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IManageRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ExecuteParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.service.IPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Prompt执行服务实现
 * 迁移对应关系: Go语言modules/prompt/application.PromptExecuteApplicationImpl
 * - 功能: 提供Prompt执行相关服务
 * - 主要方法:
 * * executeInternal - 内部执行Prompt
 * * startPromptExecutorSpan - 开始Prompt执行器Span
 * * finishPromptExecutorSpan - 完成Prompt执行器Span
 * * getPromptByID - 根据ID获取Prompt
 * * overridePromptParams - 覆盖Prompt参数
 * <p>
 * Java实现说明:
 * - 对应Go的PromptExecuteApplicationImpl结构体
 * - 使用Spring服务注解
 * - 实现内部执行功能
 * - 支持参数覆盖
 * - 集成链路追踪
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go指针类型 -> Java对象引用
 * - Go切片类型 -> Java List
 */
@Service
@RequiredArgsConstructor
public class PromptExecuteServiceImpl implements PromptExecuteService {
  private final IPromptService promptService;
  private final IManageRepo manageRepo;

  @Override
  public ExecuteInternalResponse executeInternal(ExecuteInternalRequest request) {
    ExecuteInternalResponse response = new ExecuteInternalResponse();

    // 内部接口不鉴权
    // 根据ID获取Prompt
    Prompt promptDO = getPromptByID(request.getPromptId(), request.getVersion());

    // 覆盖Prompt参数
    overridePromptParams(promptDO, request.getOverridePromptParams());

    // 执行Prompt
    ExecuteParam executeParam = ExecuteParam.builder()
      .prompt(promptDO)
      .messages(ManageConvertor.batchMessageDTO2DO(request.getMessages()))
      .variableVals(ManageConvertor.batchVariableValDTO2DO(request.getVariableVals()))
      .singleStep(false) // 内部接口不支持单步调试
      .scenario(ManageConvertor.scenarioDTO2DO(request.getScenario()))
      .build();

    Reply reply = promptService.execute(executeParam);

    if (reply != null && reply.getItem() != null) {
      response.setMessage(ManageConvertor.messageDO2DTO(reply.getItem().getMessage()));
      response.setFinishReason(reply.getItem().getFinishReason());
      response.setUsage(ManageConvertor.tokenUsageDO2DTO(reply.getItem().getTokenUsage()));
    }

    return response;
  }


  /**
   * 根据ID获取Prompt
   * 迁移对应关系: Go语言getPromptByID
   */
  private Prompt getPromptByID(Long promptId, String version) {
    return manageRepo.getPrompt(GetPromptParam.builder()
      .promptId(promptId)
      .withCommit(true).withDraft(false)
      .commitVersion(version)
      .build());
  }

  /**
   * 覆盖Prompt参数
   * 迁移对应关系: Go语言overridePromptParams
   */
  private void overridePromptParams(Prompt promptDO, OverridePromptParamsDTO overrideParams) {
    if (promptDO == null || overrideParams == null) {
      return;
    }

    if (promptDO.getPromptDetail() != null && overrideParams.getModelConfig() != null) {
      promptDO.getPromptCommit().getPromptDetail()
        .setModelConfig(ManageConvertor.modelConfigDTO2DO(overrideParams.getModelConfig()));
    }
  }


}
