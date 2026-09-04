package com.iwhalecloud.bote.mapper.model;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * 大模型管理
 *
 * @author auto
 * @since 2024-09-20
 */
public interface LargeModelQueryMapper {


  /**
   * 查询大模型列表
   *
   * @param tenantId 租户 ID
   * @return 大模型列表
   */
  List<Long> selectLargeModelList(@Param("tenantId") Long tenantId);

}
