package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户信息
 *
 * @author yangran
 * @since 2025-01-06
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "用户信息")
public class UserInfo {
  @Schema(description = "用户ID")
  private Long userId;
  @Schema(description = "用户名")
  private String username;
}
