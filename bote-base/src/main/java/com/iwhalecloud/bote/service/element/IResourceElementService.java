package com.iwhalecloud.bote.service.element;

import com.iwhalecloud.bote.dto.base.SimpleElementDTO;
import java.util.List;
import java.util.Map;

/**
 * 配置数据实体关系记录
 *
 * @author chen.linfa
 * @since 2025-07-29
 */
public interface IResourceElementService {

  /**
   * 批量新增实体关系
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param resourceType 资源类型
   * @param elementIds 元素 ID 集合
   * @param elementType 元素类型
   */
  void batchAdd(Long tenantId, Long resourceId, String resourceType, List<Long> elementIds, String elementType);

  /**
   * 删除单个实体关系
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   * @param elementId 元素 ID
   * @param elementType 元素类型
   */
  void remove(Long tenantId, Long resourceId, Long elementId, String elementType);

  /**
   * 获取关联的元素，用于增量导出
   *
   * @param tenantId 租户 ID
   * @param resourceIds 资源 ID 集合
   * @param resourceType 资源类型
   * @return 元素
   */
  Map<String, List<SimpleElementDTO>> queryRelatedResource(Long tenantId, List<Long> resourceIds, String resourceType);

  /**
   * 判断当前元素是否存在关联资源
   *
   * @param tenantId 租户 ID
   * @param elementId 元素 ID
   * @param elementType 元素类型
   * @return 当前元素是否存在引用关系
   */
  boolean existsRelatedResource(Long tenantId, Long elementId, String elementType);
}
