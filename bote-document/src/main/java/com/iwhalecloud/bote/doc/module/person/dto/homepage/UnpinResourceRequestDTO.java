package com.iwhalecloud.bote.doc.module.person.dto.homepage;

import com.iwhalecloud.bote.doc.common.model.TenantBaseRO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 取消置顶资源请求参数
 *
 * @author lizuyin
 * @since 2025-01-18
 */
@Getter
@Setter
@ToString
@Schema(description = "取消置顶资源请求参数")
public class UnpinResourceRequestDTO extends TenantBaseRO {

  @Schema(description = "目标资源ID")
  @NotNull(message = "目标资源ID不能为空")
  private String targetId;

  @Schema(description = "目标类型")
  @NotNull(message = "目标类型不能为空")
  private String targetType;
}
