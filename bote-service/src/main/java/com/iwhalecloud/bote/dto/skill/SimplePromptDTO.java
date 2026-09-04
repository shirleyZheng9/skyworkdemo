package com.iwhalecloud.bote.dto.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提示词
 *
 * @author qian.sisheng
 * @since 2025-1-22
 */
@Getter
@Setter
@ToString
public class SimplePromptDTO {
  @Schema(description = "提示词ID列表")
  private List<Long> promptIds;
  @Schema(description = "源模型ID")
  private Long sourceModelId;
  @Schema(description = "目标模型ID")
  private Long targetModelId;
  @Schema(description = "提示词内容")
  private String promptContent;
  @Schema(description = "是否覆盖")
  private String coverFlag;
  @Schema(description = "模型ID")
  private Long modelId;
  @Schema(description = "提示词ID")
  private Long promptId;
  @Schema(description = "租户 ID")
  private Long tenantId;
}
