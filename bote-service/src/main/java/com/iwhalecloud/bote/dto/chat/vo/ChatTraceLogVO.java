package com.iwhalecloud.bote.dto.chat.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话日志
 *
 * @author bianjp
 * @since 2025-01-08
 */
@Getter
@Setter
@ToString
@Schema(description = "会话日志")
public class ChatTraceLogVO {
  @Schema(description = "日志 ID")
  private Long logId;
  @Schema(description = "步骤名称")
  private String stepName;
  @Schema(description = "入参")
  private Map<String, Object> input;
  @Schema(description = "出参")
  private Object output;
  @Schema(description = "日志内容")
  private String logContent;
  @Schema(description = "耗时(ms)")
  private Integer timeSpent;
  @Schema(description = "状态(S: 成功, F: 失败)")
  private String logStatus;

  // 查询数据库使用，不返回给前端
  @JsonIgnore
  private String inputJson;
  @JsonIgnore
  private String outputJson;
}
