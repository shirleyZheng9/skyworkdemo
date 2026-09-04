package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import com.iwhalecloud.bote.loop.data.domain.dataset.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 索引项目DTO
 * 迁移对应关系: Go语言service.IndexedItem
 * - 功能: 存储带索引的项目信息
 * - 字段定义: 项目和索引
 * <p>
 * Java实现说明:
 * - 对应Go的service.IndexedItem结构体
 * - 使用Java类定义，包含索引项目字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go指针类型 -> Java对象引用
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndexedItem {

  private Item item;
  private Integer index;
}
