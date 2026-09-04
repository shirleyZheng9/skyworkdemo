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
 * 技能：插件 Entity
 *
 * @author auto
 * @since 2024-09-21
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_skill_plugin")
public class SkillPluginEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long apiId;
  @DiffField(name = "MODEL_ID")
  @Schema(description = "模型ID")
  private Long modelId;
  @DiffField(name = "API_CODE")
  @Schema(description = "API编码")
  @Size(max = 50, message = "编码超过限定长度50")
  private String apiCode;
  @DiffField(name = "API_NAME")
  @Schema(description = "API名称")
  @Size(max = 20, message = "名称超过限定长度20")
  private String apiName;
  @DiffField(name = "PRE_SCRIPT")
  @Schema(description = "前置脚本内容")
  private String preScript;
  @DiffField(name = "POST_SCRIPT")
  @Schema(description = "后置脚本内容")
  private String postScript;
  @DiffField(name = "SCRIPT_TYPE")
  @Schema(description = "脚本类型")
  private String scriptType;
  @DiffField(name = "REQ_JSON")
  @Schema(description = "请求参数")
  private String reqJson;
  @DiffField(name = "PROMPT_ID")
  @Schema(description = "提示词 ID")
  private Long promptId;
  @DiffField(name = "PROMPT_CONTENT")
  @Schema(description = "提示词内容")
  private String promptContent;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "租户ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
