package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SimpleSkillPluginDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.skill.SkillPluginDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;

/**
 * 技能：插件 服务
 *
 * @author auto
 * @since 2024-09-21
 */
public interface ISkillPluginManageService {

  /**
   * 查询单个插件
   *
   * @param tenantId 租户 ID
   * @param apiId 插件主键
   * @return 插件
   */
  SkillPluginDTO getSkillPlugin(Long tenantId, Long apiId);

  /**
   * 保存插件
   *
   * @param plugin 插件
   * @return 结果
   */
  ResultVO<SkillPluginDTO> saveSkillPlugin(SkillPluginDTO plugin);

  /**
   * 删除插件
   *
   * @param tenantId 租户 ID
   * @param apiId 插件主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillPlugin(Long tenantId, Long apiId);

  /**
   * 查询插件列表
   *
   * @param queryParams 查询条件
   * @return 插件列表
   */
  List<SkillPluginDTO> querySkillPluginList(SkillQueryParams queryParams);

  /**
   * 查询插件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 插件分页列表
   */
  PageInfo<SkillPluginDTO> querySkillPluginPage(SkillQueryParams queryParams);

  /**
   * 查询插件列表（分页）
   *
   * @param queryParams 查询条件
   * @return 插件分页列表
   */
  PageInfo<SimpleSkillPluginDTO> querySimpleSkillPluginPage(SkillQueryParams queryParams);
}
