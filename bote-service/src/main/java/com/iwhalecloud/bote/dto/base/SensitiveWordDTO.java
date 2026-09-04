package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.SensitiveWordEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 敏感词
 *
 * @author bianjp
 * @since 2025-01-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "敏感词")
public class SensitiveWordDTO extends SensitiveWordEntity {
  @Schema(description = "更新人")
  private String updatorName;
}
