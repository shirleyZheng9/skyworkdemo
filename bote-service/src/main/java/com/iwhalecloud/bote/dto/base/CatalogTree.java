package com.iwhalecloud.bote.dto.base;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用树结构对象
 *
 * @param <T> 树节点存储对象，支持泛型
 * @author chen.linfa
 * @since 2025-01-16
 */
@Getter
@Setter
@ToString
public class CatalogTree<T> {
  /** 节点 ID */
  private String id;

  /** 节点名称 */
  private String name;

  /** 父 ID */
  private String parentId;

  /** 总数，包含子节点 */
  private Integer total;

  /** 业务对象 */
  private List<T> values;

  /** 子节点 */
  private List<CatalogTree<T>> children;
}
