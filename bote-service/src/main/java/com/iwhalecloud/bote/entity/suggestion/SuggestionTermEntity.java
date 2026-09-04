package com.iwhalecloud.bote.entity.suggestion;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 联想术语 Entity
 *
 * @author lizuyin
 * @since 2025-06-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_suggestion_term")
public class SuggestionTermEntity extends BaseEntity {

  @DiffId
  @Schema(description = "术语ID")
  private Long termId;

  @DiffField(name = "TERM_CONTENT")
  @Schema(description = "联想术语内容")
  private String termContent;

  @DiffField(name = "TERM_TYPE")
  @Schema(description = "联想话术类型")
  private String termType;

  @DiffField(name = "OWNER_TYPE")
  @Schema(description = "归属者类型")
  private String ownerType;

  @DiffField(name = "BOT_ID")
  @Schema(description = "智能体ID")
  private Long botId;

  @DiffField(name = "SCENE_ID")
  @Schema(description = "智能应用ID")
  private Long sceneId;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
}
