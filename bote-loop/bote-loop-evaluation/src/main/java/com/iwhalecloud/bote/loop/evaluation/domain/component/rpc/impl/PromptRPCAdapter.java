package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.impl;

import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CommitInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.ScenarioDTO;
import com.iwhalecloud.bote.loop.client.prompt.execute.PromptExecuteService;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalRequest;
import com.iwhalecloud.bote.loop.client.prompt.execute.dto.ExecuteInternalResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.PromptManageService;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.BatchGetPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.BatchGetPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.GetPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.GetPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListCommitRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListCommitResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.PromptQuery;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.PromptResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IPromptRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.convertor.PromptConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.CommitInfo;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ExecutePromptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ExecutePromptResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.GetPromptParams;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.ListPromptVersionResult;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.LoopPrompt;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto.MGetPromptQuery;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.TokenUsage;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Prompt RPC适配器实现类
 * - 功能: Prompt相关的RPC调用实现
 * - 主要方法:
 * * getPrompt - 获取Prompt
 * * mGetPrompt - 批量获取Prompt
 * * listPrompt - 分页查询Prompt列表
 * * listPromptVersion - 分页查询Prompt版本列表
 * * executePrompt - 执行Prompt
 */
@Component
@RequiredArgsConstructor
public class PromptRPCAdapter implements IPromptRPCAdapter {
  private final PromptManageService promptManageService;
  private final PromptExecuteService promptExecuteService;

  @Override
  public LoopPrompt getPrompt(Long spaceId, Long promptId, GetPromptParams params) {
    GetPromptRequest req = new GetPromptRequest();
    req.setPromptId(promptId);
    req.setTenantId(spaceId);
    String commitVersion = params.getCommitVersion();
    if (StringUtils.isNotEmpty(commitVersion)) {
      req.setCommitVersion(commitVersion);
      req.setWithCommit(true);
    }

    GetPromptResponse resp = promptManageService.getPrompt(req);
    if (resp == null) {
      throw new BssException("RPC调用失败");
    }
    if (resp.getBaseResp() != null && resp.getBaseResp().getStatusCode() != 0) {
      throw new BssException("获取Prompt失败: " + resp.getBaseResp().getStatusMessage());
    }
    if (resp.getPrompt() == null) {
      return null;
    }

    return PromptConvertor.convertToLoopPrompt(resp.getPrompt());
  }

  @Override
  public List<LoopPrompt> mGetPrompt(Long spaceId, List<MGetPromptQuery> promptQueries) {
    List<PromptQuery> queries = Lists.newArrayList();
    for (MGetPromptQuery query : promptQueries) {
      PromptQuery promptQuery = new PromptQuery();
      promptQuery.setPromptId(query.getPromptId());
      promptQuery.setTenantId(spaceId);
      String version = query.getVersion();
      if (StringUtils.isNotEmpty(version)) {
        promptQuery.setWithCommit(true);
        promptQuery.setCommitVersion(version);
      }
      queries.add(promptQuery);
    }

    BatchGetPromptRequest req = new BatchGetPromptRequest();
    req.setQueries(queries);

    BatchGetPromptResponse resp = promptManageService.batchGetPrompt(req);
    if (resp == null) {
      throw new BssException("RPC调用失败");
    }
    if (resp.getBaseResp() != null && resp.getBaseResp().getStatusCode() != 0) {
      throw new BssException("批量获取Prompt失败: " + resp.getBaseResp().getStatusMessage());
    }

    List<PromptDTO> promptDTOs = Lists.newArrayList();
    for (PromptResult result : resp.getResults()) {
      PromptDTO prompt = result.getPrompt();
      if (prompt == null) {
        continue;
      }
      promptDTOs.add(prompt);
    }

    return PromptConvertor.convertToLoopPrompts(promptDTOs);
  }

  @Override
  public List<LoopPrompt> listPrompt(ListPromptParam param) {
    ListPromptRequest req = new ListPromptRequest();
    req.setTenantId(param.getSpaceId());
    req.setPageNum(param.getPage());
    req.setPageSize(param.getPageSize());
    req.setKeyWord(param.getKeyWord());

    ListPromptResponse resp = promptManageService.listPrompt(req);
    if (resp == null) {
      throw new BssException("RPC调用失败");
    }
    if (resp.getBaseResp() != null && resp.getBaseResp().getStatusCode() != 0) {
      throw new BssException("查询Prompt列表失败: " + resp.getBaseResp().getStatusMessage());
    }

    return PromptConvertor.convertToLoopPrompts(resp.getPrompts());
  }

  @Override
  public ListPromptVersionResult listPromptVersion(ListPromptVersionParam param) {
    ListCommitRequest req = new ListCommitRequest();
    req.setPromptId(param.getPromptId());
    req.setPageToken(param.getCursor());
    req.setPageSize(param.getPageSize());
    ListCommitResponse resp = promptManageService.listCommit(req);
    List<CommitInfo> commitInfos = Lists.newArrayList();
    for (CommitInfoDTO commitInfo : resp.getPromptCommitInfos()) {
      CommitInfo info = new CommitInfo();
      info.setVersion(commitInfo.getVersion());
      info.setBaseVersion(commitInfo.getBaseVersion());
      info.setDescription(commitInfo.getDescription());
      info.setCommittedAt(commitInfo.getCommittedAt());
      info.setCommittedBy(commitInfo.getCommittedBy());
      commitInfos.add(info);
    }
    String nextCursor = resp.getNextPageToken() != null ? resp.getNextPageToken() : "";
    boolean hasMore = StringUtils.isNotEmpty(nextCursor);
    return new ListPromptVersionResult(commitInfos, nextCursor, hasMore);
  }

  @Override
  public ExecutePromptResult executePrompt(Long spaceId, ExecutePromptParam param) {
    ExecuteInternalRequest req = new ExecuteInternalRequest();
    req.setPromptId(param.getPromptId());
    req.setWorkspaceId(spaceId);
    req.setVersion(param.getPromptVersion());
    req.setScenario(ScenarioDTO.EVAL_TARGET); // 对应Go中的prompt.ScenarioEvalTarget

    // 转换变量
    req.setVariableVals(PromptConvertor.convertVariables2Prompt(param.getVariables()));
    // 转换消息
    req.setMessages(PromptConvertor.convertMessages2Prompt(param.getHistory()));

    ExecuteInternalResponse resp = promptExecuteService.executeInternal(req);

    ExecutePromptResult result = new ExecutePromptResult();
    result.setContent(resp.getMessage().getContent());
    result.setToolCalls(PromptConvertor.convertPromptToolCalls2Eval(resp.getMessage().getToolCalls()));

    // 设置Token使用情况
    if (resp.getUsage() != null) {
      TokenUsage tokenUsage = new TokenUsage();
      tokenUsage.setInputTokens(resp.getUsage().getInputTokens());
      tokenUsage.setOutputTokens(resp.getUsage().getOutputTokens());
      result.setTokenUsage(tokenUsage);
    }

    return result;
  }
}
