package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 调整首页置顶排序请求参数
 *
 * @author lizuyin
 * @since 2025-08-15
 */
@Getter
@Setter
@ToString
@Schema(description = "调整首页置顶排序请求参数")
public class ReorderPinsRequestDTO extends TenantBaseRO {

  @Schema(description = "被拖拽的置顶项ID")
  @NotNull(message = "被拖拽的置顶项ID不能为空")
  private Long pinId;

  @Schema(description = "目标前驱项ID，如果拖拽到第一位则传null")
  private Long prevPinId;
}
