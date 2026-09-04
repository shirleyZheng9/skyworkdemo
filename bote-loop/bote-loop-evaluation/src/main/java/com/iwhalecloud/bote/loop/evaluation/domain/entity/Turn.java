package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次实体
 * 迁移对应关系: Go语言Turn
 * - 功能: 轮次数据结构
 * - 字段: id, fieldDataList
 * <p>
 * Java实现说明:
 * - 对应Go的Turn结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go []*FieldData -> Java List<FieldData>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turn {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("field_data_list")
  private List<FieldData> fieldDataList;
}
