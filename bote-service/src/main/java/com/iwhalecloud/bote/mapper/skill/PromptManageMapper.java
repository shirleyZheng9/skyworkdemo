package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.PromptContentDTO;
import com.iwhalecloud.bote.dto.skill.PromptDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 提示词管理
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
public interface PromptManageMapper {

  /**
   * 根据主键获取提示词
   */
  PromptDTO getPrompt(@Param("tenantId") Long tenantId, @Param("id") Long promptId);

  /**
   * 新增提示词
   *
   * @param prompt 提示词
   * @return 结果
   */
  int insertPrompt(@Param("dto") PromptDTO prompt);

  /**
   * 批量新增提示词内容
   *
   * @param list 提示词列表
   * @return 结果
   */
  int batchInsertPromptContent(@Param("list") List<PromptContentDTO> list);

  /**
   * 修改提示词内容
   *
   * @param dto 提示词
   * @return 结果
   */
  int updatePromptContent(@Param("dto") PromptContentDTO dto);

  /**
   * 修改提示词
   *
   * @param prompt 提示词
   * @return 结果
   */
  int updatePrompt(@Param("dto") PromptDTO prompt);

  /**
   * 批量修改提示词
   *
   * @param dto 提示词
   * @return 结果
   */
  int batchUpdatePromptContent(@Param("dto") PromptContentDTO dto);

  /**
   * 删除提示词
   */
  int deletePrompt(@Param("tenantId") Long tenantId, @Param("promptId") Long promptId, @Param("updatorId") Long updatorId);

  /**
   * 获取提示词列表
   *
   * @param queryParams 查询条件
   * @return 提示词列表
   */
  List<PromptDTO> selectPromptList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取提示词列表（分页）
   *
   * @param queryParams 查询条件
   * @return 提示词分页列表
   */
  Page<PromptDTO> selectPromptPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取提示词内容列表
   *
   * @param promptIdList 提示词ID列表
   * @param modelId 模型ID
   * @return 提示词内容列表
   */
  List<PromptContentDTO> selectPromptContentList(@Param("tenantId") Long tenantId,
                                                 @Nullable @Param("promptIdList") List<Long> promptIdList,
                                                 @Nullable @Param("modelId") Long modelId);

  /**
   * 根据提示词 ID 和模型 ID 查询提示词
   *
   * <p>优先返回对应模型的提示词，未配置时返回默认提示词</p>
   */
  String selectPromptContentByPromptIdAndModelId(@Param("tenantId") Long tenantId, @Param("promptId") Long promptId, @Param("modelId") Long modelId);

  /**
   * 根据模型 ID、标题、目录名称查询提示词
   *
   * <p>优先返回对应模型的提示词，未配置时返回默认提示词</p>
   */
  String selectPromptContent(@Param("tenantId") Long tenantId, @Param("modelId") Long modelId, @Param("title") String title,
    @Param("catalogName") String catalogName);

  /**
   * 查询所有有效的提示词（用于数据迁移）
   *
   * @return 提示词列表
   */
  List<PromptDTO> selectAllPromptsForMigration();
}
