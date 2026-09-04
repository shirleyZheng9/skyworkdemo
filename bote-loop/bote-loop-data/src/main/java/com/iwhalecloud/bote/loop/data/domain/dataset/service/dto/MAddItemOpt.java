package com.iwhalecloud.bote.loop.data.domain.dataset.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 批量添加项目选项DTO
 * 迁移对应关系: Go语言service.MAddItemOpt
 * - 功能: 批量添加项目的选项配置
 * - 字段定义: 各种添加选项
 * <p>
 * Java实现说明:
 * - 对应Go的service.MAddItemOpt结构体
 * - 使用Java类定义，包含添加选项字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go基本类型 -> Java基本类型
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MAddItemOpt {

  private Boolean partialAdd;
}
