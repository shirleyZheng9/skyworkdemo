package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.ResourceElementDTO;
import com.iwhalecloud.bote.dto.base.SimpleElementDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.Nullable;

/**
 * 资源关联 mapper
 *
 * @author auto
 * @since 2024-09-14
 */
public interface ResourceElementMapper {

  /**
   * 批量插入资源关联数据
   *
   * @param list 资源关联列表
   * @return 结果
   */
  int batchInsertResourceElement(@Param("list") List<ResourceElementDTO> list);

  /**
   * 清理实体关系
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param elementType 元素类型，可选
   * @param elementId 元素 ID，可选
   */
  int deleteElementByResourceId(@Param("tenantId") Long tenantId, @Param("resourceId") Long resourceId,
    @Param("elementType") @Nullable String elementType, @Param("elementId") @Nullable Long elementId);

  /**
   * 查询资源关联
   *
   * @param tenantId 租户 ID
   * @return 结果
   */
  List<ResourceElementDTO> selectResourceElement(@Param("tenantId") Long tenantId);

  /**
   * 查询资源关联
   *
   * @param tenantId 租户 ID
   * @param resourceIds 资源 ID 集合
   * @param resourceType 资源类型
   * @return 元素列表
   */
  List<SimpleElementDTO> selectSimpleElement(@Param("tenantId") Long tenantId, @Param("resourceIds") List<Long> resourceIds,
    @Param("resourceType") String resourceType);

  /**
   * 查询是否存在关联资源
   *
   * @param tenantId 租户 ID
   * @param elementId 元素 ID
   * @param elementType 元素类型
   * @return 是否存在关联资源
   */
  boolean existsRelatedResource(@Param("tenantId") Long tenantId, @Param("elementId") @Nullable Long elementId,
    @Param("elementType") @Nullable String elementType);

  /**
   * 清理实体关系
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param elementType 元素类型
   * @param elementId 原元素 ID
   * @param newElementId 新元素 ID
   */
  int updateElementByResourceId(@Param("tenantId") Long tenantId, @Param("resourceId") Long resourceId, @Param("elementType") String elementType,
    @Param("elementId") Long elementId, @Param("newElementId") Long newElementId);

  /**
   * 查询资源关联
   *
   * @param tenantId 租户 ID
   * @return 结果
   */
  List<ResourceElementDTO> selectElementByResourceId(@Param("tenantId") Long tenantId, @Param("resourceId") Long resourceId,
    @Param("resourceType") String resourceType);

  /**
   * 查询元素关联
   *
   * @param tenantId 租户 ID
   * @param elementId 元素 ID
   * @param elementType 元素类型
   */
  List<ResourceElementDTO> selectResourceByElementId(@Param("tenantId") Long tenantId, @Param("elementId") Long elementId,
    @Param("elementType") String elementType);

  /**
   * 存在资源关联
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param resourceType 资源类型
   */
  boolean existsByResourceId(@Param("tenantId") Long tenantId, @Param("resourceId") Long resourceId, @Param("resourceType") String resourceType);
}
