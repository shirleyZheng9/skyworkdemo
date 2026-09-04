package com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target;

import com.iwhalecloud.bote.loop.client.evaluation.domain.common.BaseInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "工作流数据传输对象")
public class WorkflowDTO {

  @Schema(description = "ID")
  private String id;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "头像URL")
  private String avatarUrl;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "基础信息")
  private BaseInfoDTO baseInfo;
}

