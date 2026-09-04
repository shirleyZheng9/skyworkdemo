package com.iwhalecloud.bote.dto.beyond;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应目录检索请求对象
 *
 * @author lizuyin
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString
public class BeyondCatalogTreeRequest {
  /** 目录类型 1-智能体，2-文档库 3-插件 4-数据库，5-MCP服务 6-工具 */
  private String catalogType;
}
