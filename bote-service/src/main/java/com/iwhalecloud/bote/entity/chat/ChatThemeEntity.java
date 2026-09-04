package com.iwhalecloud.bote.entity.chat;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Size;

/**
 * 聊天主题 Entity
 *
 * @author tingyun.wang
 * @since 2025-07-23
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_chat_theme")
public class ChatThemeEntity extends BaseEntity {

  @DiffId
  @Schema(description = "主键")
  private Long id;

  @DiffField(name = "THEME_ID")
  @Schema(description = "主题ID")
  private Long themeId;

  @DiffField(name = "THEME_NAME")
  @Schema(description = "主题名称")
  @Size(max = 255, message = "主题名称超过限定长度255")
  private String themeName;

  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户ID")
  private Long tenantId;

  @DiffField(name = "BOT_ID")
  @Schema(description = "智能应用ID")
  private Long botId;

  @DiffField(name = "THEME_SCOPE")
  @Schema(description = "级别（app:应用级别，tenant:租户级别，platform：平台级别）")
  @Size(max = 64, message = "级别超过限定长度64")
  private String themeScope;

  @DiffField(name = "THEME_JSON")
  @Schema(description = "主题JSON配置")
  private String themeJson;

  @DiffField(name = "THEME_ICON")
  @Schema(description = "主题图片")
  private String themeIcon;

  @DiffField(name = "THEME_TAG")
  @Schema(description = "主题标签")
  private String themeTag;

  @DiffField(name = "IS_USING")
  @Schema(description = "是否使用中（T/F）")
  private String isUsing;

  @DiffField(name = "REPLY_THEME_ID")
  @Schema(description = "回复主题ID")
  private Long replyThemeId;
}
