package com.iwhalecloud.bote.loop.api.controller.prompt;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
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
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.ListPromptResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.RevertDraftFromCommitRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.RevertDraftFromCommitResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.SaveDraftRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.SaveDraftResponse;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.UpdatePromptRequest;
import com.iwhalecloud.bote.loop.client.prompt.manage.dto.UpdatePromptResponse;
import com.iwhalecloud.bote.loop.prompt.application.PromptMigrationService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

/**
 * Prompt管理控制器
 * 对应Thrift: PromptManageService
 */
@RestController
@RequestMapping(BaseConsts.API_PREFIX + "api/prompt/v1/prompts")
@RequiredArgsConstructor
@Tag(name = "提示词：基础管理")
public class LoopPromptManageController {
  private final PromptManageService promptManageService;
  private final PromptMigrationService promptMigrationService;

  /**
   * 创建Prompt
   * 对应Thrift: POST /api/prompt/v1/prompts
   */
  @PostMapping
  @Operation(summary = "创建提示词")
  public ResultVO<CreatePromptResponse> createPrompt(@Valid @RequestBody CreatePromptRequest request) {
    if (promptManageService.existsPromptKey(request.getWorkspaceId(), request.getPromptKey())) {
      return ResultVO.fail("重复的PromptKey");
    }
    if (promptManageService.existsPromptName(request.getWorkspaceId(), request.getPromptName())) {
      return ResultVO.fail("重复的Prompt名称");
    }
    return ResultVO.success(promptManageService.createPrompt(request));
  }

  /**
   * 克隆Prompt
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/clone
   */
  @PostMapping("/{promptId}/clone")
  public ResultVO<ClonePromptResponse> clonePrompt(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody ClonePromptRequest request) {
    if (promptManageService.existsPromptKey(request.getWorkspaceId(), request.getClonedPromptKey())) {
      return ResultVO.fail("重复的PromptKey");
    }
    if (promptManageService.existsPromptName(request.getWorkspaceId(), request.getClonedPromptName())) {
      return ResultVO.fail("重复的Prompt名称");
    }
    request.setPromptId(promptId);
    return ResultVO.success(promptManageService.clonePrompt(request));
  }

  /**
   * 删除Prompt
   * 对应Thrift: DELETE /api/prompt/v1/prompts/:prompt_id
   */
  @DeleteMapping("/{promptId}")
  public ResultVO<DeletePromptResponse> deletePrompt(@PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
                                                     @RequestParam(name = "tenantId") @Schema(description = "租户id") Long tenantId) {
    DeletePromptRequest request = DeletePromptRequest.builder().promptId(promptId).tenantId(tenantId).build();
    return promptManageService.deletePrompt(request);
  }

  /**
   * 获取Prompt
   * 对应Thrift: GET /api/prompt/v1/prompts/:prompt_id
   */
  @GetMapping("/{promptId}")
  public ResultVO<GetPromptResponse> getPrompt(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestParam(name = "tenantId") @Schema(description = "租户id") Long tenantId,
    @RequestParam(required = false, name = "withCommit") @Schema(description = "是否包含提交信息") Boolean withCommit,
    @RequestParam(required = false, name = "commitVersion") @Schema(description = "提交版本") String commitVersion,
    @RequestParam(required = false, name = "withDraft") @Schema(description = "是否包含草稿") Boolean withDraft,
    @RequestParam(required = false, name = "withDefaultConfig") @Schema(description = "是否包含默认配置") Boolean withDefaultConfig) {

    GetPromptRequest request = GetPromptRequest.builder()
      .promptId(promptId)
      .withCommit(withCommit)
      .commitVersion(commitVersion)
      .withDraft(withDraft)
      .withDefaultConfig(withDefaultConfig)
      .tenantId(tenantId)
      .build();

    return ResultVO.success(promptManageService.getPrompt(request));
  }

  /**
   * 批量获取Prompt
   * 对应Thrift: BatchGetPrompt (内部接口)
   */
  @PostMapping("/batch")
  public ResultVO<BatchGetPromptResponse> batchGetPrompt(@RequestBody BatchGetPromptRequest request) {
    return ResultVO.success(promptManageService.batchGetPrompt(request));
  }

  /**
   * 列表Prompt
   * 对应Thrift: POST /api/prompt/v1/prompts/list
   */
  @PostMapping("/list")
  @Operation(summary = "查询提示词列表")
  public ResultVO<ListPromptResponse> listPrompt(@RequestBody ListPromptRequest request) {
    Assert.notNull(request.getTenantId(), "租户id不能为空");
    Assert.notNull(request.getPageNum(), "页码不能为空");
    Assert.notNull(request.getPageSize(), "页大小不能为空");
    return ResultVO.success(promptManageService.listPrompt(request));
  }

  /**
   * 更新Prompt
   * 对应Thrift: PUT /api/prompt/v1/prompts/:prompt_id
   */
  @PutMapping("/{promptId}")
  public ResultVO<UpdatePromptResponse> updatePrompt(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody UpdatePromptRequest request) {
    request.setPromptId(promptId);
    return ResultVO.success(promptManageService.updatePrompt(request));
  }

  /**
   * 保存草稿
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/drafts/save
   */
  @PostMapping("/{promptId}/drafts/save")
  public ResultVO<SaveDraftResponse> saveDraft(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody SaveDraftRequest request) {
    request.setPromptId(promptId);
    return ResultVO.success(promptManageService.saveDraft(request));
  }

  /**
   * 列表提交
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/commits/list
   */
  @PostMapping("/{promptId}/commits/list")
  public ResultVO<ListCommitResponse> listCommit(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody ListCommitRequest request) {
    request.setPromptId(promptId);
    return ResultVO.success(promptManageService.listCommit(request));
  }

  /**
   * 提交草稿
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/drafts/commit
   */
  @PostMapping("/{promptId}/drafts/commit")
  public ResultVO<CommitDraftResponse> commitDraft(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody CommitDraftRequest request) {
    request.setPromptId(promptId);
    return ResultVO.success(promptManageService.commitDraft(request));
  }

  /**
   * 从提交恢复草稿
   * 对应Thrift: POST /api/prompt/v1/prompts/:prompt_id/drafts/revert_from_commit
   */
  @PostMapping("/{promptId}/drafts/revert_from_commit")
  public ResultVO<RevertDraftFromCommitResponse> revertDraftFromCommit(
    @PathVariable("promptId") @Schema(description = "提示词ID") Long promptId,
    @RequestBody RevertDraftFromCommitRequest request) {
    request.setPromptId(promptId);
    return ResultVO.success(promptManageService.revertDraftFromCommit(request));
  }

  /**
   * 迁移老的提示词数据到新版本
   * 将 bt_prompt 表的数据迁移到 bt_prompt_basic、bt_prompt_user_draft、bt_prompt_commit 表
   */
  @PostMapping("/migrate")
  @Operation(summary = "迁移提示词数据")
  @IgnoreSession
  @IgnoreSign
  public ResultVO<PromptMigrationService.MigrationResult> migratePrompts() {
    PromptMigrationService.MigrationResult result = promptMigrationService.migratePrompts();
    return ResultVO.success(result);
  }
}
