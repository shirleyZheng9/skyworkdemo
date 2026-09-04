package com.iwhalecloud.bote.doc.module.knowledge.service;

import java.util.function.Supplier;

/**
 * 知识图谱聊天会话服务
 *
 * @author qian.sisheng
 * @since 2026-04-18
 */
public interface IKnowledgeGraphChatSessionService {
  /**
   * 获取或创建知识图谱聊天会话ID
   *
   * @param tenantId 租户ID
   * @param userId 用户ID
   * @param boteSessionId bote会话ID
   * @param knowledgeBaseId 知识库ID
   * @param sessionCreator 会话创建器
   * @return 知识图谱聊天会话ID
   */
  String getOrCreateSessionId(Long tenantId, Long userId, String boteSessionId, String knowledgeBaseId, Supplier<String> sessionCreator);
}
