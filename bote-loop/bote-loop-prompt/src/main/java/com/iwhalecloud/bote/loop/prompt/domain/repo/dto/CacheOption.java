package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存选项
 * 迁移对应关系: Go语言repo.CacheOption
 * - 功能: 定义缓存相关的选项
 * - 字段定义:
 * * CacheEnable: bool - 是否启用缓存
 * <p>
 * Java实现说明:
 * - 对应Go的repo.CacheOption结构体
 * - 使用Java类定义，包含缓存选项字段
 * - 使用Lombok注解简化代码
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheOption {

  /**
   * 是否启用缓存
   * 迁移对应关系: Go语言repo.CacheOption.CacheEnable (bool)
   * - 功能: 是否启用缓存
   * - 类型: Go的bool对应Java的Boolean
   * - 用途: 控制缓存行为
   */
  private Boolean cacheEnable;
}
