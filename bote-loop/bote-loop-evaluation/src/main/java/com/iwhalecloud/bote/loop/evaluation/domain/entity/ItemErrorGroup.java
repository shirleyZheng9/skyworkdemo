package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项错误组实体
 * 迁移对应关系: Go语言ItemErrorGroup
 * - 功能: 项错误组数据结构
 * - 字段: type, summary, errorCount, details
 * <p>
 * Java实现说明:
 * - 对应Go的ItemErrorGroup结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *ItemErrorType -> Java ItemErrorType
 * - Go *string -> Java String
 * - Go *int32 -> Java Integer
 * - Go []*ItemErrorDetail -> Java List<ItemErrorDetail>
 * - Go json标签 -> Jackson注解
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
