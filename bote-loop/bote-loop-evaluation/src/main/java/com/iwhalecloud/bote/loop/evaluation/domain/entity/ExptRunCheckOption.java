package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验运行检查选项
 * 对应Go: ExptRunCheckOption
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptRunCheckOption {

  private Boolean checkBenefit;
}
