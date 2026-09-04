package com.iwhalecloud.bote.mapper.intent;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.intent.IntentionMatchItemWithSceneDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 意图问句管理
 *
 * @author auto
 * @since 2024-12-18
 */
public interface IntentQuestionManageMapper {

  /**
   * 校验意图问句唯一性
   *
   * @param question 意图问句
   * @return 结果
   */
  boolean existsQuestion(@Param("dto") IntentQuestionDTO question);

  /**
   * 根据主键获取意图问句
   */
  IntentQuestionDTO getIntentQuestion(@Param("tenantId") Long tenantId, @Param("id") Long id);

  /**
   * 新增意图问题
   *
   * @param question 意图问句
   * @return 结果
   */
  int insertIntentQuestion(@Param("dto") IntentQuestionDTO question);

  /**
   * 批量新增意图问句
   *
   * @param questions 意图问句列表
   * @return 结果
   */
  int batchInsertIntentQuestion(@Param("list") List<IntentQuestionDTO> questions);

  /**
   * 修改意图问句
   *
   * @param question 意图问句
   * @return 结果
   */
  int updateIntentQuestion(@Param("dto") IntentQuestionDTO question);

  /**
   * 删除意图问句
   */
  int deleteIntentQuestion(@Param("tenantId") Long tenantId, @Param("id") Long id, @Param("updatorId") Long updatorId);

  /**
   * 获取意图问句列表
   *
   * @param queryParams 查询条件
   * @return 意图问句列表
   */
  List<IntentQuestionDTO> selectIntentQuestionList(@Param("query") IntentQueryParams queryParams);

  /**
   * 获取意图问句列表（分页）
   *
   * @param queryParams 查询条件
   * @return 意图问句分页列表
   */
  Page<IntentQuestionDTO> selectIntentQuestionPage(@Param("query") IntentQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询租户下的所有意图标注问题
   */
  List<IntentQuestionDTO> selectQuestionsByTenantId(@Param("tenantId") Long tenantId);

  /**
   * 根据意图问句 ID 列表批量查询关联的机器人、场景、问句
   */
  List<IntentionMatchItemWithSceneDTO> selectSceneAndQuestionsByIds(@Param("tenantId") Long tenantId, @Param("ids") List<Long> ids);

  /**
   * 根据意图问句 ID 查询关联的场景 ID
   */
  @Nullable
  Long selectSceneIdByQuestionId(@Param("tenantId") Long tenantId, @Param("questionId") Long questionId);

  /**
   * 根据意图问句内容删除关联的意图问句
   */
  int deleteIntentQuestionByQuestion(@Param("tenantId") Long tenantId, @Param("question") String question, @Param("updatorId") Long updatorId);
}
