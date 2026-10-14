package com.iwhalecloud.bote.dto.portal.tiangong;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 天工项目成员
 *
 * @author zhengxueli
 * @since 2024-09-23
 * <p>
 * 只承载天工成员的外部标识及展示信息，不接受系统编码或角色，防止调用方越权指定业务身份。
 */
@Getter
@Setter
public class TiangongProjectUserItem {
  @Schema(description = "天工外部用户 ID")
  private String extUserId;
  @Schema(description = "展示名称")
  private String displayName;
  @Schema(description = "手机号")
  private String phoneNo;
}
