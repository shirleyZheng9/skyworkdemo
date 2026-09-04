package com.iwhalecloud.bote.entity.chat;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 回复消息主题 Entity
 *
 * @author qian.sisheng
 * @since 2025-12-02
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "BT_CHAT_REPLY_THEME")
public class ChatReplyThemeEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long replyThemeId;
  @DiffField(name = "REPLY_THEME_NAME")
  @Schema(description = "回复主题名称")
  private String replyThemeName;
  @DiffField(name = "REPLY_THEME_CODE")
  @Schema(description = "回复主题编码")
  private String replyThemeCode;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;
  @DiffField(name = "THEME_JSON")
  @Schema(description = "回复主题内容")
  private String themeJson;
  @DiffField(name = "CONFIG_JSON")
  @Schema(description = "配置变量数据")
  private String configJson;
}
