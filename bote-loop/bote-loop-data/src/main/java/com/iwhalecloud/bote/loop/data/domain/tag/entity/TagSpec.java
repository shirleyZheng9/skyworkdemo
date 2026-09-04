package com.iwhalecloud.bote.loop.data.domain.tag.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签规格实体
 * 迁移对应关系: Go语言entity.TagSpec
 * - 功能: 存储标签规格配置
 * - 字段定义:
 * * MaxHeight: int - 最大层数
 * * MaxWidth: int - 每层最大宽度
 * <p>
 * Java实现说明:
 * - 对应Go的entity.TagSpec结构体
 * - 使用Java类定义，包含标签规格字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int类型 -> Java int类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagSpec {

  /**
   * 最大层数
   * 迁移对应关系: Go语言entity.TagSpec.MaxHeight (int)
   * - 功能: 标签值树的最大层数限制
   * - 类型: Go的int类型对应Java的int类型
   * - 用途: 控制标签值层级深度
   */
  @JsonProperty("max_height")
  private int maxHeight;

  /**
   * 每层最大宽度
   * 迁移对应关系: Go语言entity.TagSpec.MaxWidth (int)
   * - 功能: 每层标签值的最大数量限制
   * - 类型: Go的int类型对应Java的int类型
   * - 用途: 控制标签值每层数量
   */
  @JsonProperty("max_width")
  private int maxWidth;
}
