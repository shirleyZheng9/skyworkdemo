package com.iwhalecloud.bote.agent.memory.service;

import com.iwhalecloud.bote.agent.memory.helper.MemoryCompactor;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.MemoryMessage;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.mapper.agent.AiWorkspaceManageMapper;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 每日记忆更新服务
 *
 * <p>当短期记忆压缩（{@link MemoryCompactor#autoCompact}）触发时，
 * 异步将被压缩的对话内容总结为每日记忆条目，追加到 {@code MEMORY-YYYY-MM-DD.md} 文件中。</p>
 *
 * <p>写入完成后，若 Elasticsearch 可用，同时异步更新该每日记忆文件的向量索引。</p>
 *
 * @author wangtingyun
 * @since 2026-04-18
 */
@Service
@RequiredArgsConstructor
public class MemoryDailyUpdater {
  private static final Logger logger = LoggerFactory.getLogger(MemoryDailyUpdater.class);

  private final AiWorkspaceManageMapper workspaceManageMapper;
  private final LongTermMemoryVectorService vectorService;

  /**
   * 异步更新每日记忆文件
   *
   * <p>从压缩的消息列表中生成摘要，追加到当天的 {@code MEMORY-YYYY-MM-DD.md} 文件。
   * 整个过程完全异步，不阻塞当前对话流程。</p>
   *
   * @param modelClient       大模型客户端（用于生成摘要）
   * @param compressedMessages 被压缩的 MemoryMessage 列表
   * @param spaceId           用户所在空间 ID
   * @param botId             应用 ID
   * @param userId            用户 ID
   * @param tenantId          租户 ID（用于向量化嵌入模型查找）
   */
  public void asyncUpdateDailyMemory(LlmClient modelClient, List<MemoryMessage> compressedMessages,
      Long spaceId, Long botId, Long userId, Long tenantId) {
    if (compressedMessages.isEmpty()) {
      return;
    }
    // 提取纯消息对象（去掉 MemoryMessage 包装）
    List<Message> messages = compressedMessages.stream().map(MemoryMessage::message).toList();

    ThreadPools.getCommon().submit(() -> {
      try {
        doUpdateDailyMemory(modelClient, messages, spaceId, botId, userId, tenantId);
      }
      catch (Exception e) {
        logger.error("异步更新每日记忆失败: spaceId={}, botId={}, userId={}, error={}", spaceId, botId, userId, ExpUtil.getMsg(e));
      }
    });
  }

  /**
   * 实际执行每日记忆更新逻辑
   */
  private void doUpdateDailyMemory(LlmClient modelClient, List<Message> messages,
      Long spaceId, Long botId, Long userId, Long tenantId) {
    // 当天日期文件名，如 MEMORY-2026-04-18.md
    String today = LocalDate.now().toString();
    String dailyFileName = "MEMORY-" + today + ".md";

    // 查询当天的每日记忆文件内容
    AiWorkspaceDTO existing = workspaceManageMapper.selectMemoryFileByName(spaceId, tenantId, botId, null, userId, dailyFileName);

    if (existing != null && existing.getId() != null) {
      // 已有当天文件，让大模型结合已有内容做智能更新
      String existingContent = StringUtils.defaultString(existing.getFileContent());
      String updatedContent = MemoryCompactor.updateDailyMemory(modelClient, existingContent, messages);
      if (StringUtils.isBlank(updatedContent)) {
        return;
      }
      workspaceManageMapper.updateFileContentById(existing.getId(), updatedContent);
      // 更新向量索引
      updateVector(tenantId, userId, botId, spaceId, dailyFileName, updatedContent);
    }
    else {
      // 当天第一条记录，生成新摘要并创建文件
      String dailySummary = MemoryCompactor.summaryForDailyMemory(modelClient, messages);
      if (StringUtils.isBlank(dailySummary)) {
        return;
      }
      AiWorkspaceDTO workspace = new AiWorkspaceDTO();
      workspace.setId(Sequences.AI_WORKSPACE_ID.next());
      workspace.setSpaceId(spaceId);
      workspace.setTenantId(tenantId);
      workspace.setBotId(botId);
      workspace.setFileName(dailyFileName);
      workspace.setFileContent(dailySummary);
      workspace.setMemoryType(BaseConsts.MEMORY_TYPE_DAILY);
      workspace.setCreatorId(userId);
      workspace.setUpdatorId(userId);
      workspace.setStatusCd(BaseConsts.STATUS_CD_VALID);
      workspaceManageMapper.insertAiWorkspace(workspace);
      // 更新向量索引
      updateVector(tenantId, userId, botId, spaceId, dailyFileName, dailySummary);
    }
  }

  /**
   * 更新向量索引（ES 可用时执行）
   */
  private void updateVector(Long tenantId, Long userId, Long botId, Long spaceId,
                            String fileName, String content) {
    if (!vectorService.isAvailable()) {
      return;
    }
    try {
      vectorService.updateMemoryVector(tenantId, userId, botId, spaceId, fileName, content);
    }
    catch (Exception e) {
      logger.error("异步更新每日记忆向量索引失败: fileName={}, error={}", fileName, ExpUtil.getMsg(e));
    }
  }
}
