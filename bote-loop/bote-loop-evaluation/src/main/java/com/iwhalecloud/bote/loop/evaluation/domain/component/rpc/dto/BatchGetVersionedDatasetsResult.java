package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSet;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvaluationSetVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取版本化数据集结果
 * 迁移对应关系: Go语言BatchGetVersionedDatasetsResult结构体
 * - 功能: 批量获取版本化数据集的结果对象
 * - 字段定义:
 * * version: EvaluationSetVersion - 版本对象
 * * evaluationSet: EvaluationSet - 数据集对象
 * <p>
 * Java实现说明:
 * - 对应Go的BatchGetVersionedDatasetsResult结构体
 * - 使用Lombok注解简化代码
 * - 支持Builder模式
 * <p>
 * 技术栈迁移:
 * - Go *entity.EvaluationSetVersion -> Java EvaluationSetVersion
 * - Go *entity.EvaluationSet -> Java EvaluationSet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetVersionedDatasetsResult {

  /**
   * 版本对象
   * 迁移对应关系: Go语言BatchGetVersionedDatasetsResult.Version
   */
  private EvaluationSetVersion version;

  /**
   * 数据集对象
   * 迁移对应关系: Go语言BatchGetVersionedDatasetsResult.EvaluationSet
   */
  private EvaluationSet evaluationSet;
}
