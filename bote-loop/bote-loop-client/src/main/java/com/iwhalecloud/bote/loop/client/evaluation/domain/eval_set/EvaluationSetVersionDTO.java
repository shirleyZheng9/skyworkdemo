package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测集版本数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测集版本")
public class EvaluationSetVersionDTO implements UserInfoCarrier {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "应用ID")
  private Integer appId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "版本号")
  private String version;

  @Schema(description = "数字版本号")
  private Long versionNum;

  @Schema(description = "版本描述")
  private String description;

  @Schema(description = "评测集Schema")
  private EvaluationSetSchemaDTO evaluationSetSchema;

  @Schema(description = "数据条数")
  private Long itemCount;

  @Schema(description = "系统信息")
  private BaseInfoDTO baseInfo;
}
