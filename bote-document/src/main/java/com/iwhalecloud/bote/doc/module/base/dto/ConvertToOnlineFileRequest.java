package com.iwhalecloud.bote.doc.module.base.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 转换为在线文档的
 *
 */
@Getter
@Setter
@Schema(description = "转换为在线文档的")
public class ConvertToOnlineFileRequest extends TenantBaseRO {

  @Schema(description = "文档ID")
  private String documentId;
}
