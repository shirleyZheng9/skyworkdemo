package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 完成实验选项函数式接口
 * 对应Go: CompleteExptOptionFn func(*CompleteExptOption)
 */
@FunctionalInterface
public interface CompleteExptOptionFn {
  void apply(CompleteExptOption option);
}
