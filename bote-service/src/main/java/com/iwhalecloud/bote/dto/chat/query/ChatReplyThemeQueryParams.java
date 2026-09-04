package com.iwhalecloud.bote.dto.chat.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回复消息主题查询参数
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "回复消息主题查询参数")
public class ChatReplyThemeQueryParams extends PagingQueryParams {
  @Schema(description = "模糊查询")
  private String searchContent;
  @Schema(description = "租户ID")
  private Long tenantId;
}
