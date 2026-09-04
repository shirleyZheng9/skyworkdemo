package com.iwhalecloud.bote.dto.base;

import com.iwhalecloud.bote.entity.base.LabelObjectRelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 关联标签 DTO
 *
 * @author auto
 * @since 2024-09-13
 */
@Getter
@Setter
@ToString(callSuper = true)
public class LabelObjectRelDTO extends LabelObjectRelEntity {
  @Schema(description = "标签名称")
  private String labelName;
  @Schema(description = "标签颜色")
  private String labelColor;
  @Schema(description = "标签归属租户 ID")
  private Long labelTenantId;
}
