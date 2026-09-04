package com.iwhalecloud.bote.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单模型微调 DTO
 *
 * @author auto
 * @since 2025-02-17
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SimpleIntentFinetuneDTO {

  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "模型名称")
  private String baseModelName;

  @Schema(description = "模型 ID")
  private Long modelId;

  @Schema(description = "阈值")
  private BigDecimal threshold;

  @Schema(description = "上下文长度")
  private Integer maxLength;
}
