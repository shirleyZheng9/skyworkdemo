package com.iwhalecloud.bote.beyond;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.entity.beyond.BeyondSessionEntity;
import com.iwhalecloud.bote.mapper.beyond.BeyondSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author 赵旭
 * @since 2025-08-04
 */
@Component
@ConditionalOnBooleanProperty("beyond.enabled")
@RequiredArgsConstructor
public class BeyondSessionHelper {
  private final BeyondSessionMapper beyondSessionMapper;

  /**
   * 保存百应会话信息,如果已存在会话则不处理
   */
  public void saveBeyondSession(String sessionId, Long tenantId) {
    Assert.hasText(sessionId, "百应会话ID不能为空");
    if (beyondSessionMapper.existsBeyondSession(sessionId)) {
      return;
    }
    BeyondSessionEntity beyondSessionEntity = new BeyondSessionEntity();
    beyondSessionEntity.setBeyondSessionId(sessionId);
    beyondSessionEntity.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    beyondSessionEntity.setTenantId(tenantId);
    beyondSessionEntity.setStatusCd(BaseConsts.STATUS_CD_VALID);
    beyondSessionMapper.insert(beyondSessionEntity);
  }

  /**
   * 绑定百应会话信息与鲸加授权码token
   */
  public boolean bindSessionSsoToken(String sessionId, String code) {
    if (beyondSessionMapper.existsBeyondSession(sessionId)) {
      return beyondSessionMapper.updateSsoToken(sessionId, code) > 0;
    }
    return false;
  }
}
