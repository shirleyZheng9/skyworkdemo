package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * Node Path View.
 * </p>
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档节点的路径")
public class NodePathDTO {

  @Schema(description = "Node ID", example = "nod10")
  private String nodeId;

  @Schema(description = "Node Name", example = "This is a node")
  private String nodeName;
}
