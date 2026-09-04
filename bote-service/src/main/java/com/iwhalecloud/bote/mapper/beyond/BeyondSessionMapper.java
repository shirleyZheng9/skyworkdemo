package com.iwhalecloud.bote.mapper.beyond;

import com.iwhalecloud.bote.entity.beyond.BeyondSessionEntity;
import org.apache.ibatis.annotations.Param;

/**
 * @author 赵旭
 * @since 2025-08-04
 */
public interface BeyondSessionMapper {
  /**
   * 插入百应会话记录
   */
  int insert(@Param("session") BeyondSessionEntity session);
  /**
   * 查询是否存在百应会话
   */
  boolean existsBeyondSession(@Param("beyondSessionId") String beyondSessionId);
  /**
   * 更新百应会话信息,绑定ssoToken
   */
  int updateSsoToken(@Param("beyondSessionId") String beyondSessionId, @Param("ssoToken") String ssoToken);
}
