package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目错误组实体
 * 迁移对应关系: Go语言entity.ItemErrorGroup
 * - 功能: 存储项目错误组信息
 * - 字段定义:
 * * Type: *ItemErrorType - 错误类型
 * * Summary: *string - 错误摘要
 * * ErrorCount: *int32 - 错误条数
 * * Details: []*ItemErrorDetail - 错误详情列表
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ItemErrorGroup结构体
 * - 使用Java类定义，包含项目错误组字段
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
public class ItemErrorGroup {

  @JsonProperty("type")
  private ItemErrorType type;

  @JsonProperty("summary")
  private String summary;

  @JsonProperty("error_count")
  private Integer errorCount;

  @JsonProperty("details")
  private List<ItemErrorDetail> details;
}
