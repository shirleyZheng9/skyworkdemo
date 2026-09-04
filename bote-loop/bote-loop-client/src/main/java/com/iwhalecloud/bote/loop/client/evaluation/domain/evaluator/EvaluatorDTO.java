package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测器数据传输对象")
public class EvaluatorDTO implements UserInfoCarrier {

  @Schema(description = "评测器ID")
  private Long evaluatorId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测器类型")
  private EvaluatorTypeDTO evaluatorType;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "草稿是否已提交")
  private Boolean draftSubmitted;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;

  @Schema(description = "当前版本")
  private EvaluatorVersionDTO currentVersion;

  @Schema(description = "最新版本")
  private String latestVersion;

  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
