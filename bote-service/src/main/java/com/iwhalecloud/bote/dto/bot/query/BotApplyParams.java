package com.iwhalecloud.bote.dto.bot.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体发布申请参数
 *
 * @author chen.linfa
 * @since 2025-04-23
 */
@Getter
@Setter
@ToString
public class BotApplyParams {

  @Schema(description = "租户 ID")
  private Long tenantId;

  @Schema(description = "场景 ID")
  private Long sceneId;

  @Schema(description = "发布描述")
  private String applyDesc;

  @Schema(description = "是否发布为新的应用")
  private String isCreate;

  @Schema(description = "是否上架应用")
  private String isPublish;

  @Schema(description = "已有应用 ID 集合")
  private List<Long> botIds;
}
