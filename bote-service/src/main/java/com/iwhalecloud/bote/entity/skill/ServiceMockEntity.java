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
 * 模拟响应报文 Entity
 *
 * @author auto
 * @since 2024-12-17
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_service_mock")
public class ServiceMockEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long rspId;
  @DiffField(name = "MOCK_NAME")
  @Schema(description = "模拟响应报文名称")
  private String mockName;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "SERVICE_ID", parent = true)
  @Schema(description = "服务ID")
  private Long serviceId;
  @DiffField(name = "RSP_JSON")
  @Schema(description = "响应报文")
  private String rspJson;
  @DiffField(name = "CONDITION_JSON")
  @Schema(description = "条件")
  private String conditionJson;
}
