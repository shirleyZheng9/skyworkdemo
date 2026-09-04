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
 * 提示词 Entity
 *
 * @author qian.sisheng
 * @since 2024/8/2
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_prompt")
public class PromptEntity extends BaseEntity {
  @DiffId
  @Schema(description = "提示词ID")
  private Long promptId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "PROMPT_CONTENT")
  @Schema(description = "提示词内容")
  private String promptContent;
  @DiffField(name = "PROMPT_TITLE")
  @Schema(description = "提示词标题")
  private String promptTitle;
  @DiffField(name = "CATALOG_ITEM_ID")
  @Schema(description = "目录ID")
  private Long catalogItemId;
}
