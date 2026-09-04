package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SkillTextDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 技能：文本 服务
 *
 * @author auto
 * @since 2024-09-16
 */
public interface ISkillTextManageService {

  /**
   * 保存文本
   *
   * @param text 文本
   * @return 结果
   */
  ResultVO<SkillTextDTO> saveSkillText(SkillTextDTO text);

  /**
   * 查询单个文本
   *
   * @param tenantId 租户 ID
   * @param textId 文本主键
   * @return 文本
   */
  SkillTextDTO findSkillText(Long tenantId, Long textId);

  /**
   * 查询文本列表
   *
   * @param queryParams 查询条件
   * @return 文本列表
   */
  List<SkillTextDTO> querySkillTextList(SkillQueryParams queryParams);

  /**
   * 查询文本列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文本分页列表
   */
  PageInfo<SkillTextDTO> querySkillTextPage(SkillQueryParams queryParams);

  /**
   * 删除文本
   *
   * @param tenantId 租户 ID
   * @param textId 文本主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillText(Long tenantId, Long textId);
}
