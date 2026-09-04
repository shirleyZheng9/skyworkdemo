package com.iwhalecloud.bote.doc.module.base.dto;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传基础请求 DTO
 * 包含所有上传请求的公共属性
 *
 * @author yangran
 * @since 2025-08-29
 */
@Getter
@Setter
@Schema(description = "文件上传基础请求")
public class BaseUploadRequest extends TenantBaseRO {

  @Schema(description = "文档库ID")
  private String libraryId;

  @Schema(description = "父文件夹ID")
  private String parentId;

  @Schema(description = "是否转换为在线文档，默认false")
  private String convertToOnline = DocBaseConsts.FALSE;

  @Schema(description = "业务类型")
  private String busiType;
}
