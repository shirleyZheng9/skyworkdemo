package com.iwhalecloud.bote.doc.module.control.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwhalecloud.bote.doc.common.support.tree.Tree;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档节点树信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档节点树信息")
public class NodeInfoTreeVo extends NodeInfoVo implements Tree<NodeInfoTreeVo> {

  @Schema(description = "子节点集合")
  private List<NodeInfoTreeVo> children;

  @Schema(description = "文件夹下第一层的文件和文件夹总数")
  private Integer fileCount;

  @Schema(description = "文件总数（不含文件夹节点）")
  private Integer fileTotalCount;

  @JsonIgnore
  @Override
  public String getNodeParentId() {
    return getParentId();
  }

  @JsonIgnore
  @Override
  public List<NodeInfoTreeVo> getChildrenNodes() {
    return this.children;
  }

  @Override
  public void setChildrenNodes(List<NodeInfoTreeVo> childrenNodes) {
    this.children = childrenNodes;
  }
}
