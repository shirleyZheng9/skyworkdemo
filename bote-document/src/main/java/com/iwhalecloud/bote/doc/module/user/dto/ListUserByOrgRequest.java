package com.iwhalecloud.bote.doc.module.user.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 组织成员列表请求参数
 *
 * @author lizuyin
 * @since 2025-09-06
 */
@Getter
@Setter
@ToString
public class ListUserByOrgRequest extends TenantBaseRO {
  @Schema(description = "组织ID")
  private Long orgId;
}
