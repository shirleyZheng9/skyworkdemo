package com.iwhalecloud.bote.loop.prompt.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import com.iwhalecloud.bote.entity.loop.prompt.PromptCommitEntity;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.llm.client.dto.message.SystemMessage;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptBasicMapper;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptCommitMapper;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptUserDraftMapper;
import com.iwhalecloud.bote.mapper.skill.PromptManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提示词数据迁移服务
 * 用于将老的提示词数据迁移到新版本
 */
@Service
@RequiredArgsConstructor
public class PromptMigrationService {
  private static final Logger logger = LoggerFactory.getLogger(PromptMigrationService.class);
  private final PromptManageMapper promptManageMapper;
  private final PromptBasicMapper promptBasicMapper;
  private final PromptUserDraftMapper promptUserDraftMapper;
  private final PromptCommitMapper promptCommitMapper;
  private final IIDGenerator idGenerator;

  /**
   * 执行数据迁移
   *
   * @return 迁移结果统计
   */
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public MigrationResult migratePrompts() {
    MigrationResult result = new MigrationResult();
    // 查询所有有效的提示词
    List<PromptDTO> oldPrompts = promptManageMapper.selectAllPromptsForMigration();
    logger.info("查询到 {} 条待迁移的提示词数据", oldPrompts.size());
    Date now = new Date();
    for (PromptDTO oldPrompt : oldPrompts) {
      try {
        Long promptId = oldPrompt.getPromptId();
        Long spaceId = oldPrompt.getTenantId();
        // 检查是否已迁移（通过检查bt_prompt_basic表）
        if (promptBasicMapper.selectById(promptId, spaceId, false) != null) {
          // 检查迁移的数据是否有效
          if (!hasValidData(promptManageMapper.selectPromptContentByPromptIdAndModelId(oldPrompt.getTenantId(), promptId, null))) {
            //有数据但数据无法转JSON,更新
            PromptCommitEntity promptCommitEntity = promptCommitMapper.selectByPromptIdAndVersion(oldPrompt.getPromptId(), "0.0.1");
            if (promptCommitEntity == null) {
              insertPromptCommit(oldPrompt, now);
              continue;
            }
            promptCommitEntity.setMessages(buildMessagesJson(oldPrompt.getPromptContent()));
            promptCommitMapper.updateById(promptCommitEntity);
            continue;
          }
          logger.debug("提示词 {} 已存在，跳过迁移", promptId);
          result.incrementSkipped();
          continue;
        }
        // 1. 插入 bt_prompt_basic
        insertPromptBasic(oldPrompt, now);
        result.incrementBasicSuccess();
        // 2. 插入 bt_prompt_commit
        insertPromptCommit(oldPrompt, now);
        result.incrementCommitSuccess();
        result.incrementTotalSuccess();
        logger.debug("成功迁移提示词: {}", promptId);
        promptManageMapper.deletePrompt(oldPrompt.getTenantId(), promptId, 0L);
      }
      catch (Exception e) {
        logger.error("迁移提示词 {} 失败: {}", oldPrompt.getPromptId(), e.getMessage(), e);
        result.incrementError();
      }
    }
    logger.info("迁移完成: 成功={}, 跳过={}, 失败={}",
      result.getTotalSuccess(), result.getSkipped(), result.getError());
    return result;
  }

  /**
   * 插入 bt_prompt_basic 表
   */
  private void insertPromptBasic(PromptDTO oldPrompt, Date now) {
    String promptKey = "gen_" + oldPrompt.getPromptId();
    String creatorId = oldPrompt.getCreatorId() != null ? oldPrompt.getCreatorId().toString() : "system";
    String updatorId = oldPrompt.getUpdatorId() != null ? oldPrompt.getUpdatorId().toString() : "system";
    PromptBasicEntity basicEntity = PromptBasicEntity.builder()
      .id(oldPrompt.getPromptId())
      .spaceId(oldPrompt.getTenantId())
      .promptKey(promptKey)
      .name(oldPrompt.getPromptTitle() != null ? oldPrompt.getPromptTitle() : "")
      .description(oldPrompt.getPromptTitle() != null ? oldPrompt.getPromptTitle() : "")
      .createdBy(creatorId)
      .updatedBy(updatorId)
      .commitStatus(1)
      .latestVersion("0.0.1")
      .createdAt(oldPrompt.getCreatedTime() != null ? oldPrompt.getCreatedTime() : now)
      .updatedAt(now)
      .deletedAt(0L)
      .catalogItemId(oldPrompt.getCatalogItemId())
      .build();
    promptBasicMapper.insert(basicEntity);
  }

  /**
   * 插入 bt_prompt_commit 表
   */
  private void insertPromptCommit(PromptDTO oldPrompt, Date now) {
    // 检查是否已存在（通过promptId和version）
    if (promptCommitMapper.selectByPromptIdAndVersion(oldPrompt.getPromptId(), "0.0.1") != null) {
      return;
    }
    String promptKey = "gen_" + oldPrompt.getPromptId();
    String creatorId = oldPrompt.getCreatorId() != null ? oldPrompt.getCreatorId().toString() : "system";
    String messagesJson = buildMessagesJson(oldPrompt.getPromptContent());
    PromptCommitEntity commitEntity = PromptCommitEntity.builder()
      .id(idGenerator.genId())
      .spaceId(oldPrompt.getTenantId())
      .promptId(oldPrompt.getPromptId())
      .promptKey(promptKey)
      .templateType("normal")
      .messages(messagesJson)
      .modelConfig(null)
      .variableDefs(null)
      .tools(null)
      .toolCallConfig(null)
      .version("0.0.1")
      .baseVersion("")
      .committedBy(creatorId)
      .description(null)
      .createdAt(oldPrompt.getCreatedTime() != null ? oldPrompt.getCreatedTime() : now)
      .updatedAt(now)
      .build();
    promptCommitMapper.insert(commitEntity);
  }

  /**
   * 构建 messages JSON 字符串
   * 格式: [{"role":"SYSTEM","content":"..."}]
   */
  private String buildMessagesJson(String promptContent) {
    if (promptContent == null) {
      promptContent = "";
    }
    List<Message> messageList = new ArrayList<>();
    messageList.add(new SystemMessage(promptContent));
    return JsonUtil.toJsonString(messageList);
  }

  private boolean hasValidData(String content) {
    try {
      List<Message> messageList = JsonUtil.parseJsonRequired(content, new TypeReference<List<Message>>() {
      });
      return !messageList.isEmpty();
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * 迁移结果统计
   */
  @Getter
  public static class MigrationResult {
    private int totalSuccess = 0;
    private int basicSuccess = 0;
    private int commitSuccess = 0;
    private int skipped = 0;
    private int error = 0;

    public void incrementTotalSuccess() {
      totalSuccess++;
    }

    public void incrementBasicSuccess() {
      basicSuccess++;
    }

    public void incrementCommitSuccess() {
      commitSuccess++;
    }

    public void incrementSkipped() {
      skipped++;
    }

    public void incrementError() {
      error++;
    }
  }
}
