package com.iwhalecloud.bote.doc.module.knowledge.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "知识库权限设置请求")
public class KnowledgeBasePermissionRequestDTO extends TenantBaseRO {

  @NotBlank(message = "操作类型不能为空")
  @Schema(description = "操作类型：ADD_MEMBER-添加成员，UPDATE_MEMBER-更新成员权限，REMOVE_MEMBER-移除成员")
  private String action;

//  @Schema(description = "租户标识")
//  private Long tenantId;

  @NotNull(message = "权限数据不能为空")
  @Schema(description = "权限数据")
  private KnowledgeBasePermissionDataDTO data;

  @Getter
  @Setter
  @ToString
  @Schema(description = "文档库权限数据")
  public static class KnowledgeBasePermissionDataDTO {

    @NotBlank(message = "主体类型不能为空")
    @Schema(description = "主体类型：USER-用户，ORG-部门，ROLE-角色，GROUP-分组")
    private String subjectType;

    @NotNull(message = "主体ID不能为空")
    @Schema(description = "主体ID（用户ID、部门ID、角色ID、分组ID）")
    private Long subjectId;

    @Schema(description = "权限类型：MANAGE-可管理，EDIT-可编辑，READ-只读")
    private String permissionType;
  }
}
