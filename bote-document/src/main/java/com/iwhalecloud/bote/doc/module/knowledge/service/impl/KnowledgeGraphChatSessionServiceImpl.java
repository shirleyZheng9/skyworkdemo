package com.iwhalecloud.bote.doc.module.knowledge.service.impl;

import com.iwhalecloud.bote.doc.module.knowledge.entity.KnowledgeGraphSessionRelEntity;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeGraphChatSessionMapper;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeGraphChatSessionService;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Date;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * knowledgeGraph 会话映射服务
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBooleanProperty("knowledge.knowledgeGraph.enabled")
public class KnowledgeGraphChatSessionServiceImpl implements IKnowledgeGraphChatSessionService {
  private final KnowledgeGraphChatSessionMapper mapper;

  /**
   * 获取或创建 knowledgeGraph 会话 ID
   *
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @param boteSessionId 博特会话 ID
   * @param knowledgeBaseId 知识库标识
   * @param sessionCreator 第三方会话创建函数
   * @return third-party session_id
   */
  @Transactional
  public String getOrCreateSessionId(Long tenantId, Long userId, String boteSessionId, String knowledgeBaseId,
                                     Supplier<String> sessionCreator) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    Assert.notNull(userId, "userId 不能为空");
    Assert.hasText(boteSessionId, "boteSessionId 不能为空");
    Assert.hasText(knowledgeBaseId, "knowledgeBaseId 不能为空");
    KnowledgeGraphSessionRelEntity existing = mapper.findByUniqueKey(tenantId, userId, boteSessionId, knowledgeBaseId);
    if (existing != null && StringUtils.isNotEmpty(existing.getKgSessionId())) {
      return existing.getKgSessionId();
    }
    // 不存在映射时才创建第三方会话
    String kgSessionId = sessionCreator.get();
    KnowledgeGraphSessionRelEntity entity = new KnowledgeGraphSessionRelEntity();
    entity.setId(IDUtils.nextId());
    entity.setBoteSessionId(boteSessionId);
    entity.setKnowledgeBaseId(knowledgeBaseId);
    entity.setKgSessionId(kgSessionId);
    entity.setTenantId(tenantId);
    entity.setUserId(userId);
    entity.setCreatedTime(new Date());
    entity.setUpdatedTime(new Date());
    mapper.insert(entity);
    return kgSessionId;
  }
}
