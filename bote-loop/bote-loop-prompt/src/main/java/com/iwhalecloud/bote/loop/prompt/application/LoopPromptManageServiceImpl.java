package com.iwhalecloud.bote.loop.prompt.application;

import com.google.common.collect.Maps;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.CommitInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DraftInfoDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptBasicDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDetailDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDraftDTO;
import com.iwhalecloud.bote.loop.client.prompt.manage.PromptManageService;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.BatchGetPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.BatchGetPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ClonePromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ClonePromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.CommitDraftRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.CommitDraftResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.CreatePromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.CreatePromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.DeletePromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.DeletePromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.GetPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.GetPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListCommitRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListCommitResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptOrderBy;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.PromptQuery;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.PromptResult;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.RevertDraftFromCommitRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.RevertDraftFromCommitResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.SaveDraftRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.SaveDraftResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.UpdatePromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.UpdatePromptResponse;
import com.iwhalecloud.bote.loop.domain.component.rpc.IUserProvider;
import com.iwhalecloud.bote.loop.domain.component.rpc.dto.RpcUserInfo;
import com.iwhalecloud.bote.loop.infra.session.SessionContext;
import com.iwhalecloud.bote.loop.prompt.application.convertor.ManageConvertor;
import com.iwhalecloud.bote.loop.prompt.application.convertor.UserConvertor;
import com.iwhalecloud.bote.loop.prompt.domain.component.conf.IConfigProvider;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DraftInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptBasic;
import com.iwhalecloud.bote.loop.prompt.domain.entity.PromptDraft;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IManageRepo;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.CommitDraftParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitInfoParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.UpdatePromptParam;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Prompt管理服务实现
 * 迁移对应关系: Go语言PromptManageApplicationImpl
 * - 功能: Prompt管理的应用层服务实现
 * - 主要方法:
 * * createPrompt - 创建Prompt
 * * clonePrompt - 克隆Prompt
 * * deletePrompt - 删除Prompt
 * * getPrompt - 获取Prompt
 * * batchGetPrompt - 批量获取Prompt
 * * listPrompt - 列表查询Prompt
 * * updatePrompt - 更新Prompt
 * * saveDraft - 保存草稿
 * * commitDraft - 提交草稿
 * * listCommit - 列表查询提交信息
 * * revertDraftFromCommit - 从提交恢复草稿
 * <p>
 * Java实现说明:
 * - 对应Go的PromptManageApplicationImpl结构体
 * - 使用Spring Service注解
 * - 集成权限检查、审核、用户信息等RPC服务
 * - 使用SessionContext获取用户信息
 */
@Service
@RequiredArgsConstructor
public class LoopPromptManageServiceImpl implements PromptManageService {
  private final IManageRepo manageRepo;
  private final IConfigProvider configProvider;
  private final IUserProvider userProvider;
  private final IResourceElementService resourceElementService;

  @Override
  public boolean existsPromptKey(Long workspaceId, String promptKey) {
    return manageRepo.existsPromptKey(workspaceId, promptKey);
  }

  @Override
  public boolean existsPromptName(Long workspaceId, String promptName) {
    return manageRepo.existsPromptName(workspaceId, promptName);
  }

  @Override
  public CreatePromptResponse createPrompt(CreatePromptRequest request) {
    CreatePromptResponse response = CreatePromptResponse.builder().build();

    String userId = SessionContext.getCurrentUserId();

    PromptDraftDTO promptDraftDTO = request.getDraftDetail() == null ? null : PromptDraftDTO.builder()
      .draftInfo(DraftInfoDTO.builder()
        .userId(userId)
        .isModified(true)
        .build())
      .detail(request.getDraftDetail())
      .build();
    PromptDTO promptDTO = PromptDTO.builder()
      .workspaceId(request.getWorkspaceId())
      .promptKey(request.getPromptKey())
      .promptBasic(PromptBasicDTO.builder()
        .displayName(request.getPromptName()).description(request.getPromptDescription())
        .createdBy(userId).updatedBy(userId)
        .promptType(request.getPromptType())
        .build())
      .promptDraft(promptDraftDTO)
      .catalogItemId(request.getCatalogItemId())
      .build();
    // 构建Prompt对象
    Prompt promptDO = ManageConvertor.promptDTO2DO(promptDTO);

    // 创建Prompt
    Long promptId = manageRepo.createPrompt(promptDO);
    response.setPromptId(promptId);
    return response;
  }

  @Override
  public ClonePromptResponse clonePrompt(ClonePromptRequest request) {
    ClonePromptResponse response = ClonePromptResponse.builder().build();

    String userId = SessionContext.getCurrentUserId();
    // 获取原始Prompt
    GetPromptParam getPromptParam = GetPromptParam.builder()
      .promptId(request.getPromptId())
      .spaceId(request.getWorkspaceId())
      .withCommit(true).withDraft(false)
      .commitVersion(request.getCommitVersion())
      .build();
    Prompt promptDO = manageRepo.getPrompt(getPromptParam);
    // 克隆Prompt
    Prompt clonedPromptDO = clonePromptDetail(promptDO, request, userId);
    clonedPromptDO.setCatalogItemId(request.getCatalogItemId());
    Long clonedPromptId = manageRepo.createPrompt(clonedPromptDO);
    response.setClonedPromptId(clonedPromptId);
    return response;
  }

  @Override
  public ResultVO<DeletePromptResponse> deletePrompt(DeletePromptRequest request) {
    boolean existsed = resourceElementService.existsRelatedResource(request.getTenantId(), request.getPromptId(), "prompt");
    if (existsed) {
      return ResultVO.fail("提示词已存在关联配置数据，不允许删除");
    }
    DeletePromptResponse response = DeletePromptResponse.builder().build();
    // 删除Prompt
    ResultVO<Void> result = manageRepo.deletePrompt(request.getPromptId(), request.getTenantId());
    if (!result.isSuccess()) {
      return new ResultVO<>(result);
    }
    return ResultVO.success(response);
  }

  @Override
  public GetPromptResponse getPrompt(GetPromptRequest request) {
    GetPromptResponse response = GetPromptResponse.builder().build();

    String userId = SessionContext.getCurrentUserId();
    // 处理默认版本
    String commitVersion = request.getCommitVersion();
    if (request.getWithCommit() != null && request.getWithCommit() && !StringUtils.hasText(commitVersion)) {
      GetPromptParam getPromptParam = GetPromptParam.builder()
        .promptId(request.getPromptId())
        .spaceId(request.getTenantId())
        .withCommit(false)
        .withDraft(false)
        .build();
      Prompt promptDO = manageRepo.getPrompt(getPromptParam);
      commitVersion = promptDO.getPromptBasic().getLatestVersion();
    }
    // 获取Prompt
    GetPromptParam getPromptParam = GetPromptParam.builder()
      .promptId(request.getPromptId())
      .spaceId(request.getTenantId())
      .withCommit(StringUtils.hasText(commitVersion))
      .commitVersion(commitVersion)
      .withDraft(request.getWithDraft() != null ? request.getWithDraft() : false)
      .userId(userId)
      .build();
    Prompt promptDO = manageRepo.getPrompt(getPromptParam);

    // 返回结果
    response.setPrompt(ManageConvertor.promptDO2DTO(promptDO));
    // 返回默认配置
    if (request.getWithDefaultConfig() != null && request.getWithDefaultConfig()) {
      PromptDetailDTO defaultConfig = configProvider.getPromptDefaultConfig(request.getTenantId());
      response.setDefaultConfig(defaultConfig);
    }
    return response;
  }

  @Override
  public BatchGetPromptResponse batchGetPrompt(BatchGetPromptRequest request) {
    BatchGetPromptResponse response = BatchGetPromptResponse.builder().build();
    // 内部接口不鉴权
    Map<GetPromptParam, PromptQuery> paramMap = Maps.newHashMap();
    for (PromptQuery query : request.getQueries()) {
      if (query == null) {
        continue;
      }
      GetPromptParam param = GetPromptParam.builder()
        .spaceId(query.getTenantId())
        .promptId(query.getPromptId())
        .withCommit(query.getWithCommit())
        .commitVersion(query.getCommitVersion())
        .withDraft(false)
        .build();
      paramMap.put(param, query);
    }
    Map<GetPromptParam, Prompt> promptMap = manageRepo.mGetPrompt(paramMap.keySet().stream().toList());
    List<PromptResult> results = promptMap.entrySet().stream()
      .map(entry -> {
        GetPromptParam query = entry.getKey();
        Prompt promptDO = entry.getValue();
        return PromptResult.builder()
          .query(paramMap.get(query))
          .prompt(ManageConvertor.promptDO2DTO(promptDO))
          .build();
      })
      .collect(Collectors.toList());
    response.setResults(results);
    return response;
  }

  @Override
  public ListPromptResponse listPrompt(ListPromptRequest request) {
    ListPromptResponse response = new ListPromptResponse();
    ListPromptParam listPromptParam = getListPromptParam(request);
    ListPromptResult listPromptResult = manageRepo.listPrompt(listPromptParam);
    if (listPromptResult == null) {
      return response;
    }
    response.setTotal(listPromptResult.getTotal());
    response.setPrompts(ManageConvertor.batchPromptDO2DTO(listPromptResult.getPromptDOs()));
    // 获取用户信息
    Set<String> userIdSet = response.getPrompts().stream()
      .filter(prompt -> {
        return prompt != null && prompt.getPromptBasic() != null &&
          StringUtils.hasText(prompt.getPromptBasic().getCreatedBy());
      })
      .map(prompt -> prompt.getPromptBasic().getCreatedBy())
      .collect(Collectors.toSet());
    if (!userIdSet.isEmpty()) {
      List<RpcUserInfo> userDOs = userProvider.mGetUserInfo(userIdSet.stream().toList());
      response.setUsers(UserConvertor.batchUserInfoDO2DTO(userDOs));
    }
    return response;
  }

  @Override
  public UpdatePromptResponse updatePrompt(UpdatePromptRequest request) {
    UpdatePromptResponse response = UpdatePromptResponse.builder().build();
    String userId = SessionContext.getCurrentUserId();
    UpdatePromptParam updatePromptParam = UpdatePromptParam.builder()
      .promptId(request.getPromptId())
      .updatedBy(userId)
      .promptName(request.getPromptName())
      .promptDescription(request.getPromptDescription())
      .catalogItemId(request.getCatalogItemId())
      .spaceId(request.getTenantId())
      .promptType(request.getPromptType() != null ? request.getPromptType().getValue() : null)
      .build();
    manageRepo.updatePrompt(updatePromptParam);
    return response;
  }

  @Override
  public ListCommitResponse listCommit(ListCommitRequest request) {
    ListCommitResponse response = ListCommitResponse.builder().promptCommitInfos(new ArrayList<>()).build();

    // 校验分页token
    Long pageToken = null;
    if (StringUtils.hasText(request.getPageToken())) {
      try {
        pageToken = Long.parseLong(request.getPageToken());
      }
      catch (NumberFormatException e) {
        throw new BssException("Page token is invalid, page token = " + request.getPageToken(), e);
      }
    }
    // 列表查询提交信息
    ListCommitInfoParam listCommitParam = ListCommitInfoParam.builder()
      .promptId(request.getPromptId())
      .pageSize(request.getPageSize())
      .pageToken(pageToken)
      .asc(request.getAsc())
      .build();
    ListCommitResult listCommitResult = manageRepo.listCommitInfo(listCommitParam);
    if (listCommitResult == null) {
      return response;
    }
    if (listCommitResult.getNextPageToken() != null && listCommitResult.getNextPageToken() > 0) {
      response.setNextPageToken(String.valueOf(listCommitResult.getNextPageToken()));
      response.setHasMore(true);
    }
    response.setPromptCommitInfos(ManageConvertor.batchCommitInfoDO2DTO(listCommitResult.getCommitInfoDOs()));
    // 获取用户信息
    Set<String> userIdSet = response.getPromptCommitInfos().stream()
      .filter(commitInfo -> commitInfo != null && StringUtils.hasText(commitInfo.getCommittedBy()))
      .map(CommitInfoDTO::getCommittedBy)
      .collect(Collectors.toSet());
    if (!userIdSet.isEmpty()) {
      List<RpcUserInfo> userDOs = userProvider.mGetUserInfo(userIdSet.stream().toList());
      response.setUsers(UserConvertor.batchUserInfoDO2DTO(userDOs));
    }
    return response;
  }

  @Override
  public SaveDraftResponse saveDraft(SaveDraftRequest request) {
    SaveDraftResponse response = SaveDraftResponse.builder().build();

    String userId = SessionContext.getCurrentUserId();
    // 校验
    if (request.getPromptDraft() == null || request.getPromptDraft().getDraftInfo() == null ||
      request.getPromptDraft().getDetail() == null) {
      throw new BssException("Draft is not specified");
    }
    PromptDTO promptDTO = PromptDTO.builder().id(request.getPromptId()).promptDraft(request.getPromptDraft()).build();
    promptDTO.getPromptDraft().getDraftInfo().setUserId(userId);
    promptDTO.setWorkspaceId(request.getTenantId());
    // 准备保存数据
    Prompt savingPromptDO = ManageConvertor.promptDTO2DO(promptDTO);

    // 保存草稿
    DraftInfo draftInfoDO = manageRepo.saveDraft(savingPromptDO);
    response.setDraftInfo(ManageConvertor.draftInfoDO2DTO(draftInfoDO));
    return response;
  }

  @Override
  public CommitDraftResponse commitDraft(CommitDraftRequest request) {
    CommitDraftResponse response = CommitDraftResponse.builder().build();
    String userId = SessionContext.getCurrentUserId();
    // 提交草稿
    CommitDraftParam commitDraftParam = CommitDraftParam.builder()
      .promptId(request.getPromptId())
      .userId(userId)
      .commitVersion(request.getCommitVersion())
      .commitDescription(request.getCommitDescription())
      .spaceId(request.getTenantId())
      .build();
    manageRepo.commitDraft(commitDraftParam);
    return response;
  }

  private @NotNull ListPromptParam getListPromptParam(ListPromptRequest request) {
    String userId = SessionContext.getCurrentUserId();
    ListPromptParam listPromptParam = new ListPromptParam();
    listPromptParam.setSpaceId(request.getTenantId());
    listPromptParam.setKeyWord(request.getKeyWord());
    listPromptParam.setCreatedBys(request.getCreatedBys());
    listPromptParam.setUserId(userId);
    listPromptParam.setCommittedOnly(request.getCommittedOnly());
    listPromptParam.setPageNum(request.getPageNum());
    listPromptParam.setPageSize(request.getPageSize());
    listPromptParam.setOrderBy(listPromptOrderBy(request.getOrderBy()));
    listPromptParam.setAsc(request.getAsc());
    listPromptParam.setCatalogItemId(request.getCatalogItemId());
    // 转换 promptType: PromptType 枚举 -> String
    if (request.getPromptType() != null) {
      listPromptParam.setPromptType(request.getPromptType().getValue());
    }
    return listPromptParam;
  }

  @Override
  public RevertDraftFromCommitResponse revertDraftFromCommit(RevertDraftFromCommitRequest request) {
    RevertDraftFromCommitResponse response = RevertDraftFromCommitResponse.builder().build();
    // 获取用户信息
    String userId = SessionContext.getCurrentUserId();
    // 获取Prompt
    GetPromptParam getPromptParam = GetPromptParam.builder()
      .promptId(request.getPromptId())
      .withCommit(true).withDraft(false)
      .commitVersion(request.getCommitVersionRevertingFrom())
      .spaceId(request.getTenantId())
      .build();
    Prompt promptDO = manageRepo.getPrompt(getPromptParam);
    if (promptDO == null || promptDO.getPromptCommit() == null) {
      throw new BssException("Prompt or commit not found, prompt id = " + request.getPromptId() +
        ", commit version = " + request.getCommitVersionRevertingFrom());
    }

    // 保存草稿
    promptDO.setPromptDraft(new PromptDraft());
    promptDO.getPromptDraft().setDraftInfo(new DraftInfo());
    promptDO.getPromptDraft().getDraftInfo().setUserId(userId);
    promptDO.getPromptDraft().getDraftInfo().setBaseVersion(promptDO.getPromptCommit().getCommitInfo().getVersion());
    promptDO.getPromptDraft().setPromptDetail(promptDO.getPromptCommit().getPromptDetail());
    promptDO.setSpaceId(request.getTenantId());
    manageRepo.saveDraft(promptDO);
    return response;
  }

  /**
   * 克隆Prompt详情
   */
  private Prompt clonePromptDetail(Prompt promptDO, ClonePromptRequest request, String userId) {
    promptDO.setPromptKey(request.getClonedPromptKey());
    // 设置基础信息
    PromptBasic promptBasic = new PromptBasic();
    promptBasic.setDisplayName(request.getClonedPromptName());
    promptBasic.setDescription(request.getClonedPromptDescription());
    promptBasic.setCreatedBy(userId);
    promptBasic.setUpdatedBy(userId);
    // 克隆 promptType：优先使用请求中的 promptType，否则继承原 Prompt 的 promptType
    if (request.getPromptType() != null) {
      promptBasic.setPromptType(request.getPromptType());
    }
    else if (promptDO.getPromptBasic() != null && promptDO.getPromptBasic().getPromptType() != null) {
      promptBasic.setPromptType(promptDO.getPromptBasic().getPromptType());
    }
    promptDO.setPromptBasic(promptBasic);
    // 设置草稿信息
    PromptDraft promptDraft = new PromptDraft();
    DraftInfo draftInfo = new DraftInfo();
    draftInfo.setUserId(userId);
    draftInfo.setIsModified(true);
    promptDraft.setDraftInfo(draftInfo);
    promptDraft.setPromptDetail(promptDO.getPromptCommit().getPromptDetail());
    promptDO.setPromptDraft(promptDraft);
    // 清空提交信息
    promptDO.setPromptCommit(null);
    return promptDO;
  }

  /**
   * 列表查询排序字段映射
   */
  private int listPromptOrderBy(ListPromptOrderBy orderBy) {
    if (orderBy == null) {
      return 1; // ListPromptBasicOrderByID
    }
    return switch (orderBy) {
      case CREATED_AT -> 2; // ListPromptBasicOrderByCreatedAt
      case COMMITTED_AT -> 3; // ListPromptBasicOrderByLatestCommittedAt
      case UPDATED_AT -> 4; // ListPromptBasicOrderByLatestCommittedAt
      default -> 1; // ListPromptBasicOrderByID
    };
  }
}
