package com.iwhalecloud.bote.loop.prompt.application;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.loop.client.prompt.openapi.PromptOpenAPIService;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.BatchGetPromptByPromptKeyRequest;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.BatchGetPromptByPromptKeyResponse;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.PromptQuery;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.PromptResult;
import com.iwhalecloud.bote.loop.client.prompt.openapi.dto.PromptResultData;
import com.iwhalecloud.bote.loop.prompt.application.convertor.OpenApiConvertor;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IManageRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptKeyVersionPair;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptOptions;
import com.iwhalecloud.bote.loop.prompt.domain.service.IPromptService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Prompt OpenAPI服务实现
 * 对应Thrift: PromptOpenAPIService实现
 */
@Service
@RequiredArgsConstructor
public class PromptOpenAPIServiceImpl implements PromptOpenAPIService {
  private final IPromptService promptService;
  private final IManageRepo manageRepo;

  @Override
  public BatchGetPromptByPromptKeyResponse batchGetPromptByPromptKey(
    BatchGetPromptByPromptKeyRequest request) {

    if (request.getWorkspaceId() == null) {
      throw new BssException("invalid_param workspace_id参数为空");
    }

    // TODO 限流检查

    // 查询prompt id并鉴权
    List<String> promptKeys = new ArrayList<>();
    for (PromptQuery promptQuery : request.getQueries()) {
      String promptKey = promptQuery.getPromptKey();
      promptKeys.add(promptKey);
    }
    Map<String, Long> promptKeyIDMap = promptService.mGetPromptIDs(request.getWorkspaceId(), promptKeys);

    // TODO 执行权限检查

    return fetchPromptResults(request, promptKeyIDMap);
  }

  private BatchGetPromptByPromptKeyResponse fetchPromptResults(BatchGetPromptByPromptKeyRequest request, Map<String, Long> promptKeyIDMap) {
    // 准备查询参数
    List<PromptKeyVersionPair> pairs = request.getQueries().stream()
      .map(o -> {
        return PromptKeyVersionPair.builder()
          .promptKey(o.getPromptKey())
          .version(o.getVersion())
          .build();
      }).toList();

    // 解析具体的提交版本
    Map<PromptKeyVersionPair, String> promptKeyCommitVersionMap = promptService.mParseCommitVersionByPromptKey(request.getWorkspaceId(), pairs);

    // 获取prompt详细信息
    List<GetPromptParam> mGetParams = request.getQueries().stream()
      .map(o -> {
        return GetPromptParam.builder()
          .promptId(promptKeyIDMap.get(o.getPromptKey()))
          .withCommit(true).withDraft(false)
          .commitVersion(promptKeyCommitVersionMap.get(PromptKeyVersionPair.builder()
            .promptKey(o.getPromptKey())
            .version(o.getVersion())
            .build()))
          .build();
      }).toList();
    Map<GetPromptParam, Prompt> prompts = manageRepo.mGetPrompt(mGetParams, PromptOptions.withPromptCacheEnable());

    // 构建版本映射
    Map<PromptKeyVersionPair, Prompt> promptMap = Maps.newHashMap();
    for (Map.Entry<GetPromptParam, Prompt> entry : prompts.entrySet()) {
      Prompt prompt = entry.getValue();

      promptMap.put(PromptKeyVersionPair.builder()
        .promptKey(prompt.getPromptKey())
        .version(prompt.getVersion())
        .build(), prompt);
    }

    // 构建响应
    List<PromptResult> items = request.getQueries().stream().map(o -> {
      PromptKeyVersionPair promptKeyVersionPair = PromptKeyVersionPair.builder()
        .promptKey(o.getPromptKey())
        .version(o.getVersion())
        .build();
      // 找到具体的版本
      String commitVersion = promptKeyCommitVersionMap.get(promptKeyVersionPair);

      PromptDTO promptDTO = OpenApiConvertor.openApiPromptDO2DTO(promptMap.get(PromptKeyVersionPair.builder()
        .promptKey(o.getPromptKey())
        .version(commitVersion)
        .build()));

      return PromptResult.builder().query(o).prompt(promptDTO).build();
    }).toList();

    PromptResultData data = PromptResultData.builder()
      .items(items)
      .build();

    return BatchGetPromptByPromptKeyResponse.builder()
      .data(data)
      .build();
  }

}
