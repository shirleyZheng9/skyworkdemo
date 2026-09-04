package com.iwhalecloud.bote.doc.module.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档的权限设置信息
 *
 * @author Aiqing
 * @since 2025/9/5
 */
@Getter
@Setter
@ToString
public class DocumentPermissionSetDTO extends PermissionDTO {

  @Schema(description = "用户头像")
  private String avatar;
  @Schema(description = "授权对象名称")
  private String subjectName;
  @Schema(description = "授权人")
  private Long grantedBy;
  @Schema(description = "授权人名称")
  private String grantedByName;
  @Schema(description = "是否可修改")
  private Boolean editable;
  @Schema(description = "是否可移除")
  private Boolean removable;
  @Schema(description = "是否是所有者")
  private Boolean owner;
  @Schema(description = "是否是继承文档库")
  private Boolean inherit;
  @Schema(description = "继承的上级权限")
  private String inheritPermission;

  @Schema(description = "授权描述")
  private String grantDesc;

  /**
   * 是否继承自文档库
   */
  @Schema(hidden = true)
  @JsonIgnore
  private Boolean inheritFromLibrary;

  @Schema(description = "可分配的角色集合")
  private List<String> roleSet;
}
