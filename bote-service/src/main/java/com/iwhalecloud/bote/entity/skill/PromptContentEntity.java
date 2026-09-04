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
 * 提示词内容
 *
 * @author qian.sisheng
 * @since 2025-2-13
 */

@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_prompt_content")
public class PromptContentEntity extends BaseEntity {
  @DiffId
  @Schema(description = "内容ID")
  private Long contentId;
  @DiffField(name = "PROMPT_ID", parent = true)
  @Schema(description = "提示词ID")
  private Long promptId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "MODEL_ID")
  @Schema(description = "模型ID")
  private Long modelId;
  @DiffField(name = "PROMPT_CONTENT")
  @Schema(description = "提示词内容")
  private String promptContent;
}
