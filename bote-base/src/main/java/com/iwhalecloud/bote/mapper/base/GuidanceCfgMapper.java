package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.GuidanceCfgDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台引用配置 mapper
 *
 * @author wangtingyun
 * @since 2025-11-11
 */
public interface GuidanceCfgMapper {

  /**
   * 查询是否存在新手入门指引
   */
  boolean existsBeginnerGuidanceCfg();

  /**
   * 查询平台新手指引操作指引
   */
  List<GuidanceCfgDTO> selectBeginnerGuidanceCfg();

  /**
   * 查询步骤指引配置列表
   *
   * @param typeList 步骤类型列表
   * @return 步骤指引配置列表
   */
  List<GuidanceCfgDTO> selectStepGuidance(@Param("typeList") List<String> typeList);

}
