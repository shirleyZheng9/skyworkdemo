package com.iwhalecloud.bote.mapper.intent;

import com.iwhalecloud.bote.dto.intent.IntentionEmbeddingDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 意图向量相关数据库操作
 *
 * @author bianjp
 * @since 2024-12-23
 */
public interface IntentionEmbeddingMapper {
  /**
   * 插入记录
   */
  int insert(@Param("dto") IntentionEmbeddingDTO dto);

  /**
   * 批量插入记录
   */
  int batchInsert(@Param("list") List<IntentionEmbeddingDTO> list);

  /**
   * 根据 ID 更新问题
   */
  int update(@Param("id") Long id, @Param("question") float[] question);

  /**
   * 根据意图问句 ID 查询主键
   */
  @Nullable
  Long selectIdByQuestionId(@Param("questionId") Long questionId);

  /**
   * 根据意图问句 ID 列表批量查询主键
   */
  List<IntentionEmbeddingDTO> selectIdsByQuestionIds(@Param("questionIds") List<Long> questionIds);

  /**
   * 根据意图问句 ID 删除记录
   */
  int deleteByQuestionId(@Param("questionId") Long questionId);

  /**
   * 根据机器人 ID 删除记录
   */
  int deleteByBotId(@Param("botId") Long botId);

  /**
   * 根据租户 ID 删除记录
   */
  int deleteByTenantId(@Param("tenantId") Long tenantId);

  /**
   * 获取匹配度最高的前 topNum 项
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID, 可选
   * @param question 问题
   * @param minScore 最低评分
   * @param topNum 最大返回数量
   * @return 匹配项列表
   */
  List<IntentionMatchItemDTO> selectTopMatches(@Param("tenantId") Long tenantId,
                                               @Nullable @Param("botId") Long botId,
                                               @Param("question") float[] question,
                                               @Param("minScore") float minScore,
                                               @Param("topNum") int topNum);

}
