package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.AttrSpecValueImport;
import com.iwhalecloud.bote.dto.skill.SkillAttrSpecDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 技能：属性管理服务
 *
 * @author auto
 * @since 2024-09-15
 */
public interface ISkillAttrManageService {
  /**
   * 查询单个属性
   *
   * @param tenantId 租户 ID
   * @param attrId 属性主键
   * @return 属性
   */
  @Nullable
  SkillAttrSpecDTO findAttrSpec(Long tenantId, Long attrId);

  /**
   * 查询单个属性
   *
   * @param attrCode 属性编码
   * @param tenantId 租户 ID
   * @return 属性
   */
  @Nullable
  SkillAttrSpecDTO findAttrSpecByCode(String attrCode, Long tenantId);

  /**
   * 保存属性
   *
   * @param attrSpec 属性
   * @return 结果
   */
  ResultVO<SkillAttrSpecDTO> saveAttrSpec(SkillAttrSpecDTO attrSpec);

  /**
   * 删除属性
   *
   * @param tenantId 租户 ID
   * @param attrId 属性主键
   * @return 结果
   */
  ResultVO<Void> deleteAttrSpec(Long tenantId, Long attrId);

  /**
   * 查询属性列表
   *
   * @param queryParams 查询条件
   * @return 属性列表
   */
  List<SkillAttrSpecDTO> queryAttrSpecList(SkillQueryParams queryParams);

  /**
   * 查询属性列表（分页）
   *
   * @param queryParams 查询条件
   * @return 属性分页列表
   */
  PageInfo<SkillAttrSpecDTO> queryAttrSpecPage(SkillQueryParams queryParams);

  /**
   * 根据导入的静态数据列表来解析导入数据，转换成静态数据
   *
   * @param attrSpecValueImportList 导入的静态数据列表
   * @param tenantId 租户ID
   * @param catalogItemId 目录ID
   */
  void parseSpec(List<AttrSpecValueImport> attrSpecValueImportList, Long tenantId, Long catalogItemId);
}
