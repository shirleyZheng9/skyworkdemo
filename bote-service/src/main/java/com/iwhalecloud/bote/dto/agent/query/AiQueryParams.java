package com.iwhalecloud.bote.dto.agent.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * BoteClaw 查询参数
 *
 * @author linmengfan
 * @since 2026-03-05
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "BoteClaw查询参数")
public class AiQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "关键字(模糊搜索名称、编码)")
  private String keyword;
  @Schema(description = "企业空间ID")
  private Long spaceId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "模型类型")
  private String modelType;
  @Schema(description = "启用的技能 ID", example = "T")
  private String enable;
  @Schema(description = "提示词内容")
  private String promptContent;
}
