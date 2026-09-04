package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内置Coze Bot评估对象实体
 * 迁移对应关系: Go语言TargetBuiltinCozeBot
 * - 功能: 内置Coze Bot评估对象数据结构
 * - 字段: botId, botKey
 * <p>
 * Java实现说明:
 * - 对应Go的TargetBuiltinCozeBot结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetBuiltinCozeBot {
  @JsonProperty("bot_id")
  private Long botId;

  @JsonProperty("bot_key")
  private String botKey;
}
