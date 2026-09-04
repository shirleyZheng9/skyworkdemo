package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 实验运行检查选项函数式接口
 * 对应Go: ExptRunCheckOptionFn func(*ExptRunCheckOption)
 */
@FunctionalInterface
public interface ExptRunCheckOptionFn {
  void apply(ExptRunCheckOption option);
}
