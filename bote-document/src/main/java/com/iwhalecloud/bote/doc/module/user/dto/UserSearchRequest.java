package com.iwhalecloud.bote.doc.module.user.dto;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户搜索请求参数
 *
 * @author lizuyin
 * @since 2025-09-08
 */
@Getter
@Setter
@ToString
public class UserSearchRequest extends TenantBaseRO {

  @Schema(description = "搜索关键词（用户名或真实姓名）")
  private String keyword;
}
