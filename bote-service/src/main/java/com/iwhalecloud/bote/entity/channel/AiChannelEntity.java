package com.iwhalecloud.bote.entity.channel;

import com.iwhalecloud.bss.litchi.diffc.annotations.DiffField;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffId;
import com.iwhalecloud.bss.litchi.diffc.annotations.DiffNode;
import com.iwhalecloud.bss.litchi.diffc.vo.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 渠道配置表
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
@Getter
@Setter
@ToString(callSuper = true)
@DiffNode(name = "bt_ai_channel")
public class AiChannelEntity extends BaseEntity {
  @DiffId
  @Schema(description = "主键ID")
  private Long id;

  @DiffField(name = "SPACE_ID")
  @Schema(description = "空间ID")
  private Long spaceId;

  @DiffField(name = "BOT_ID")
  @Schema(description = "应用ID")
  private Long botId;

  @DiffField(name = "CHANNEL_TYPE")
  @Schema(description = "渠道类型：DingTalk，Feishu，QQ")
  private String channelType;

  @DiffField(name = "IS_ENABLED")
  @Schema(description = "是否开启：T为开启，F为关闭")
  private String isEnabled;

  @DiffField(name = "CHANNEL_JSON")
  @Schema(description = "渠道配置信息json字符串")
  private String channelJson;
}
