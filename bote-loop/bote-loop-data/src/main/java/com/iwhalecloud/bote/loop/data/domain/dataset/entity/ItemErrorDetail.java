package com.iwhalecloud.bote.loop.data.domain.dataset.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目错误详情实体
 * 迁移对应关系: Go语言entity.ItemErrorDetail
 * - 功能: 存储项目错误详情信息
 * - 字段定义:
 * * Message: *string - 错误消息
 * * Index: *int32 - 单条错误数据在输入数据中的索引
 * * StartIndex: *int32 - 区间错误范围开始索引
 * * EndIndex: *int32 - 区间错误范围结束索引
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ItemErrorDetail结构体
 * - 使用Java类定义，包含项目错误详情字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemErrorDetail {

  @JsonProperty("message")
  private String message;

  @JsonProperty("index")
  private Integer index;

  @JsonProperty("start_index")
  private Integer startIndex;

  @JsonProperty("end_index")
  private Integer endIndex;
}
