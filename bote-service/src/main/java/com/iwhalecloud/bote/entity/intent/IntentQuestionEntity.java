package com.iwhalecloud.bote.entity.intent;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 意图问句 Entity
 *
 * @author auto
 * @since 2024-12-18
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_INTENTION_QUESTION")
public class IntentQuestionEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long questionId;

  @DiffField(name = "QUESTION")
  @Schema(description = "意图问题")
  private String question;

  @DiffField(name = "ATTRIBUTE")
  @Schema(description = "扩展参数")
  private String attribute;

  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景 ID")
  private Long sceneId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
}
