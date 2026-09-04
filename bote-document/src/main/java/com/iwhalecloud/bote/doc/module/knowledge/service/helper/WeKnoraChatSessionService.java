package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.resp.WeKnoraSessionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.entity.WeKnoraChatSessionEntity;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.WeKnoraChatSessionMapper;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Date;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * WeKnora 会话管理服务
 *
 * <p>维护 博特会话 ↔ WeKnora session_id 的映射；按租户 + 博特会话标识 + 创建人（creatorId）复用记录。</p>
 *
 * @author huangyunming
 * @since 2026-03-31
 */
@Service
@ConditionalOnBooleanProperty(name = "knowledge.weknora.enabled")
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WeKnoraChatSessionService {

  private static final Logger logger = LoggerFactory.getLogger(WeKnoraChatSessionService.class);

  private final WeKnoraLoginHelper loginHelper;
  private final WeKnoraChatSessionMapper chatSessionMapper;

  /**
   * 获取或创建 WeKnora Session ID
   *
   * <p>优先从 {@code bt_weknora_chat_session} 按租户、博特会话标识、创建人查找复用；
   * 不存在时调用 WeKnora 创建 Session 并落库。对应列 {@code creator_id}。</p>
   *
   * @param tenantId      博特租户 ID
   * @param creatorId     创建人 ID（列 creator_id）
   * @param boteSessionId 博特会话标识（可使用 clientId 或自定义）
   * @return WeKnora session_id
   */
  public String getOrCreateSessionId(Long tenantId, Long creatorId, String boteSessionId) {
    Assert.notNull(tenantId, "tenantId 不能为空");
    Assert.hasText(boteSessionId, "boteSessionId 不能为空");

    WeKnoraChatSessionEntity existing = chatSessionMapper.findByTenantAndBoteSessionAndCreator(tenantId, boteSessionId, creatorId);
    if (existing != null) {
      return existing.getWeKnoraSessionId();
    }

    WeKnoraSessionDTO sessionDTO = loginHelper.createSession(tenantId, new HashMap<>());
    String weKnoraSessionId = sessionDTO.getId();

    WeKnoraChatSessionEntity entity = new WeKnoraChatSessionEntity();
    entity.setWeKnoraChatSessionId(IDUtils.nextId());
    entity.setBoteSessionId(boteSessionId);
    entity.setWeKnoraSessionId(weKnoraSessionId);
    entity.setTenantId(tenantId);
    entity.setCreatorId(creatorId);
    entity.setCreateTime(new Date());

    try {
      chatSessionMapper.insert(entity);
    }
    catch (Exception e) {
      logger.warn("Failed to save WeKnora session mapping (may be duplicate): tenantId={}, boteSessionId={}, creatorId={}",
        tenantId, boteSessionId, creatorId, e);
    }

    return weKnoraSessionId;
  }
}
