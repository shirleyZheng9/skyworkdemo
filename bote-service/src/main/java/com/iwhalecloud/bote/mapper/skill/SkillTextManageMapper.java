package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：文本 Mapper
 *
 * @author auto
 * @since 2024-09-16
 */
public interface SkillTextManageMapper {
  /**
   * 新增文本
   *
   * @param text 文本
   * @return 结果
   */
  int insertSkillText(@Param("dto") SkillTextDTO text);

  /**
   * 修改文本
   *
   * @param text 文本
   * @return 结果
   */
  int updateSkillText(@Param("dto") SkillTextDTO text);

  /**
   * 根据主键获取文本
   */
  SkillTextDTO getSkillText(@Param("tenantId") Long tenantId, @Param("id") Long textId);

  /**
   * 获取文本列表
   *
   * @param queryParams 查询条件
   * @return 文本列表
   */
  List<SkillTextDTO> selectSkillTextList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取文本列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文本分页列表
   */
  Page<SkillTextDTO> selectSkillTextPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除文本
   */
  int deleteSkillText(@Param("tenantId") Long tenantId, @Param("textId") Long textId, @Param("updatorId") Long updatorId);

}
