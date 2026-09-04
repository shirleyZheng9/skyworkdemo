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
 * 技能：页面函数 Entity
 *
 * @author auto
 * @since 2024-09-15
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_page_func")
public class SkillPageFuncEntity extends BaseEntity {
  @DiffId
  @Schema(description = "函数ID")
  private Long pageFuncId;
  @DiffField(name = "FUNC_CODE")
  @Schema(description = "函数编码")
  @Size(max = 50, message = "页面函数编码超过限定长度50")
  private String funcCode;
  @DiffField(name = "FUNC_NAME")
  @Schema(description = "函数名称")
  @Size(max = 20, message = "页面函数名称超过限定长度20")
  private String funcName;
  @DiffField(name = "REQ_JSON")
  @Schema(description = "服务入参")
  private String reqJson;
  @DiffField(name = "RESP_JSON")
  @Schema(description = "服务出参")
  private String respJson;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
  @Schema(description = "函数类型")
  @DiffField(name = "FUNC_TYPE")
  private String funcType;
  @Schema(description = "终端类型: IOS Android PC")
  private String terminalType;
}
