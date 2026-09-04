package com.iwhalecloud.bote.dto.ontology.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 本体分页查询参数
 *
 * @author qian.sisheng
 * @since 2026-04-29
 */
@Getter
@Setter
@ToString
public class OntologyQueryParams extends PagingQueryParams {
  /** 租户 ID */
  private Long tenantId;
  /** 应用 ID */
  private Long appId;
  /** 场景 ID */
  private Long sceneId;
  /** 搜索内容 */
  private String searchContent;
}
