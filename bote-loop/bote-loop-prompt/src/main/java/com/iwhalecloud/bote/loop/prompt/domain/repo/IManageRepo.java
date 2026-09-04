package com.iwhalecloud.bote.loop.prompt.domain.repo;

import com.iwhalecloud.bote.loop.prompt.domain.entity.DraftInfo;
import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.CommitDraftParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitInfoParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptResult;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.UpdatePromptParam;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;

/**
 * Prompt管理仓库接口
 */
public interface IManageRepo {

  /**
   * 创建Prompt
   */
  Long createPrompt(Prompt promptDO);

  /**
   * 删除Prompt
   */
  ResultVO<Void> deletePrompt(Long promptId, Long spaceId);
  /**
   * 获取Prompt
   */
  Prompt getPrompt(GetPromptParam param);

  /**
   * 批量获取Prompt
   */
  Map<GetPromptParam, Prompt> mGetPrompt(List<GetPromptParam> queries, GetPromptOptionFunc... opts);

  /**
   * 根据PromptKey批量获取基础信息
   */
  List<Prompt> mGetPromptBasicByPromptKey(Long spaceId, List<String> promptKeys, GetPromptBasicOptionFunc... opts);

  /**
   * 列表查询Prompt
   */
  ListPromptResult listPrompt(ListPromptParam param);

  /**
   * 更新Prompt
   */
  void updatePrompt(UpdatePromptParam param);

  /**
   * 保存草稿
   */
  DraftInfo saveDraft(Prompt promptDO);

  /**
   * 提交草稿
   */
  void commitDraft(CommitDraftParam param);

  /**
   * 列表查询提交信息
   */
  ListCommitResult listCommitInfo(ListCommitInfoParam param);

  /**
   * 是否存在promptKey
   */
  boolean existsPromptKey(Long workspaceId, String promptKey);
  /**
   * 是否存在promptName
   */
  boolean existsPromptName(Long workspaceId, String promptName);
}
