package com.iwhalecloud.bote.entity.bot;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 场景变量
 *
 * @author chen.linfa
 * @since 2024-08-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_scene_param")
public class BotSceneParamEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long paramId;
  @Schema(description = "场景 ID")
  @DiffField(name = "SCENE_ID", parent = true)
  private Long sceneId;
  @Schema(description = "变量信息")
  @DiffField(name = "VARIABLE_JSON")
  private String variableJson;
  @Schema(description = "入参信息")
  @DiffField(name = "REQUEST_JSON")
  private String requestJson;
  @Schema(description = "出参信息")
  @DiffField(name = "RESPONSE_JSON")
  private String responseJson;
  @Schema(description = "租户 ID")
  @DiffField(name = "TENANT_ID")
  private Long tenantId;
}
