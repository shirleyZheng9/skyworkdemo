package com.iwhalecloud.bote.dto.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 资源取消发布请求
 *
 * @author bianjp
 * @since 2025-09-17
 */
@Getter
@Setter
@ToString
@Schema(description = "资源取消发布请求")
public class ResourceUnpublishRequest {
  @NotNull
  @Schema(description = "租户 ID", requiredMode = RequiredMode.REQUIRED)
  private Long tenantId;
  
  @Schema(description = "发布ID列表")
  private List<Long> recordIdList;
}
