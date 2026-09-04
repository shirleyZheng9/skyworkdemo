package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验更新字段实体
 * 迁移对应关系: Go语言ExptUpdateFields
 * - 功能: 实验更新字段数据结构
 * - 字段: name, desc
 * <p>
 * Java实现说明:
 * - 对应Go的ExptUpdateFields结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现ToFieldMap方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptUpdateFields {
  @JsonProperty("name")
  private String name;

  @JsonProperty("desc")
  private String desc;

  /**
   * 转换为字段映射
   * 迁移对应关系: Go语言ExptUpdateFields.ToFieldMap()
   */
  public Map<String, Object> toFieldMap() {
    Map<String, Object> map = new java.util.HashMap<>();
    if (this.name != null) {
      map.put("name", this.name);
    }
    if (this.desc != null) {
      map.put("desc", this.desc);
    }
    return map;
  }
}
