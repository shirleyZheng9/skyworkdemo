package com.iwhalecloud.bote.doc.module.knowledge.strategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * 获取接入的知识库策略入口
 *
 * @author lxs
 * @since 2025/07/14
 */
@Component
public class KnowledgeAccessStrategyEntry {

  /** 接入的知识库集合 */
  private final Map<String, IKnowledgeAccessStrategy> knowledgeAccessMap;

  /**
   * 初始化所有接入的知识库
   */
  public KnowledgeAccessStrategyEntry(List<IKnowledgeAccessStrategy> accessStrategyList) {
    this.knowledgeAccessMap = accessStrategyList.stream()
      .collect(Collectors.toMap(IKnowledgeAccessStrategy::getKnowledgeAccessType, b -> b));
  }

  /**
   * 获取接入的知识库
   *
   * @param accessType 接入类型
   * @return 接入的知识库
   */
  public IKnowledgeAccessStrategy getAccessStrategy(String accessType) {
    IKnowledgeAccessStrategy accessStrategy = knowledgeAccessMap.get(accessType);
    if (accessStrategy == null) {
      throw new IllegalArgumentException("没有对应的接入知识库策略：accessType:" + accessType);
    }
    return accessStrategy;
  }
}
