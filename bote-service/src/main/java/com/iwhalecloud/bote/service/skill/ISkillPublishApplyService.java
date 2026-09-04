package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SkillPublishApplyDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillPublishApplyQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 技能发布申请服务接口
 *
 * @author wangtingyun
 * @since 2026-04-03
 */
public interface ISkillPublishApplyService {

  /**
   * 保存技能发布申请
   *
   * @param dto 技能发布申请
   * @return 结果
   */
  ResultVO<SkillPublishApplyDTO> saveSkillPublishApply(SkillPublishApplyDTO dto);

  /**
   * 根据主键获取技能发布申请
   *
   * @param applyId 主键
   * @return 技能发布申请
   */
  SkillPublishApplyDTO getSkillPublishApply(Long applyId);

  /**
   * 删除技能发布申请
   *
   * @param applyId 主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillPublishApply(Long applyId);

  /**
   * 分页获取技能发布申请审核列表（管理端使用）
   *
   * @param params 技能发布申请查询参数
   * @return 技能发布申请列表
   */
  PageInfo<SkillPublishApplyDTO> getSkillPublishApplyAuditPage(SkillPublishApplyQueryParams params);

  /**
   * 分页获取用户发布的技能申请列表
   *
   * @param params 技能发布申请查询参数
   * @return 技能发布申请列表
   */
  PageInfo<SkillPublishApplyDTO> getUserSkillPublishApplyPage(SkillPublishApplyQueryParams params);

  /**
   * 审批技能发布申请
   *
   * @param applyDTO 审核参数
   * @return 结果
   */
  ResultVO<Void> auditSkillPublishApply(SkillPublishApplyDTO applyDTO);
}
