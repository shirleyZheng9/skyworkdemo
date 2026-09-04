package com.iwhalecloud.bote.entity.agent;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 新增表记录启用模型 Entity
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_AI_MODEL")
public class AiModelEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;
  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用ID")
  private Long botId;
  @DiffField(name = "MODEL_ID")
  @Schema(description = "模型ID")
  private Long modelId;
  @DiffField(name = "MODEL_TYPE")
  @Schema(description = "模型类型")
  private String modelType;
}
