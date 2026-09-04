package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测目标数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测目标数据传输对象")
public class EvalTargetDTO {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "来源目标ID")
  private String sourceTargetId;

  @Schema(description = "评测目标类型")
  private EvalTargetTypeDTO evalTargetType;

  @Schema(description = "评测目标版本")
  private EvalTargetVersionDTO evalTargetVersion;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;
}
