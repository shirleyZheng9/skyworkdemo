package com.iwhalecloud.bote.doc.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 请求参数的基础类
 *
 * @author Aiqing
 * @since 2025/8/18
 */
@Getter
@Setter
@ToString
public class TenantBaseRO {

  @Schema(description = "租户ID")
  protected Long tenantId;

  @Schema(description = "企业空间ID")
  protected Long spaceId;

  @Schema(description = "ai门户标识值为：portal，为空或者其他值为开发者门户")
  protected String platform;
}
