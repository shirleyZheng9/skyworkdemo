package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.iwhalecloud.bote.doc.module.knowledge.entity.WeKnoraChatSessionEntity;
import org.apache.ibatis.annotations.Param;

/**
 * WeKnora 会话映射 Mapper
 *
 * @author bianjp
 * @since 2026-03-31
 */
public interface WeKnoraChatSessionMapper {

  /**
   * 按租户、博特会话标识、创建人查询会话映射
   *
   * @param tenantId      租户 ID
   * @param boteSessionId 博特会话 ID
   * @param creatorId     创建人 ID
   * @return 会话映射，不存在则返回 null
   */
  WeKnoraChatSessionEntity findByTenantAndBoteSessionAndCreator(@Param("tenantId") Long tenantId,
                                                                @Param("boteSessionId") String boteSessionId,
                                                                @Param("creatorId") Long creatorId);

  /**
   * 插入会话映射记录
   *
   * @param entity 会话映射
   * @return 影响行数
   */
  int insert(@Param("entity") WeKnoraChatSessionEntity entity);
}
