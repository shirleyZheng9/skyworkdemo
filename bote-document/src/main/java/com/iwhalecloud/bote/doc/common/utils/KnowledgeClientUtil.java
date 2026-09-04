package com.iwhalecloud.bote.doc.common.utils;

import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeCallObserver;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.ObservingKnowledgeClient;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.util.Assert;

/**
 * 知识库客户端工具类
 *
 * @author bianjp
 * @since 2025-05-13
 */
public final class KnowledgeClientUtil {
  /** 知识库客户端列表，按 Bean 优先级排序 */
  private static final List<KnowledgeClient> knowledgeClients = SpringUtil.getBeans(KnowledgeClient.class);

  private KnowledgeClientUtil() {
  }

  /**
   * 获取知识库类型列表
   *
   * @return 知识库类型列表
   */
  public static List<String> getKnowledgeTypes() {
    return knowledgeClients.stream().map(KnowledgeClient::getKnowledgeType).collect(Collectors.toList());
  }

  /**
   * 获取知识库客户端
   *
   * @param knowledgeType 知识库类型
   * @return 知识库客户端
   */
  public static KnowledgeClient getClient(String knowledgeType) {
    Assert.hasLength(knowledgeType, "知识库类型不能为空");
    KnowledgeClient knowledgeClient = IterableUtils.find(knowledgeClients, k -> knowledgeType.equals(k.getKnowledgeType()));
    Assert.notNull(knowledgeClient, () -> "未知的知识库类型: " + knowledgeType);
    KnowledgeCallObserver observer = SpringUtil.getBeanOptional(KnowledgeCallObserver.class);
    if (observer != null) {
      return new ObservingKnowledgeClient(knowledgeClient, observer);
    }
    return knowledgeClient;
  }
}
