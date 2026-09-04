package com.iwhalecloud.bote.doc.module.person.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.iwhalecloud.bote.doc.common.support.tree.SortableNode;
import com.iwhalecloud.bote.doc.common.support.tree.Tree;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UserInfo;
import com.iwhalecloud.bote.doc.module.document.entity.DcDocumentEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 我的文档
 *
 * @author yangran
 * @since 2025-08-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "我的文档")
public class MyDocumentDTO extends DcDocumentEntity implements SortableNode, Tree<MyDocumentDTO> {
  @Schema(description = "文档编码")
  private String documentCode;

  @Schema(description = "文件图标")
  private String fileIcon;

  @Schema(description = "创建人信息")
  private UserInfo creator;

  @Schema(description = "是否收藏")
  private Boolean isFavorite;

  @Schema(description = "是否置顶")
  private Boolean isPinned;

  @Schema(description = "权限")
  private String permissions;

  @Schema(description = "是否有子节点")
  private Boolean hasChildren;

  @Schema(description = "是否是我的文档库文档：T为是")
  private String myDoc;

  @Schema(description = "子节点")
  private List<MyDocumentDTO> children;

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
  public List<MyDocumentDTO> getChildrenNodes() {
    return this.getChildren();
  }

  @Override
  public void setChildrenNodes(List<MyDocumentDTO> childrenNodes) {
    this.setChildren(childrenNodes);
  }

}

