package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bot实体
 * 迁移对应关系: Go语言CozeBot
 * - 功能: Bot数据结构
 * - 字段: botId, botVersion, botInfoType, publishVersion, botName, avatarUrl, description, baseInfo
 * <p>
 * Java实现说明:
 * - 对应Go的CozeBot结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *string -> Java String
 * - Go *BaseInfo -> Java BaseInfo
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot {
  @JsonProperty("bot_id")
  private Long botId;

  @JsonProperty("bot_version")
  private String botVersion;

  @JsonProperty("bot_info_type")
  private BotInfoType botInfoType;

  @JsonProperty("publish_version")
  private String publishVersion;

  @JsonProperty("bot_name")
  private String botName;

  @JsonProperty("avatar_url")
  private String avatarUrl;

  @JsonProperty("description")
  private String description;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}

