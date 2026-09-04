package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 变更日志实体
 * 迁移对应关系: Go语言entity.ChangeLog
 * - 功能: 存储变更历史信息
 * - 字段定义:
 * * ChangeTarget: TagChangeTargetType - 变更目标
 * * Operation: TagOperationType - 操作类型
 * * BeforeValue: string - 变更前值
 * * AfterValue: string - 变更后值
 * * TargetValue: string - 目标值
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ChangeLog结构体
 * - 使用Java类定义，包含变更日志字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go枚举类型 -> Java枚举
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeLog {

  /**
   * 变更目标
   * 迁移对应关系: Go语言entity.ChangeLog.ChangeTarget (TagChangeTargetType)
   * - 功能: 变更的目标类型
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 标识变更对象
   */
  @JsonProperty("change_target")
  private TagChangeTargetType changeTarget;

  /**
   * 操作类型
   * 迁移对应关系: Go语言entity.ChangeLog.Operation (TagOperationType)
   * - 功能: 变更操作类型
   * - 类型: Go的枚举类型对应Java的枚举
   * - 用途: 标识操作类型
   */
  @JsonProperty("operation")
  private TagOperationType operation;

  /**
   * 变更前值
   * 迁移对应关系: Go语言entity.ChangeLog.BeforeValue (string)
   * - 功能: 变更前的值
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 记录变更前状态
   */
  @JsonProperty("before_value")
  private String beforeValue;

  /**
   * 变更后值
   * 迁移对应关系: Go语言entity.ChangeLog.AfterValue (string)
   * - 功能: 变更后的值
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 记录变更后状态
   */
  @JsonProperty("after_value")
  private String afterValue;

  /**
   * 目标值
   * 迁移对应关系: Go语言entity.ChangeLog.TargetValue (string)
   * - 功能: 变更的目标值
   * - 类型: Go的string类型对应Java的String类型
   * - 用途: 记录变更目标
   */
  @JsonProperty("target_value")
  private String targetValue;
}
