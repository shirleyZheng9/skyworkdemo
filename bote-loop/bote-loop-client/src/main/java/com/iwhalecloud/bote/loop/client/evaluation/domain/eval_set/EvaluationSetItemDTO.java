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
 * 评测集项目数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测集项目")
public class EvaluationSetItemDTO implements UserInfoCarrier {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "应用ID")
  private Integer appId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "Schema ID")
  private Long schemaId;

  @Schema(description = "数据项ID")
  private Long itemId;

  @Schema(description = "数据项Key")
  private String itemKey;

  @Schema(description = "轮次数据内容")
  private List<TurnDTO> turns;

  @Schema(description = "系统信息")
  private BaseInfoDTO baseInfo;
}
