package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IPromptRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.CommitInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ExecutePromptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ExecutePromptResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.GetPromptParams;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.LoopPrompt;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.MGetPromptQuery;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.VariableDef;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.BaseInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.Content;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ContentType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetExecuteResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetOutputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunError;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetRunStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetUsage;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetVersion;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldDisplayFormat;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.LoopPromptDO;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.SubmitStatus;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.UserInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.VariableVal;
import com.iwhalecloud.bote.loop.evaluation.domain.service.ISourceEvalTargetOperateService;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Prompt源评估目标操作服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/target_source_loopprompt_impl.go
 * - 功能: Prompt类型的评测对象操作实现
 * - 主要方法:
 * * evalType - 获取评测对象类型
 * * buildBySource - 根据源对象构建评测对象
 * * listSource - 查询源对象列表
 * * listSourceVersion - 查询源对象版本列表
 * * batchGetSource - 批量获取源对象
 * * packSourceInfo - 包装源信息
 * * packSourceVersionInfo - 包装源版本信息
 * * validateInput - 验证输入
 * * execute - 执行评测
 * <p>
 * Java实现说明:
 * - 对应Go的PromptSourceEvalTargetServiceImpl结构体
 * - 使用Spring Service注解
 * - 依赖PromptRPCAdapter进行RPC调用
 * - 处理Prompt相关的评测对象操作
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go strconv.ParseInt -> Java Long.parseLong
 * - Go map[string]*rpc.LoopPrompt -> Java Map<String, LoopPrompt>
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class PromptSourceEvalTargetServiceImpl implements ISourceEvalTargetOperateService {
  private static final Logger logger = LoggerFactory.getLogger(PromptSourceEvalTargetServiceImpl.class);

  private final IPromptRPCAdapter promptRPCAdapter;

  @Override
  public EvalTargetType evalType() {
    return EvalTargetType.LOOP_PROMPT;
  }

  @Override
  public void validateInput(Long spaceId, List<ArgsSchema> inputSchema, EvalTargetInputData input) {
    // 调用输入数据的验证方法
    input.validateInputSchema(inputSchema);
  }

  @Override
  public EvalTargetExecuteResult execute(Long spaceId, ExecuteEvalTargetParam param) {
    long startTime = System.currentTimeMillis();
    EvalTargetOutputData outputData = new EvalTargetOutputData();
    EvalTargetRunStatus runStatus = EvalTargetRunStatus.SUCCESS;

    try {
      ExecutePromptParam exePromptParam = buildExecutePromptParam(param);
      ExecutePromptResult executePromptResult = promptRPCAdapter.executePrompt(spaceId, exePromptParam);
      processExecuteResult(executePromptResult, outputData);
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

  private ExecutePromptParam buildExecutePromptParam(ExecuteEvalTargetParam param) {
    Long promptId = Long.parseLong(param.getSourceTargetId());
    ExecutePromptParam exePromptParam = new ExecutePromptParam();
    exePromptParam.setPromptId(promptId);
    exePromptParam.setPromptVersion(param.getSourceTargetVersion());
    exePromptParam.setHistory(param.getInput().getHistoryMessages());
    exePromptParam.setVariables(buildVariableVals(param.getInput().getInputFields()));
    return exePromptParam;
  }

  private List<VariableVal> buildVariableVals(Map<String, Content> inputFields) {
    List<VariableVal> vals = new ArrayList<>();
    for (Map.Entry<String, Content> entry : inputFields.entrySet()) {
      String key = entry.getKey();
      Content content = entry.getValue();
      if (content != null) {
        VariableVal variable = createVariableVal(key, content);
        vals.add(variable);
      }
    }
    return vals;
  }

  private VariableVal createVariableVal(String key, Content content) {
    VariableVal variable = new VariableVal();
    variable.setKey(key);
    variable.setValue(content.getText());
    variable.setPlaceholderMessages(null);

    if (content.getText() != null) {
      try {
        variable.setPlaceholderMessages(new ArrayList<>());
      }
      catch (Exception e) {
        // 解析失败时保持为null
      }
    }
    return variable;
  }

  private void processExecuteResult(ExecutePromptResult executePromptResult, EvalTargetOutputData outputData) {
    String outputStr = extractOutputString(executePromptResult);
    setOutputFields(outputData, outputStr);
    setTokenUsage(outputData, executePromptResult);
  }

  private String extractOutputString(ExecutePromptResult executePromptResult) {
    if (executePromptResult == null) {
      return "";
    }
    if (executePromptResult.getContent() != null) {
      return executePromptResult.getContent();
    }
    if (executePromptResult.getToolCalls() != null) {
      return executePromptResult.getToolCalls().toString();
    }
    return "";
  }

  private void setOutputFields(EvalTargetOutputData outputData, String outputStr) {
    Map<String, Content> outputFields = new HashMap<>();
    Content outputContent = new Content();
    outputContent.setContentType(ContentType.TEXT);
    outputContent.setFormat(FieldDisplayFormat.MARKDOWN);
    outputContent.setText(outputStr);
    outputFields.put("actual_output", outputContent);
    outputData.setOutputFields(outputFields);
  }

  private void setTokenUsage(EvalTargetOutputData outputData, ExecutePromptResult executePromptResult) {
    if (executePromptResult != null && executePromptResult.getTokenUsage() != null) {
      EvalTargetUsage usage = new EvalTargetUsage();
      usage.setInputTokens(executePromptResult.getTokenUsage().getInputTokens());
      usage.setOutputTokens(executePromptResult.getTokenUsage().getOutputTokens());
      outputData.setEvalTargetUsage(usage);
    }
  }

  private void handleExecutionError(Exception e, EvalTargetOutputData outputData) {
    EvalTargetRunError runError = new EvalTargetRunError();
    runError.setCode(500);
    runError.setMessage(e.getMessage());
    outputData.setEvalTargetRunError(runError);
  }

  private void setExecutionTime(EvalTargetOutputData outputData, long startTime) {
    long timeCostMs = System.currentTimeMillis() - startTime;
    outputData.setTimeConsumingMs(timeCostMs);
  }

  private EvalTargetExecuteResult buildExecuteResult(EvalTargetOutputData outputData, EvalTargetRunStatus runStatus) {
    EvalTargetExecuteResult evalTargetExecuteResult = new EvalTargetExecuteResult();
    evalTargetExecuteResult.setOutputData(outputData);
    evalTargetExecuteResult.setStatus(runStatus);
    return evalTargetExecuteResult;
  }

  @Override
  public EvalTarget buildBySource(Long spaceId, String sourceTargetId, String sourceTargetVersion) {
    Long promptId = Long.parseLong(sourceTargetId);

    GetPromptParams params = new GetPromptParams();
    params.setCommitVersion(sourceTargetVersion);

    LoopPrompt prompt = promptRPCAdapter.getPrompt(spaceId, promptId, params);
    if (prompt == null) {
      throw new BssException("Prompt不存在");
    }

    // 构建输入Schema
    List<ArgsSchema> inputSchema = new ArrayList<>();
    if (prompt.getPromptCommit() != null && prompt.getPromptCommit().getDetail() != null && prompt.getPromptCommit().getDetail().getPromptTemplate() != null) {
      List<VariableDef> variableDefs = prompt.getPromptCommit().getDetail().getPromptTemplate().getVariableDefs();
      if (variableDefs != null) {
        for (VariableDef variableDef : variableDefs) {
          ArgsSchema schema = new ArgsSchema();
          schema.setKey(variableDef.getKey());
          // 目前prompt变量只支持text string类型，后续可以拓展其他类型
          schema.setSupportContentTypes(List.of(ContentType.TEXT));
          schema.setJsonSchema("{\"type\":\"string\"}");
          inputSchema.add(schema);
        }
      }
    }

    String userIDInContext = SessionContext.getCurrentUserId();

    // 构建评测对象
    EvalTarget evalTarget = new EvalTarget();
    evalTarget.setSpaceId(spaceId);
    evalTarget.setSourceTargetId(sourceTargetId);
    evalTarget.setEvalTargetType(EvalTargetType.LOOP_PROMPT);

    // 构建评测对象版本
    EvalTargetVersion version = new EvalTargetVersion();
    version.setSpaceId(spaceId);
    version.setSourceTargetVersion(sourceTargetVersion);
    version.setEvalTargetType(EvalTargetType.LOOP_PROMPT);

    // 构建Prompt信息
    LoopPromptDO loopPrompt = new LoopPromptDO();
    loopPrompt.setPromptId(promptId);
    loopPrompt.setVersion(sourceTargetVersion);
    version.setPrompt(loopPrompt);

    version.setInputSchema(inputSchema);

    // 构建输出Schema
    List<ArgsSchema> outputSchema = new ArrayList<>();
    ArgsSchema outputArgsSchema = new ArgsSchema();
    outputArgsSchema.setKey("output");
    // 目前prompt输出只支持text string类型，后续可以拓展其他类型
    outputArgsSchema.setSupportContentTypes(List.of(ContentType.TEXT, ContentType.MULTI_PART));
    outputArgsSchema.setJsonSchema("{\"type\":\"string\"}");
    outputSchema.add(outputArgsSchema);
    version.setOutputSchema(outputSchema);

    // 设置基础信息
    BaseInfo baseInfo = new BaseInfo();
    UserInfo createdBy = new UserInfo();
    UserInfo updatedBy = new UserInfo();
    createdBy.setUserId(userIDInContext);
    updatedBy.setUserId(userIDInContext);
    baseInfo.setCreatedBy(createdBy);
    baseInfo.setUpdatedBy(updatedBy);
    version.setBaseInfo(baseInfo);

    evalTarget.setEvalTargetVersion(version);
    evalTarget.setBaseInfo(baseInfo);

    return evalTarget;
  }

  @Override
  public ListSourceResult listSource(ListSourceParam param) {
    // Prompt没有滚动分页接口，需要自己适配一下
    int page = buildPageByCursor(param.getCursor());

    // 请求prompt列表
    ListPromptParam listParam = new ListPromptParam();
    listParam.setSpaceId(param.getSpaceId());
    listParam.setPageSize(param.getPageSize());
    listParam.setPage(page);
    listParam.setKeyWord(param.getKeyWord());

    List<LoopPrompt> prompts = promptRPCAdapter.listPrompt(listParam);

    // 结果构建
    List<EvalTarget> targets = new ArrayList<>();
    for (LoopPrompt p : prompts) {
      String name = "";
      String desc = "";
      SubmitStatus status = SubmitStatus.DRAFT;

      if (p.getPromptBasic() != null) {
        name = p.getPromptBasic().getDisplayName();
        desc = p.getPromptBasic().getDescription();
        status = p.getPromptBasic().getLatestVersion() == null ? SubmitStatus.DRAFT : SubmitStatus.SUBMITTED;
      }

      EvalTarget target = new EvalTarget();
      target.setSpaceId(param.getSpaceId());
      target.setSourceTargetId(String.valueOf(p.getId()));
      target.setEvalTargetType(EvalTargetType.LOOP_PROMPT);

      EvalTargetVersion version = new EvalTargetVersion();
      version.setSpaceId(param.getSpaceId());

      LoopPromptDO loopPrompt = new LoopPromptDO();
      loopPrompt.setPromptId(p.getId());
      loopPrompt.setPromptKey(p.getPromptKey());
      loopPrompt.setName(name);
      loopPrompt.setDescription(desc);
      loopPrompt.setSubmitStatus(status);
      version.setPrompt(loopPrompt);

      target.setEvalTargetVersion(version);
      targets.add(target);
    }

    String nextCursor = String.valueOf(page + 1);
    boolean hasMore = prompts.size() == param.getPageSize();
    ListSourceResult listSourceResult = new ListSourceResult();
    listSourceResult.setEvalTargets(targets);
    listSourceResult.setNextCursor(nextCursor);
    listSourceResult.setHasMore(hasMore);
    listSourceResult.setTotal((long) targets.size());
    return listSourceResult;
  }

  @Override
  public ListSourceVersionResult listSourceVersion(ListSourceVersionParam param) {
    Long promptId = Long.parseLong(param.getSourceTargetId());

    GetPromptParams getParams = new GetPromptParams();
    LoopPrompt prompt = promptRPCAdapter.getPrompt(param.getSpaceId(), promptId, getParams);
    if (prompt == null) {
      throw new BssException("Prompt不存在");
    }

    String name = "";
    SubmitStatus status = SubmitStatus.DRAFT;
    if (prompt.getPromptBasic() != null) {
      name = prompt.getPromptBasic().getDisplayName();
      status = prompt.getPromptBasic().getLatestVersion() == null ? SubmitStatus.DRAFT : SubmitStatus.SUBMITTED;
    }

    ListPromptVersionParam versionParam = new ListPromptVersionParam();
    versionParam.setPromptId(promptId);
    versionParam.setSpaceId(param.getSpaceId());
    versionParam.setPageSize(param.getPageSize());
    versionParam.setCursor(param.getCursor());

    ListPromptVersionResult listPromptVersionResult = promptRPCAdapter.listPromptVersion(versionParam);
    List<CommitInfo> versionInfos = listPromptVersionResult.getVersions();

    List<EvalTargetVersion> versions = new ArrayList<>();
    for (CommitInfo versionInfo : versionInfos) {
      EvalTargetVersion version = new EvalTargetVersion();
      version.setSpaceId(param.getSpaceId());
      version.setSourceTargetVersion(versionInfo.getVersion());
      version.setEvalTargetType(EvalTargetType.LOOP_PROMPT);

      LoopPromptDO loopPrompt = new LoopPromptDO();
      loopPrompt.setPromptId(prompt.getId());
      loopPrompt.setVersion(versionInfo.getVersion());
      loopPrompt.setName(name);
      loopPrompt.setPromptKey(prompt.getPromptKey());
      loopPrompt.setSubmitStatus(status);
      loopPrompt.setDescription(versionInfo.getDescription());
      version.setPrompt(loopPrompt);

      versions.add(version);
    }

    String nextCursor = versionInfos.isEmpty() ? "" : String.valueOf(versionInfos.size());
    boolean hasMore = versionInfos.size() == param.getPageSize();
    ListSourceVersionResult listSourceVersionResult = new ListSourceVersionResult();
    listSourceVersionResult.setVersions(versions);
    listSourceVersionResult.setNextCursor(nextCursor);
    listSourceVersionResult.setHasMore(hasMore);
    return listSourceVersionResult;
  }

  @Override
  public void packSourceInfo(Long spaceId, List<EvalTarget> targets) {
    Map<String, LoopPrompt> sourcePromptMap = new HashMap<>();
    List<MGetPromptQuery> promptQueries = new ArrayList<>();

    for (EvalTarget target : targets) {
      if (target.getEvalTargetType() != EvalTargetType.LOOP_PROMPT) {
        continue;
      }

      try {
        Long promptId = Long.parseLong(target.getSourceTargetId());
        MGetPromptQuery query = new MGetPromptQuery();
        query.setPromptId(promptId);
        query.setVersion(null);
        promptQueries.add(query);
      }
      catch (NumberFormatException e) {
        logger.error("解析Prompt ID失败: {}", e.getMessage(), e);
      }
    }

    if (promptQueries.isEmpty()) {
      return;
    }

    List<LoopPrompt> prompts = promptRPCAdapter.mGetPrompt(spaceId, promptQueries);
    for (LoopPrompt prompt : prompts) {
      sourcePromptMap.put(String.valueOf(prompt.getId()), prompt);
    }

    for (EvalTarget target : targets) {
      if (target.getEvalTargetType() != EvalTargetType.LOOP_PROMPT) {
        continue;
      }

      LoopPrompt prompt = sourcePromptMap.get(target.getSourceTargetId());
      if (prompt != null) {
        String name = "";
        if (prompt.getPromptBasic() != null) {
          name = prompt.getPromptBasic().getDisplayName();
        }

        EvalTargetVersion version = new EvalTargetVersion();
        LoopPromptDO loopPrompt = new LoopPromptDO();
        loopPrompt.setName(name);
        version.setPrompt(loopPrompt);
        target.setEvalTargetVersion(version);
      }
    }
  }

  @Override
  public void packSourceVersionInfo(Long spaceId, List<EvalTarget> targets) {
    List<MGetPromptQuery> promptQueries = buildPromptQueries(targets);
    if (promptQueries.isEmpty()) {
      return;
    }

    Map<String, LoopPrompt> sourcePromptMap = buildSourcePromptMap(spaceId, promptQueries);
    updateTargetsWithPromptInfo(targets, sourcePromptMap);
  }

  private List<MGetPromptQuery> buildPromptQueries(List<EvalTarget> targets) {
    List<MGetPromptQuery> promptQueries = new ArrayList<>();
    for (EvalTarget target : targets) {
      if (isValidPromptTarget(target)) {
        MGetPromptQuery query = createPromptQuery(target);
        promptQueries.add(query);
      }
    }
    return promptQueries;
  }

  private boolean isValidPromptTarget(EvalTarget target) {
    return target.getEvalTargetType() == EvalTargetType.LOOP_PROMPT
        && target.getEvalTargetVersion() != null
        && target.getEvalTargetVersion().getPrompt() != null;
  }

  private MGetPromptQuery createPromptQuery(EvalTarget target) {
    MGetPromptQuery query = new MGetPromptQuery();
    query.setPromptId(target.getEvalTargetVersion().getPrompt().getPromptId());
    query.setVersion(target.getEvalTargetVersion().getSourceTargetVersion());
    return query;
  }

  private Map<String, LoopPrompt> buildSourcePromptMap(Long spaceId, List<MGetPromptQuery> promptQueries) {
    List<LoopPrompt> prompts = promptRPCAdapter.mGetPrompt(spaceId, promptQueries);
    Map<String, LoopPrompt> sourcePromptMap = new HashMap<>();

    for (LoopPrompt prompt : prompts) {
      if (prompt.getPromptCommit() != null && prompt.getPromptCommit().getCommitInfo() != null) {
        String key = prompt.getId() + "_" + prompt.getPromptCommit().getCommitInfo().getVersion();
        sourcePromptMap.put(key, prompt);
      }
    }
    return sourcePromptMap;
  }

  private void updateTargetsWithPromptInfo(List<EvalTarget> targets, Map<String, LoopPrompt> sourcePromptMap) {
    for (EvalTarget target : targets) {
      if (isValidPromptTarget(target)) {
        updateTargetWithPromptInfo(target, sourcePromptMap);
      }
    }
  }

  private void updateTargetWithPromptInfo(EvalTarget target, Map<String, LoopPrompt> sourcePromptMap) {
    String key = target.getSourceTargetId() + "_" + target.getEvalTargetVersion().getSourceTargetVersion();
    LoopPrompt prompt = sourcePromptMap.get(key);

    if (prompt != null) {
      updateTargetWithValidPrompt(target, prompt);
    } else {
      markTargetAsDeleted(target);
    }
  }

  private void updateTargetWithValidPrompt(EvalTarget target, LoopPrompt prompt) {
    String name = getPromptName(prompt);
    target.getEvalTargetVersion().getPrompt().setName(name);

    if (prompt.getPromptCommit() != null && prompt.getPromptCommit().getCommitInfo() != null) {
      target.getEvalTargetVersion().getPrompt().setDescription(prompt.getPromptCommit().getCommitInfo().getDescription());
    }
  }

  private String getPromptName(LoopPrompt prompt) {
    if (prompt.getPromptBasic() != null) {
      return prompt.getPromptBasic().getDisplayName();
    }
    return "";
  }

  private void markTargetAsDeleted(EvalTarget target) {
    if (target.getBaseInfo() == null) {
      target.setBaseInfo(new BaseInfo());
    }
    target.getBaseInfo().setDeletedAt(1L);
  }

  @Override
  public List<EvalTarget> batchGetSource(Long spaceId, List<String> ids) {
    List<MGetPromptQuery> promptQueries = new ArrayList<>();

    for (String id : ids) {
      try {
        Long promptId = Long.parseLong(id);
        MGetPromptQuery query = new MGetPromptQuery();
        query.setPromptId(promptId);
        query.setVersion(null);
        promptQueries.add(query);
      }
      catch (NumberFormatException e) {
        logger.error("解析Prompt ID失败: {}", e.getMessage(), e);
      }
    }

    if (promptQueries.isEmpty()) {
      return new ArrayList<>();
    }

    List<LoopPrompt> prompts = promptRPCAdapter.mGetPrompt(spaceId, promptQueries);
    List<EvalTarget> targets = new ArrayList<>();

    for (LoopPrompt prompt : prompts) {
      EvalTarget target = new EvalTarget();
      target.setSpaceId(spaceId);
      target.setSourceTargetId(String.valueOf(prompt.getId()));
      target.setEvalTargetType(EvalTargetType.LOOP_PROMPT);

      EvalTargetVersion version = new EvalTargetVersion();
      version.setSpaceId(spaceId);
      version.setEvalTargetType(EvalTargetType.LOOP_PROMPT);

      LoopPromptDO loopPrompt = new LoopPromptDO();
      loopPrompt.setPromptId(prompt.getId());
      loopPrompt.setName(prompt.getPromptBasic() != null ? prompt.getPromptBasic().getDisplayName() : "");
      loopPrompt.setPromptKey(prompt.getPromptKey());
      loopPrompt.setDescription(prompt.getPromptBasic() != null ? prompt.getPromptBasic().getDescription() : "");
      version.setPrompt(loopPrompt);

      target.setEvalTargetVersion(version);
      targets.add(target);
    }

    return targets;
  }

  /**
   * 根据游标构建页码
   */
  private int buildPageByCursor(String cursor) {
    if (cursor == null || cursor.isEmpty()) {
      return 1;
    }
    try {
      return Integer.parseInt(cursor);
    }
    catch (NumberFormatException e) {
      return 1;
    }
  }
}
