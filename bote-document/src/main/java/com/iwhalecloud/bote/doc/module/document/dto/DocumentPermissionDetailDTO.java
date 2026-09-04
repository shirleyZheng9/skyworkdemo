package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档权限的配置详情
 *
 * @author Aiqing
 * @since 2025/10/15
 */
@Getter
@Setter
@ToString
public class DocumentPermissionDetailDTO {

  @Schema(description = "权限模式， 0：继承模式，1：独立模式")
  private Integer permissionMode;
  @Schema(description = "权限配置记录")
  private List<DocumentPermissionSetDTO> permissions;
}
