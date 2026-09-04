package com.iwhalecloud.bote.loop.prompt.domain.repo;

import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.GetPromptBasicOption;

/**
 * 获取Prompt基础信息选项函数
 * 迁移对应关系: Go语言repo.GetPromptBasicOptionFunc
 * - 功能: 函数式接口，用于配置GetPromptBasicOption
 * - 类型: func(option *GetPromptBasicOption)
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
public interface GetPromptBasicOptionFunc {

  /**
   * 应用选项
   * 迁移对应关系: Go语言repo.GetPromptBasicOptionFunc
   * - 功能: 应用选项配置
   * - 参数: 选项对象
   */
  void apply(GetPromptBasicOption option);
}
