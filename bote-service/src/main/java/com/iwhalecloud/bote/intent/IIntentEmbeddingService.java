package com.iwhalecloud.bote.intent;

import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemWithSceneDTO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 意图问句向量服务
 *
 * @author bianjp
 * @since 2024-12-23
 */
public interface IIntentEmbeddingService {

  /**
   * 意图问句向量化
   */
  void save(IntentQuestionDTO question);

  /**
   * 批量意图问句向量化
   */
  void batchSave(List<IntentQuestionDTO> questions);

  /**
   * 删除意图问句向量
   */
  void delete(Long tenantId, Long questionId);

  /**
   * 重建租户的意图问句数据
   */
  void rebuild(Long tenantId);

  /**
   * 获取最佳匹配的场景 ID
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID, 可选。不为空时只匹配该机器人下的问题
   * @param question 问句
   * @return 场景 ID
   */
  @Nullable
  Long bestMatch(Long tenantId, @Nullable Long botId, String question);

  /**
   * 获取匹配度最高的前 topNum 项
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID, 可选。不为空时只匹配该机器人下的问题
   * @param question 问句
   * @param scoreThreshold 评分阈值，可选，默认取租户设置
   * @param topNum 最大返回数量
   * @return 匹配项列表
   */
  List<IntentionMatchItemWithSceneDTO> topMatches(Long tenantId, @Nullable Long botId, String question, @Nullable Float scoreThreshold, int topNum);
}
