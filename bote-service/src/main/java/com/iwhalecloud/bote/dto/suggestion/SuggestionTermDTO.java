package com.iwhalecloud.bote.dto.suggestion;

import com.iwhalecloud.bote.common.enums.SuggestionTermOwnerTypeEnum;
import com.iwhalecloud.bote.common.enums.SuggestionTermTypeEnum;
import com.iwhalecloud.bote.entity.suggestion.SuggestionTermEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 联想术语 DTO
 *
 * @author lizuyin
 * @since 2025-06-09
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SuggestionTermDTO extends SuggestionTermEntity {
  @Schema(description = "租户名称")
  private String tenantName;

  @Schema(description = "智能体名称")
  private String botName;

  @Schema(description = "智能应用名称")
  private String sceneName;

  @Schema(description = "联想话术类型")
  private SuggestionTermTypeEnum suggestionTermType;
  
  @Schema(description = "归属者类型")
  private SuggestionTermOwnerTypeEnum suggestionTermOwnerType;
  
  @Schema(description = "更新者姓名")
  private String updatorName;
}
