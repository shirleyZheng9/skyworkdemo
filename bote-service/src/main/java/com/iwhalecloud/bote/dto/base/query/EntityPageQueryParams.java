package com.iwhalecloud.bote.dto.base.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 实体分页查询参数DTO
 *
 * @author qian.sisheng
 * @since 2025-12-08
 */
@Getter
@Setter
@ToString(callSuper = true)
public class EntityPageQueryParams extends PagingQueryParams {
  /** 租户ID */
  private Long tenantId;
  /** 实体类型 */
  private String entityType;
  /** 模糊查询 */
  private String searchContent;
}
