package com.iwhalecloud.bote.dto.chat.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话查询参数
 *
 * @author chen.linfa
 * @since 2025-09-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "会话查询参数")
public class ChatSessionQueryParams extends PagingQueryParams {

  @Schema(description = "空间 ID")
  private Long spaceId;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "应用 ID")
  private Long botId;
  @Schema(description = "外系统 ID")
  private Long extSystemId;
  @Schema(description = "搜索内容")
  private String searchContent;
  @Schema(description = "动作类型")
  private String actionType;
  @Schema(description = "是否调试")
  private Boolean isTest;

  @Schema(description = "用户 ID")
  private Long userId;
  @Schema(description = "是否只查询标记的应用")
  private Boolean isMark;
  @Schema(description = "是否例外完成的应用")
  private Boolean isClose;
  @Schema(description = "是否运行态")
  private Boolean isRuntime;
}
