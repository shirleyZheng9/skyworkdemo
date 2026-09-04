package com.iwhalecloud.bote.doc.module.control.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档节点的基础信息
 *
 * @author Aiqing
 * @since 2025-08-18
 */
@Data
public class BaseNodeInfo {

  @Schema(description = "文档节点ID", example = "nod10")
  protected String nodeId;

  @Schema(description = "文档名称", example = "This is a node")
  protected String nodeName;

  @Schema(description = "文档节点类型", example = "1")
  private String nodeType;
  @Schema(description = "内容来源", example = "UPLOAD/ONLINE")
  private String contentSource;
}
