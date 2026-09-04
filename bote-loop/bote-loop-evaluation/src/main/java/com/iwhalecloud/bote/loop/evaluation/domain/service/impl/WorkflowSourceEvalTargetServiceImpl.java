package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineRequest;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Workflow;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetExecuteResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SkillFlowDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bote.service.orchestration.IOrchestrationEngine;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.skill.ISkillFlowManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 工作流源评估目标操作服务实现
 */
@Service
@RequiredArgsConstructor
public class WorkflowSourceEvalTargetServiceImpl extends AbstractSourceEvalTargetServiceImpl {
  private static final Logger logger = LoggerFactory.getLogger(WorkflowSourceEvalTargetServiceImpl.class);

  private final IOrchestrationEngine orchestrationEngine;
  private final ISkillFlowManageService skillFlowManageService;

  @Override
  public EvalTargetType evalType() {
    return EvalTargetType.WORKFLOW;
  }

  @Override
  public void validateInput(Long spaceId, List<ArgsSchema> inputSchema, EvalTargetInputData input) {
    input.validateInputSchema(inputSchema);
  }

  @Override
  public EvalTargetExecuteResult execute(Long spaceId, ExecuteEvalTargetParam param) {
    long startTime = System.currentTimeMillis();
    EvalTargetOutputData outputData = new EvalTargetOutputData();
    EvalTargetRunStatus runStatus = EvalTargetRunStatus.SUCCESS;

    try {
      OrchestrationEngineRequest request = buildOrchestrationRequest(spaceId, param);
      OrchestrationEngineResponse response = orchestrationEngine.run(request);
      processExecuteResult(response, outputData);
    }
    catch (Exception e) {
      handleExecutionError(e, outputData);
      runStatus = EvalTargetRunStatus.FAIL;
    }
    finally {
      setExecutionTime(outputData, startTime);
    }

    return buildExecuteResult(outputData, runStatus);
  }

  private OrchestrationEngineRequest buildOrchestrationRequest(Long spaceId, ExecuteEvalTargetParam param) {
    Long flowId = Long.parseLong(param.getSourceTargetId());
    OrchestrationEngineRequest request = new OrchestrationEngineRequest();
    request.setTenantId(spaceId);
    request.setFlowId(flowId);
    NonStreamFlowReplyHandler replyHandler = new NonStreamFlowReplyHandler(param.getSourceTargetId());
    request.setReplyHandler(replyHandler);
    if (param.getInput() != null && param.getInput().getInputFields() != null) {
      Map<String, Object> params = new HashMap<>();
      for (Map.Entry<String, com.iwhalecloud.bote.loop.evaluation.domain.entity.Content> entry : param.getInput().getInputFields().entrySet()) {
        if (entry.getValue() != null) {
          params.put(entry.getKey(), entry.getValue().getText());
        }
      }
      request.setParameters(params);

      com.iwhalecloud.bote.loop.evaluation.domain.entity.Content messageContent = param.getInput().getInputFields().get("message");
      if (messageContent != null && messageContent.getText() != null) {
        request.setMessageContent(messageContent.getText());
      }
    }
    return request;
  }

  private void processExecuteResult(OrchestrationEngineResponse response, EvalTargetOutputData outputData) {
    if (Boolean.TRUE.equals(response.getSuccess())) {
      if (response.getOutput() != null) {
        Map<String, com.iwhalecloud.bote.loop.evaluation.domain.entity.Content> outputFields = new HashMap<>();
        com.iwhalecloud.bote.loop.evaluation.domain.entity.Content content = new com.iwhalecloud.bote.loop.evaluation.domain.entity.Content();
        content.setContentType(ContentType.TEXT);
        // response.getOutput() 是 Map<String, Object>，需要转换为字符串
        content.setText(String.valueOf(response.getOutput()));
        outputFields.put("actual_output", content);
        outputData.setOutputFields(outputFields);
      }
    }
    else {
      throw new BssException(response.getFailMsg());
    }
  }

  private void handleExecutionError(Exception e, EvalTargetOutputData outputData) {
    logger.error("执行工作流失败", e);
    EvalTargetRunError error = new EvalTargetRunError();
    error.setMessage(e.getMessage());
    outputData.setEvalTargetRunError(error);
  }

  private void setExecutionTime(EvalTargetOutputData outputData, long startTime) {
    long timeCostMs = System.currentTimeMillis() - startTime;
    outputData.setTimeConsumingMs(timeCostMs);
  }

  private EvalTargetExecuteResult buildExecuteResult(EvalTargetOutputData outputData, EvalTargetRunStatus runStatus) {
    EvalTargetExecuteResult result = new EvalTargetExecuteResult();
    result.setOutputData(outputData);
    result.setStatus(runStatus);
    return result;
  }

  @Override
  public EvalTarget buildBySource(Long spaceId, String sourceTargetId, String sourceTargetVersion) {
    String userId = SessionContext.getCurrentUserId();

    EvalTarget evalTarget = new EvalTarget();
    evalTarget.setSpaceId(spaceId);
    evalTarget.setSourceTargetId(sourceTargetId);
      evalTarget.setEvalTargetType(EvalTargetType.WORKFLOW);

    EvalTargetVersion version = new EvalTargetVersion();
    version.setSpaceId(spaceId);
    version.setSourceTargetVersion(sourceTargetVersion);
      version.setEvalTargetType(EvalTargetType.WORKFLOW);

    Workflow workflow = new Workflow();
    workflow.setId(sourceTargetId);
    workflow.setVersion(sourceTargetVersion);
      version.setWorkflow(workflow);

    // 工作流的输入Schema需要根据实际工作流定义来构建
    // 这里先创建一个基础的输入Schema
    List<ArgsSchema> inputSchema = new ArrayList<>();
    ArgsSchema inputSchemaItem = new ArgsSchema();
    inputSchemaItem.setKey("input");
    inputSchemaItem.setSupportContentTypes(List.of(ContentType.TEXT));
    inputSchemaItem.setJsonSchema("{\"type\":\"object\"}");
    inputSchema.add(inputSchemaItem);
    version.setInputSchema(inputSchema);

    List<ArgsSchema> outputSchema = new ArrayList<>();
    ArgsSchema outputArgsSchema = new ArgsSchema();
    outputArgsSchema.setKey("output");
    outputArgsSchema.setSupportContentTypes(List.of(ContentType.TEXT, ContentType.MULTI_PART));
    outputArgsSchema.setJsonSchema("{\"type\":\"object\"}");
    outputSchema.add(outputArgsSchema);
    version.setOutputSchema(outputSchema);

    BaseInfo baseInfo = new BaseInfo();
    UserInfo createdBy = new UserInfo();
    UserInfo updatedBy = new UserInfo();
    createdBy.setUserId(userId);
    updatedBy.setUserId(userId);
    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    version.setBaseInfo(baseInfo);

    evalTarget.setEvalTargetVersion(version);
    evalTarget.setBaseInfo(baseInfo);

    return evalTarget;
  }

  @Override
  public ListSourceResult listSource(ListSourceParam param) {
    int page = buildPageByCursor(param.getCursor());

    SkillQueryParams queryParams = new SkillQueryParams();
    queryParams.setTenantId(param.getSpaceId());
    queryParams.setSearchContent(param.getKeyWord());
    queryParams.setPageNum(page);
    queryParams.setPageSize(param.getPageSize() != null ? param.getPageSize() : 20);

    PageInfo<SkillFlowDTO> pageInfo = skillFlowManageService.querySkillFlowPage(queryParams);

    List<EvalTarget> targets = new ArrayList<>();
    for (SkillFlowDTO flow : pageInfo.getList()) {
      EvalTarget target = new EvalTarget();
      target.setSpaceId(param.getSpaceId());
      target.setSourceTargetId(String.valueOf(flow.getFlowId()));
      target.setEvalTargetType(EvalTargetType.WORKFLOW);

      EvalTargetVersion version = new EvalTargetVersion();
      version.setSpaceId(param.getSpaceId());

      Workflow workflow = new Workflow();
      workflow.setId(String.valueOf(flow.getFlowId()));
      workflow.setName(flow.getFlowName());
      workflow.setDescription(flow.getFlowDesc());
      version.setWorkflow(workflow);

      target.setEvalTargetVersion(version);
      targets.add(target);
    }

    return buildListSourceResult(targets, pageInfo, page);
  }

  @Override
  public void packSourceVersionInfo(Long spaceId, List<EvalTarget> targets) {
    if (CollectionUtils.isEmpty(targets)) {
      return;
    }
    for (EvalTarget target : targets) {
      String sourceTargetId = target.getSourceTargetId();
      SkillFlowDTO skillFlow = null;
      try {
        skillFlow = skillFlowManageService.findSkillFlow(spaceId, Long.parseLong(sourceTargetId));
        target.getEvalTargetVersion().getWorkflow().setName(skillFlow.getFlowName());
      }
      catch (Exception e) {
        logger.warn("工作流{}未找到", sourceTargetId);
      }
    }
  }
}

