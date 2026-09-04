package com.iwhalecloud.bote.loop.client.evaluation.domain.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户信息数据传输对象")
public class UserInfoDTO {

  @Schema(description = "名称")
  private String name;

  @Schema(description = "英文名称")
  private String enName;

  @Schema(description = "头像URL")
  private String avatarUrl;

  @Schema(description = "头像缩略图")
  private String avatarThumb;

  @Schema(description = "Open ID")
  private String openId;

  @Schema(description = "Union ID")
  private String unionId;

  @Schema(description = "用户ID")
  private String userId;

  @Schema(description = "邮箱")
  private String email;
}
