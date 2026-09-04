package com.iwhalecloud.bote.entity.publish;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 在线环境维护 Entity
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_publish_gateway")
public class PublishGatewayEntity extends BaseEntity {

  @DiffId
  @Schema(description = "网关ID")
  private Long gatewayId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "GATEWAY_NAME")
  @Schema(description = "网关名称")
  private String gatewayName;

  @DiffField(name = "GATEWAY_ICON")
  @Schema(description = "网关图标")
  private String gatewayIcon;

  @DiffField(name = "GATEWAY_URL")
  @Schema(description = "网关URL")
  private String gatewayUrl;

  @DiffField(name = "GATEWAY_TOKEN")
  @Schema(description = "网关Token")
  private String gatewayToken;
}
