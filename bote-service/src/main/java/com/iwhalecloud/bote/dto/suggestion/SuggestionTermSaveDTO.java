package com.iwhalecloud.bote.dto.suggestion;

import com.iwhalecloud.bote.common.enums.SuggestionTermOwnerTypeEnum;
import com.iwhalecloud.bote.common.enums.SuggestionTermTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 联想术语新增 DTO
 *
 * @author lizuyin
 * @since 2025-06-10
 */
@Getter
@Setter
@ToString
@Schema(description = "联想术语新增参数")
public class SuggestionTermSaveDTO {
  @Schema(description = "术语ID")
  private Long termId;
  @Schema(description = "联想术语内容")
  private String termContent;
  @Schema(description = "智能体ID")
  private Long botId;
  @Schema(description = "智能应用ID")
  private Long sceneId;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "备注")
  private String remark;
  @Schema(description = "联想话术类型")
  private SuggestionTermTypeEnum termType;
  @Schema(description = "归属者类型")
  private SuggestionTermOwnerTypeEnum ownerType;
}
