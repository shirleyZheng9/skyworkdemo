package com.iwhalecloud.bote.entity.base;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 智能体发布 Entity
 *
 * @author auto
 * @since 2025-02-12
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_app_publish")
public class AppPublishEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键")
  private Long publishId;
  @DiffField(name = "TENANT_ID")
  @Schema(description = "租户 ID")
  private Long tenantId;
  @DiffField(name = "BOT_ID")
  @Schema(description = "应用 ID")
  private Long botId;
  @DiffField(name = "BOT_IDS")
  @Schema(description = "应用 ID 集合")
  private String botIds;
  @DiffField(name = "MODE_TYPE")
  @Schema(description = "模式")
  private String modeType;
  @DiffField(name = "TOKEN")
  @Schema(description = "密钥")
  private String token;
  @DiffField(name = "URL")
  @Schema(description = "链接")
  private String url;
  @DiffField(name = "IS_NEW_SESSION")
  @Schema(description = "是否新开会话")
  private String isNewSession;
}
