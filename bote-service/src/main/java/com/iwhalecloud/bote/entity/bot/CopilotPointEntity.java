package com.iwhalecloud.bote.entity.bot;

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
 * 副驾指令 Entity
 *
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_copilot_point")
public class CopilotPointEntity extends BaseEntity {
  @DiffId
  @Schema(description = "指令 ID")
  private Long pointId;
  @DiffField(name = "POINT_NAME")
  @Schema(description = "指令名称")
  @Size(max = 20, message = "指令名称超过限定长度20")
  private String pointName;
  @DiffField(name = "POINT_CODE")
  @Schema(description = "指令编码")
  private String pointCode;
  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景 ID")
  private Long sceneId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;
  @DiffField(name = "REQUEST_JSON")
  @Schema(description = "模板参数")
  private String requestJson;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录")
  private Long catalogItemId;
  @DiffField(name = "IS_NEW_SESSION")
  @Schema(description = "是否开启新的会话")
  private String isNewSession;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
