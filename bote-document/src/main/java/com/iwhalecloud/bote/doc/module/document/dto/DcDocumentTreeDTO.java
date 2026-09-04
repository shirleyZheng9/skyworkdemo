package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.common.support.tree.SortableNode;
import com.iwhalecloud.bote.doc.common.support.tree.Tree;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档树结构DTO
 *
 * @author system
 * @since 2025-01-27
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "文档树结构")
public class DcDocumentTreeDTO extends DcDocumentEntity implements SortableNode, Tree<DcDocumentTreeDTO> {

  @Schema(description = "文档库名称")
  private String libraryName;

  @Schema(description = "文件Id")
  private Long fileId;

  @Schema(description = "子节点")
  private List<DcDocumentTreeDTO> children;

  @JsonIgnore
  @Override
  public String getNodeId() {
    return this.getDocumentId();
  }

  @JsonIgnore
  @Override
  public String getPreNodeId() {
    return this.getPrevDocumentId();
  }

  @JsonIgnore
  @Override
  public String getNodeParentId() {
    return this.getParentId();
  }

  @JsonIgnore
  @Override
  public List<DcDocumentTreeDTO> getChildrenNodes() {
    return this.getChildren();
  }

  @Override
  public void setChildrenNodes(List<DcDocumentTreeDTO> childrenNodes) {
    this.setChildren(childrenNodes);
  }
}
