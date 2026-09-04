package com.iwhalecloud.bote.entity.bot;

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
 * 机器人辅助功能 Entity
 *
 * @author auto
 * @since 2024-09-14
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_bot_user_experience")
public class BotUserExperienceEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long experienceId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "机器人 ID")
  private Long botId;
  @DiffField(name = "CONTENT")
  @Schema(description = "内容")
  private String content;
  @DiffField(name = "TYPE")
  @Schema(description = "类型 模板：module 指令：point 常用问题：request 术语：term")
  private String type;
  @DiffField(name = "SCENE_ID")
  @Schema(description = "场景ID")
  private Long sceneId;
  @DiffField(name = "TITLE")
  @Schema(description = "标题")
  @Size(max = 20, message = "指令名称超过限定长度20")
  private String title;
  @DiffField(name = "PAGE_CONTENT")
  @Schema(description = "页面内容")
  private String pageContent;
  @DiffField(name = "PAGE_TEMPLATE_JSON")
  @Schema(description = "页面模板JSON")
  private String pageTemplateJson;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "POINT_ACTION")
  @Schema(description = "指令动作")
  private String pointAction;
  @DiffField(name = "OPEN_LINK_TYPE")
  @Schema(description = "记录打开链接方式:打开新窗口 open,弹窗打开 modal,下载 download")
  private String openLinkType;
}
