package com.iwhalecloud.bote.dto.publish;

import com.iwhalecloud.bote.entity.publish.PublishGatewayEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线环境维护 DTO
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PublishGatewayDTO extends PublishGatewayEntity {
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "修改人名称")
  private String updatorName;
}
