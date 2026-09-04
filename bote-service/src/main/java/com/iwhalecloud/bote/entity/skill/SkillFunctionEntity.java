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
 * 技能：服务函数 Entity
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_function")
public class SkillFunctionEntity extends BaseEntity {
  @DiffId
  @Schema(description = "函数ID")
  private Long funcId;
  @DiffField(name = "FUNC_TYPE")
  @Schema(description = "函数类型")
  private String funcType;
  @DiffField(name = "FUNC_CODE")
  @Schema(description = "函数编码")
  @Size(max = 50, message = "函数编码超过限定长度50")
  private String funcCode;
  @DiffField(name = "FUNC_NAME")
  @Schema(description = "函数名称")
  @Size(max = 20, message = "函数名称超过限定长度20")
  private String funcName;
  @DiffField(name = "SCRIPT_JSON")
  @Schema(description = "函数内容")
  private String scriptJson;
  @DiffField(name = "REQ_JSON")
  @Schema(description = "服务入参")
  private String reqJson;
  @DiffField(name = "RESP_JSON")
  @Schema(description = "服务出参")
  private String respJson;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "PY_PACKAGE")
  @Schema(description = "python包列表")
  private String pyPackage;
}
