package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.base.GuidanceCfgDTO;

import java.util.List;

/**
 * 平台使用指引管理服务
 *
 * @author wangtingyun
 * @since 2025-11-11
 */
public interface IGuidanceManageService {

  /**
   * 根据用户角色获取指引信息
   *
   * @param roleCode 用户角色
   * @return 步骤指引信息
   */
  List<GuidanceCfgDTO> queryStepGuidance(String roleCode);

  /**
   * 查询平台新手操作指引
   *
   * @return 操作指引
   */
  List<GuidanceCfgDTO> getBeginnerGuidance();

  /**
   * 查询是否存在新手入门指引
   *
   * @return 是否存在新手入门指引
   */
  Boolean existsBeginnerGuidance();

}
