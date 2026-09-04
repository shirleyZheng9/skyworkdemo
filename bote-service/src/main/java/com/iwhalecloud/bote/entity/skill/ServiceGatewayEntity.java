package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 技能：API 网关 Entity
 *
 * @author auto
 * @since 2024-09-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_service_gateway")
public class ServiceGatewayEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long gatewayId;
  @DiffField(name = "PLATFORM_ID", parent = true)
  @Schema(description = "平台 ID")
  private Long platformId;
  @DiffField(name = "ENV_CODE")
  @Schema(description = "环境编码")
  private String envCode;
  @DiffField(name = "URL")
  @Schema(description = "环境地址")
  private String url;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "HEADER_JSON")
  @Schema(description = "头部信息")
  private String headerJson;
}
