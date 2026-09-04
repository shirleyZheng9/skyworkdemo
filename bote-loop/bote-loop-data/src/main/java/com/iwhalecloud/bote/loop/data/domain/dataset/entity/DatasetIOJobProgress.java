package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据集IO任务进度实体
 * 迁移对应关系: Go语言entity.DatasetIOJobProgress
 * - 功能: 存储数据集IO任务进度信息
 * - 字段定义:
 * * Total: *int64 - 总量
 * * Processed: *int64 - 已处理数量
 * * Added: *int64 - 已成功处理的数量
 * * Name: *string - 子任务名称
 * * SubProgresses: []*DatasetIOJobProgress - 子任务的进度
 * <p>
 * Java实现说明:
 * - 对应Go的entity.DatasetIOJobProgress结构体
 * - 使用Java类定义，包含任务进度字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 * - Go切片 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetIOJobProgress {

  @JsonProperty("total")
  private Long total;

  @JsonProperty("processed")
  private Long processed;

  @JsonProperty("added")
  private Long added;

  @JsonProperty("name")
  private String name;

  @JsonProperty("sub_progresses")
  private List<DatasetIOJobProgress> subProgresses;
}
