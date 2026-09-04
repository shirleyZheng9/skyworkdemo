package com.iwhalecloud.bote.doc.module.document.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档权限批量更新入参
 *
 * @author Aiqing
 * @since 2025/9/5
 */
@Getter
@Setter
@ToString
public class DocumentPermissionBatchUpdateRO extends TenantBaseRO {

  @Schema(description = "操作类型")
  @NotEmpty(message = "操作类型不能为空")
  private String action;
  @Schema(description = "权限数据")
  @NotEmpty(message = "权限数据不能为空")
  private List<PermissionDTO> members;
}
