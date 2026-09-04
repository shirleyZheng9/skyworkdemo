package com.iwhalecloud.bote.dto.chat.query;

import com.iwhalecloud.bote.dto.base.query.PagingQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 聊天主题查询参数
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(description = "聊天主题查询参数")
public class ChatThemeQueryParams extends PagingQueryParams {

  @Schema(description = "租户ID")
  private Long tenantId;

  @Schema(description = "智能应用ID")
  private Long botId;

  @Schema(description = "级别（app:应用级别，tenant:租户级别，platform：平台级别）")
  private String themeScope;

  @Schema(description = "主题名称（模糊搜索）")
  private String themeName;

  @Schema(description = "是否使用中（T/F）")
  private String isUsing;

}
