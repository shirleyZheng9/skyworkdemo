package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档权限添加请求入参
 *
 * @author Aiqing
 * @since 2025/9/5
 */
@Getter
@Setter
@ToString
public class DocumentPermissionUpdateRO extends TenantBaseRO {

  @Schema(description = "操作类型: ADD_MEMBER-添加成员，UPDATE_MEMBER-更新成员权限，REMOVE_MEMBER-移除成员")
  @NotEmpty(message = "操作类型不能为空")
  private String action;

  @Schema(description = "权限数据")
  private PermissionDTO data;
}
