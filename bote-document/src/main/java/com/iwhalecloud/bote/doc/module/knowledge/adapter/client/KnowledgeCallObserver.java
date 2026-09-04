package com.iwhalecloud.bote.doc.module.knowledge.adapter.client;

import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallParamDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeRecallResponse;
import org.springframework.lang.Nullable;

/**
 * 知识库调用观察者（用于可观测性埋点）
 *
 * @author cursor
 * @since 2026-07-23
 */
public interface KnowledgeCallObserver {

  /**
   * 知识召回结束回调
   */
  void onRecall(String knowledgeType, KnowledgeRecallParamDTO params, @Nullable KnowledgeRecallResponse response,
                long startEpochMs, long endEpochMs, @Nullable Throwable error);

  /**
   * 知识问答结束回调
   */
  void onChat(String knowledgeType, KnowledgeChatParamsDTO params, @Nullable KnowledgeChatResponse response,
              long startEpochMs, long endEpochMs, @Nullable Throwable error);
}
