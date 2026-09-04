package com.iwhalecloud.bote.dto.bot.query;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author auto
 * @since 2024-09-19
 */
@Getter
@Setter
@ToString
@Schema(description = "副驾指令参数")
public class CopilotPointParams {
  @Schema(description = "指令编码")
  private String pointCode;
  @Schema(description = "业务参数")
  private Map<String, Object> params;
  @Schema(description = "外系统 ID")
  private Long extSystemId;
  @Schema(description = "是否忽略会话")
  private String ignoreSession;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "当前的会话 ID")
  private Long sessionId;
}
