package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.entity.bot.PlatBotInfoEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 语料基本信息 DTO
 *
 * @author auto
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString(callSuper = true)
public class PlatBotInfoDTO extends PlatBotInfoEntity {
  @Schema(description = "修改人名称")
  private String updatorName;
  @Schema(description = "目录名称")
  private String catalogName;
  @Schema(description = "创建人名称")
  private String creatorName;
}
