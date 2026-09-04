package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.iwhalecloud.bote.loop.prompt.domain.repo.GetPromptBasicOptionFunc;
import com.iwhalecloud.bote.loop.prompt.domain.repo.GetPromptOptionFunc;

/**
 * Prompt选项工具类
 * 迁移对应关系: Go语言repo.WithPromptBasicCacheEnable和WithPromptCacheEnable函数
 * - 功能: 提供便捷的选项配置方法
 * - 方法:
 * * withPromptBasicCacheEnable - 启用Prompt基础信息缓存
 * * withPromptCacheEnable - 启用Prompt缓存
 * <p>
 * Java实现说明:
 * - 对应Go的选项配置函数
 * - 使用静态方法提供便捷配置
 * - 支持函数式编程风格
 * <p>
 * 技术栈迁移:
 * - Go函数 -> Java静态方法
 * - Go函数式选项 -> Java函数式接口
 */
public final class PromptOptions {
  private PromptOptions() {
  }

  /**
   * 启用Prompt基础信息缓存
   * 迁移对应关系: Go语言repo.WithPromptBasicCacheEnable
   * - 功能: 创建启用缓存的选项函数
   * - 返回: 选项配置函数
   */
  public static GetPromptBasicOptionFunc withPromptBasicCacheEnable() {
    return option -> option.setCacheEnable(true);
  }

  /**
   * 启用Prompt缓存
   * 迁移对应关系: Go语言repo.WithPromptCacheEnable
   * - 功能: 创建启用缓存的选项函数
   * - 返回: 选项配置函数
   */
  public static GetPromptOptionFunc withPromptCacheEnable() {
    return option -> option.setCacheEnable(true);
  }
}
