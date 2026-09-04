package com.iwhalecloud.bote.service.element;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.EntityInfoDTO;
import com.iwhalecloud.bote.dto.base.query.EntityPageQueryParams;
import java.util.List;
import java.util.Set;

/**
 * 实体查询服务
 *
 * @author qian.sisheng
 * @since 2025-12-08
 */
public interface IEntityInfoQueryService {
  /**
   * 查询实体基本信息
   *
   * @param tenantId 租户ID
   * @param entityType 实体类型
   * @param entityIds 实体ID
   */
  List<EntityInfoDTO> getEntityInfo(Long tenantId, String entityType, Set<Long> entityIds);

  /**
   * 查询实体分页信息
   *
   * @param queryParam 查询参数
   */
  PageInfo<?> queryEntityPage(EntityPageQueryParams queryParam);
}
