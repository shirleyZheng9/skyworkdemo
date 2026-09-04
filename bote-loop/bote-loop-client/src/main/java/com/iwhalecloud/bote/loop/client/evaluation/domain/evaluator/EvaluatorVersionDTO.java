package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import com.iwhalecloud.bote.loop.client.common.userinfo.UserInfoCarrier;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评测器版本数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评测器版本数据传输对象")
public class EvaluatorVersionDTO implements UserInfoCarrier {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;

  @Schema(description = "评测器内容")
  private EvaluatorContentDTO evaluatorContent;
}
