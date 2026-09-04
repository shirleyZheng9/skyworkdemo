package com.iwhalecloud.bote.loop.client.prompt.manage;

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
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.RevertDraftFromCommitRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.RevertDraftFromCommitResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.SaveDraftRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.SaveDraftResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.UpdatePromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.UpdatePromptResponse;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * Prompt管理服务接口
 * 对应Thrift: PromptManageService
 */
public interface PromptManageService {
  /**
   * 是否存在promptKey
   */
  boolean existsPromptKey(Long workspaceId, String promptKey);

  /**
   * 是否存在promptName
   */
  boolean existsPromptName(Long workspaceId, String promptName);
  // --------------- Prompt管理 --------------- //

  /**
   * 创建Prompt
   * 对应Thrift方法: CreatePrompt
   */
  CreatePromptResponse createPrompt(CreatePromptRequest request);

  /**
   * 克隆Prompt
   * 对应Thrift方法: ClonePrompt
   */
  ClonePromptResponse clonePrompt(ClonePromptRequest request);

  /**
   * 删除Prompt
   * 对应Thrift方法: DeletePrompt
   */
  ResultVO<DeletePromptResponse> deletePrompt(DeletePromptRequest request);

  /**
   * 获取Prompt
   * 对应Thrift方法: GetPrompt
   */
  GetPromptResponse getPrompt(GetPromptRequest request);

  /**
   * 批量获取Prompt
   * 对应Thrift方法: BatchGetPrompt
   */
  BatchGetPromptResponse batchGetPrompt(BatchGetPromptRequest request);

  /**
   * 列表Prompt
   * 对应Thrift方法: ListPrompt
   */
  ListPromptResponse listPrompt(ListPromptRequest request);

  /**
   * 更新Prompt
   * 对应Thrift方法: UpdatePrompt
   */
  UpdatePromptResponse updatePrompt(UpdatePromptRequest request);

  /**
   * 保存草稿
   * 对应Thrift方法: SaveDraft
   */
  SaveDraftResponse saveDraft(SaveDraftRequest request);

  // --------------- Prompt版本管理 --------------- //

  /**
   * 列表提交
   * 对应Thrift方法: ListCommit
   */
  ListCommitResponse listCommit(ListCommitRequest request);

  /**
   * 提交草稿
   * 对应Thrift方法: CommitDraft
   */
  CommitDraftResponse commitDraft(CommitDraftRequest request);

  /**
   * 从提交恢复草稿
   * 对应Thrift方法: RevertDraftFromCommit
   */
  RevertDraftFromCommitResponse revertDraftFromCommit(RevertDraftFromCommitRequest request);
}
