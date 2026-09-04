package com.iwhalecloud.bote.entity.skill;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 技能：SQL Entity
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_sql")
public class SkillSqlEntity extends BaseEntity {
  @DiffId
  @Schema(description = "SQL服务ID")
  private Long serviceId;
  @DiffField(name = "DATA_SOURCE_ID")
  @Schema(description = "数据源ID")
  private Long dataSourceId;
  @DiffField(name = "SERVICE_CODE")
  @Schema(description = "服务编码")
  @Size(max = 50, message = "SQL编码超过限定长度50")
  private String serviceCode;
  @DiffField(name = "SERVICE_NAME")
  @Schema(description = "服务名称")
  @Size(max = 20, message = "SQL名称超过限定长度20")
  private String serviceName;
  @DiffField(name = "SCRIPT_SQL")
  @Schema(description = "脚本")
  private String scriptSql;
  @DiffField(name = "REQ_JSON")
  @Schema(description = "服务入参")
  private String reqJson;
  @DiffField(name = "RESP_JSON")
  @Schema(description = "服务出参")
  private String respJson;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "RESULT_TYPE")
  @Schema(description = "返回结果类型(1: 单值, 2: 单对象, 3: 列表, 4: 分页列表)")
  private String resultType;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
