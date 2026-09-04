package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.CatalogDTO;
import com.iwhalecloud.bote.dto.base.query.CatalogQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 目录管理
 *
 * @author auto
 * @since 2024-09-13
 */
public interface CatalogManageMapper {
  /**
   * 根据主键获取目录
   */
  CatalogDTO getCatalog(@Param("tenantId") Long tenantId, @Param("id") Long catalogId);

  /**
   * 新增目录
   *
   * @param catalog 目录
   * @return 结果
   */
  int insertCatalog(@Param("dto") CatalogDTO catalog);

  /**
   * 修改目录
   *
   * @param catalog 目录
   * @return 结果
   */
  int updateCatalog(@Param("dto") CatalogDTO catalog);

  /**
   * 删除目录
   */
  int deleteCatalog(@Param("tenantId") Long tenantId, @Param("catalogId") Long catalogId, @Param("updatorId") Long updatorId);

  /**
   * 获取目录列表
   *
   * @param queryParams 查询条件
   * @return 目录列表
   */
  List<CatalogDTO> selectCatalogList(@Param("query") CatalogQueryParams queryParams);

  /**
   * 获取子目录 ID 列表
   */
  List<Long> selectChildrenCatalogIds(@Param("tenantId") Long tenantId, @Param("spaceId") Long spaceId, @Param("catalogId") String catalogId, @Param("catalogType") String catalogType);

  /**
   * 检验目录下是否有数据
   *
   * @param catalogId 目录ID
   * @param tenantId 租户ID
   * @return 结果
   */
  boolean existsData(@Param("catalogItemId") Long catalogId, @Param("tenantId") Long tenantId);

  /**
   * 检查目录名称是否存在
   */
  boolean existsCatalogName(@Param("tenantId") Long tenantId, @Param("parCatalogId") Long parCatalogId,
                            @Param("catalogType") String catalogType, @Param("catalogName") String catalogName, @Param("spaceId") Long spaceId);
}
