package com.iwhalecloud.bote.loop.evaluation.domain.entity;

/**
 * 获取实验元组选项函数式接口
 * 对应Go: GetExptTupleOptionFn func(*GetExptTupleOption)
 */
@FunctionalInterface
public interface GetExptTupleOptionFn {
  void apply(GetExptTupleOption option);
}
