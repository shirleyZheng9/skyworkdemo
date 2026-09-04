package com.iwhalecloud.bote.service.lcdp;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppAggregateInfoDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAppVersionDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpAttrSpecDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpPageInstDTO;
import com.iwhalecloud.bote.dto.lcdp.LcdpStandardServiceDTO;
import com.iwhalecloud.bote.dto.lcdp.query.LcdpQueryParams;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 灵犀平台查询服务
 *
 * @author qian.sisheng
 * @since 2025-06-05
 */
public interface ILcdpManageService {
  /**
   * 查询编排服务列表
   *
   * @param queryParams 查询参数
   * @return 应用版本列表
   */
  PageInfo<LcdpStandardServiceDTO> queryStandardServicePage(LcdpQueryParams queryParams);

  /**
   * 查询应用版本列表
   *
   * @param tenantId 租户 ID
   * @return 应用版本列表
   */
  List<LcdpAppVersionDTO> queryAppVersionList(Long tenantId);

  /**
   * 查询应用聚合信息
   *
   * @param queryParams 查询参数
   * @return 应用聚合信息
   */
  LcdpAppAggregateInfoDTO queryAppAggregateInfo(LcdpQueryParams queryParams);

  /**
   * 查询页面实例列表
   *
   * @param queryParams 查询参数
   * @return 页面实例列表
   */
  PageInfo<LcdpPageInstDTO> queryPageInstPage(LcdpQueryParams queryParams);

  /**
   * 查询属性规格列表
   *
   * @param queryParams 查询参数
   * @return 属性规格列表
   */
  PageInfo<LcdpAttrSpecDTO> queryAttrSpecPage(LcdpQueryParams queryParams);

  /**
   * 批量保存页面实例
   *
   * @param skillPageList 页面实例列表
   * @return 结果
   */
  ResultVO<Void> batchSavePageInst(List<SkillPageDTO> skillPageList);

  /**
   * 保存属性规格
   *
   * @param attrSpec 属性规格
   * @return 结果
   */
  ResultVO<Void> batchSaveAttrSpec(LcdpAttrSpecDTO attrSpec);

  /**
   * 批量保存API服务
   *
   * @param service 服务
   * @return 结果
   */
  ResultVO<Void> batchSaveService(SkillServiceDTO service);
}
