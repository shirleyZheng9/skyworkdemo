package com.iwhalecloud.bote.doc.module.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 文档权限更新结果
 *
 * @author Aiqing
 * @since 2025/11/5
 */
@Getter
@Setter
@ToString
public class DocumentPermissionUpdateResultDTO {

  @Schema(description = "成功记录数")
  private Integer successCount;
}
