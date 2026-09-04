package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测集Schema数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测集Schema")
public class EvaluationSetSchemaDTO implements UserInfoCarrier {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "应用ID")
  private Integer appId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "字段Schema列表")
  private List<FieldSchemaDTO> fieldSchemas;

  @Schema(description = "系统信息")
  private BaseInfoDTO baseInfo;
}
