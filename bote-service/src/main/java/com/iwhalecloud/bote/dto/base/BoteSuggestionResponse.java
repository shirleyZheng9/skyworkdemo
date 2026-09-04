package com.iwhalecloud.bote.dto.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Elasticsearch建议查询响应
 *
 * @author lizuyin
 * @since 2025-06-28
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "Elasticsearch建议查询响应")
public class BoteSuggestionResponse {

  @Schema(description = "建议文本")
  private String text;

  @Schema(description = "术语ID")
  private String termId;

  @Schema(description = "术语内容")
  private String termContent;

  @Schema(description = "所有者类型")
  private String ownerType;

  @Schema(description = "所有者ID")
  private String ownerId;

  @Schema(description = "功能类别")
  private String type;

  @Schema(description = "补全内容")
  private String termCompletion;

  @Schema(description = "命中词")
  private String hitWord;

  @Schema(description = "高亮内容")
  private String highlight;

  @Schema(description = "得分")
  private Double score;
}
