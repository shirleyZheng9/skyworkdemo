package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;

import com.iwhalecloud.bote.dto.skill.query.SkillPublishApplyQueryParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能发布申请 Mapper
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
public interface SkillPublishApplyMapper {
  /**
   * 根据主键获取技能发布申请
   *
   * @param applyId 主键
   * @return 技能发布申请
   */
  SkillPublishApplyDTO selectSkillPublishApply(@Param("applyId") Long applyId);

  /**
   * 新增技能发布申请
   *
   * @param dto 技能发布申请
   * @return 结果
   */
  int insertSkillPublishApply(@Param("dto") SkillPublishApplyDTO dto);

  /**
   * 修改技能发布申请
   *
   * @param dto 技能发布申请
   * @return 结果
   */
  int updateSkillPublishApply(@Param("dto") SkillPublishApplyDTO dto);

  /**
   * 删除技能发布申请
   *
   * @param applyId 主键
   * @param updatorId 更新人 ID
   * @return 结果
   */
  int deleteSkillPublishApply(@Param("applyId") Long applyId, @Param("updatorId") Long updatorId);

  /**
   * 分页获取技能发布申请审核列表（管理端使用）
   *
   * @param params 技能发布申请查询参数
   * @param rowBounds 分页参数
   * @return 技能发布申请分页列表
   */
  Page<SkillPublishApplyDTO> selectSkillPublishApplyAuditPage(@Param("params") SkillPublishApplyQueryParams params, RowBounds rowBounds);

  /**
   * 分页获取用户发布的技能申请列表
   *
   * @param params 技能发布申请查询参数
   * @param rowBounds 分页参数
   * @return 技能发布申请分页列表
   */
  Page<SkillPublishApplyDTO> selectUserSkillPublishApplyPage(@Param("params") SkillPublishApplyQueryParams params, RowBounds rowBounds);

  /**
   * 更新审核状态
   *
   * @param applyId 申请 ID
   * @param auditStatus 审核状态
   * @param auditUserId 审核人 ID
   * @param auditContent 审核意见
   * @return 结果
   */
  int updateAuditStatus(@Param("applyId") Long applyId, @Param("auditStatus") Integer auditStatus,
                        @Param("auditUserId") Long auditUserId, @Param("auditContent") String auditContent);

  /**
   * 失效技能在此次发布申请之前的全部待审核申请
   *
   * @param applyId 申请 ID
   * @param spaceId 空间 ID
   * @param skillId 技能 ID
   * @param userId 用户 ID
   * @return 结果
   */
  int invalidSkillOldApply(@Param("applyId") Long applyId, @Param("spaceId") Long spaceId,
                           @Param("skillId") Long skillId, @Param("userId") Long userId);
}
