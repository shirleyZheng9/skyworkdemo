package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取实验元组选项
 * 对应Go: GetExptTupleOption
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetExptTupleOption {

  private Boolean withoutDeleted;
}
