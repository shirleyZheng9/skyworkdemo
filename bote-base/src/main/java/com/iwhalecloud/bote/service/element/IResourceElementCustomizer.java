package com.iwhalecloud.bote.service.element;

/**
 * 配置数据实体关系记录 - 定制服务
 *
 * @author chen.linfa
 * @since 2025-04-09
 */
public interface IResourceElementCustomizer {
  /**
   * 计算并记录实体关系，用于配置新增、修改场景
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   */
  void submit(Long tenantId, Long resourceId);

  /**
   * 清理实体关系，用于配置删除场景
   *
   * @param tenantId 租户 ID
   * @param resourceId 资源 ID
   */
  void clear(Long tenantId, Long resourceId);
}
