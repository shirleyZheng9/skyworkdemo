package com.iwhalecloud.bote.loop.prompt.domain.repo;

import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptOption;

/**
 * 获取Prompt选项函数
 * 迁移对应关系: Go语言repo.GetPromptOptionFunc
 * - 功能: 函数式接口，用于配置GetPromptOption
 * - 类型: func(option *GetPromptOption)
 * <p>
 * Java实现说明:
 * - 对应Go的函数类型
 * - 使用Java函数式接口
 * - 支持函数式编程
 * <p>
 * 技术栈迁移:
 * - Go函数类型 -> Java函数式接口
 */
@FunctionalInterface
public interface GetPromptOptionFunc {

  /**
   * 应用选项
   * 迁移对应关系: Go语言repo.GetPromptOptionFunc
   * - 功能: 应用选项配置
   * - 参数: 选项对象
   */
  void apply(GetPromptOption option);
}
