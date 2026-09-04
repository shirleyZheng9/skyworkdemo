package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.EvaluatorFieldMappingDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.TargetFieldMappingDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估配置转换结果
 * 对应Go: ConvertEntityToDTO返回值
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationConfigurationResult {

  /**
   * 目标字段映射
   * 对应Go: targetMapping *domain_expt.TargetFieldMapping
   */
  private TargetFieldMappingDTO targetMapping;

  /**
   * 评估器字段映射列表
   * 对应Go: evaluatorMappings []*domain_expt.EvaluatorFieldMapping
   */
  private List<EvaluatorFieldMappingDTO> evaluatorMappings;
}
