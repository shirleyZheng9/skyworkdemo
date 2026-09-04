package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 检查敏感词请求
 *
 * @author bianjp
 * @since 2025-01-15
 */
@Getter
@Setter
@ToString
@Schema(description = "检查敏感词请求")
public class CheckSensitiveWordRequest {
  @Schema(description = "文本")
  private String text;
}
