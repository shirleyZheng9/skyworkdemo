package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.iwhalecloud.bote.doc.module.knowledge.dto.BoteModelDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.WeknoraModelRelDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 博特模型与weknora模型关联 Mapper
 *
 * @author qian.sisheng
 * @since 2026-04-09
 */
public interface WeKnoraModelRelMapper {

  /**
   * 插入博特模型与weknora模型关联
   *
   * @param dto 博特模型与weknora模型关联
   * @return 影响行数
   */
  int insertWeKnoraModelRel(@Param("dto") WeknoraModelRelDTO dto);

  /**
   * 根据博特模型ID获取博特模型与weknora模型关联
   *
   * @param boteModelId 博特模型ID
   * @param tenantId 租户ID
   * @return 博特模型与weknora模型关联
   */
  WeknoraModelRelDTO getWeKnoraModelRelByBoteModelId(@Param("boteModelId") Long boteModelId, @Param("tenantId") Long tenantId);

  /**
   * 获取博特模型
   *
   * @param modelId 博特模型ID
   * @param tenantId 租户ID
   * @return 博特模型
   */
  BoteModelDTO getModelById(@Param("modelId") Long modelId, @Param("tenantId") Long tenantId);

  /**
   * 获取默认模型
   *
   * @param tenantId 租户ID
   * @return 博特默认模型
   */
  String getTenantDefaultModel(@Param("tenantId") Long tenantId);
}
