package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.iwhalecloud.bote.doc.module.knowledge.entity.KnowledgeGraphSessionRelEntity;
import org.apache.ibatis.annotations.Param;

/**
 * knowledgeGraph 会话映射 Mapper
 *
 * @author qian.sisheng
 * @since 2026-04-13
 */
public interface KnowledgeGraphChatSessionMapper {
  /**
   * 按唯一键查询会话映射
   *
   * @param tenantId 租户 ID
   * @param userId 用户 ID
   * @param boteSessionId 博特会话 ID
   * @param knowledgeBaseId 知识库标识
   * @return 会话映射
   */
  KnowledgeGraphSessionRelEntity findByUniqueKey(@Param("tenantId") Long tenantId,
                                                  @Param("userId") Long userId,
                                                  @Param("boteSessionId") String boteSessionId,
                                                  @Param("knowledgeBaseId") String knowledgeBaseId);

  /**
   * 插入会话映射记录
   *
   * @param entity 会话映射对象
   * @return 影响行数
   */
  int insert(@Param("entity") KnowledgeGraphSessionRelEntity entity);
}
