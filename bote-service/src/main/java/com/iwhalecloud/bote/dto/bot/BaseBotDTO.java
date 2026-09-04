package com.iwhalecloud.bote.dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 机器人基础信息
 *
 * @author qian.sisheng
 * @since 2025-06-12
 */
@Getter
@Setter
@Schema(description = "机器人基础信息")
@JsonInclude(Include.NON_NULL)
public class BaseBotDTO {
  @Schema(description = "机器人ID")
  private Long botId;
  @Schema(description = "机器人名称")
  private String botName;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "租户名称")
  private String tenantName;
  @Schema(description = "创建时间")
  private Date createdTime;
  @Schema(description = "创建人名称")
  private String creatorName;
  @Schema(description = "机器人场景列表")
  private List<SimpleBotSceneRelDTO> scenes;
}
