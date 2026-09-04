package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集最后操作实体
 * 迁移对应关系: Go语言entity.DatasetLastOperation
 * - 功能: 存储数据集的最后操作信息
 * - 字段定义:
 * * OP: DatasetOpType - 操作类型
 * * LastOperatedAt: time.Time - 最后操作时间
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetLastOperation结构体
 * - 使用Java类定义，包含最后操作字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go time.Time -> Java LocalDateTime
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetLastOperation {

  @JsonProperty("op")
  private DatasetOpType op;

  @JsonProperty("last_operated_at")
  private LocalDateTime lastOperatedAt;
}
