package com.iwhalecloud.bote.intent.vector;

import com.iwhalecloud.bote.dto.intent.IntentionEmbeddingDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemDTO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 意图向量存储接口
 *
 * @author bianjp
 * @since 2024-12-23
 */
public interface IntentionVectorStore {

  /**
   * 新增向量记录
   */
  void add(IntentionEmbeddingDTO intentionEmbedding);

  /**
   * 批量保存向量记录
   */
  void batchAdd(List<IntentionEmbeddingDTO> intentionEmbeddings);

  /**
   * 保存向量记录（新增或修改）
   */
  void save(IntentionEmbeddingDTO intentionEmbedding);

  /**
   * 批量保存向量记录（新增或修改）
   */
  void batchSave(List<IntentionEmbeddingDTO> intentionEmbeddings);

  /**
   * 删除向量记录
   */
  void delete(Long tenantId, Long questionId);

  /**
   * 根据租户 ID 批量删除向量存储
   */
  void deleteByTenant(Long tenantId);

  /**
   * 获取最佳匹配
   *
   * @param tenantId 租户 ID
   * @param sceneIds 智能体 ID 集合, 可选。不为空时只匹配范围内的问题
   * @param question 问题
   * @param scoreThreshold 评分阈值(最小评分, 0~1)
   * @return 最佳匹配的意图标注 ID
   */
  @Nullable
  Long bestMatch(Long tenantId, @Nullable List<Long> sceneIds, float[] question, float scoreThreshold);

  /**
   * 获取匹配度最高的前 topNum 项
   *
   * @param tenantId 租户 ID
   * @param sceneIds 智能体 ID 集合, 可选。不为空时只匹配范围内的问题
   * @param question 问题
   * @param scoreThreshold 评分阈值(最小评分, 0~1)
   * @param topNum 最大返回数量
   * @return 匹配项列表
   */
  List<IntentionMatchItemDTO> topMatches(Long tenantId, @Nullable List<Long> sceneIds, float[] question, float scoreThreshold, int topNum);
}
