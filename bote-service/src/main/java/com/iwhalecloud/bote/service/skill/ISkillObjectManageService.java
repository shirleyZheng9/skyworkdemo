package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SkillObjectDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 技能：对象 服务
 *
 * @author auto
 * @since 2024-09-16
 */
public interface ISkillObjectManageService {

  /**
   * 保存对象
   *
   * @param object 对象
   * @return 结果
   */
  ResultVO<SkillObjectDTO> saveSkillObject(SkillObjectDTO object);

  /**
   * 查询单个对象
   *
   * @param tenantId 租户 ID
   * @param busiObjectId 对象主键
   * @return 对象
   */
  SkillObjectDTO findSkillObject(Long tenantId, Long busiObjectId);

  /**
   * 查询对象列表
   *
   * @param queryParams 查询条件
   * @return 对象列表
   */
  List<SkillObjectDTO> querySkillObjectList(SkillQueryParams queryParams);

  /**
   * 查询对象列表（分页）
   *
   * @param queryParams 查询条件
   * @return 对象分页列表
   */
  PageInfo<SkillObjectDTO> querySkillObjectPage(SkillQueryParams queryParams);

  /**
   * 删除对象
   *
   * @param tenantId 租户 ID
   * @param busiObjectId 对象主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillObject(Long tenantId, Long busiObjectId);
}
