package com.iwhalecloud.bote.dto.bot;

import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 百应平台资源分隔DTO（智能应用与智能体分开）
 *
 * @author lizuyin
 * @since 2025-11-27
 */
@Getter
@Setter
@ToString
@Schema(description = "百应平台资源分隔DTO")
public class BeyondSeparatedResourceDTO {
  @Schema(description = "智能应用分页列表")
  private PageInfo<BotDTO> botPageInfo;
  @Schema(description = "智能体分页列表")
  private PageInfo<BotSceneDTO> scenePageInfo;
}

