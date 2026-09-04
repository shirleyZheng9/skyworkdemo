package com.iwhalecloud.bote.doc.module.knowledge.entity;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 博特模型与weknora模型关联 Entity
 *
 * @author qian.sisheng
 * @since 2026-04-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_weknora_model_rel")
public class WeknoraModelRelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long relId;
  @DiffField(name = "BOTE_MODEL_ID")
  @Schema(description = "bote模型id")
  private Long boteModelId;
  @DiffField(name = "WEKNORA_MODEL_ID")
  @Schema(description = "weknora模型id")
  private String weknoraModelId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户id")
  private Long tenantId;
}
