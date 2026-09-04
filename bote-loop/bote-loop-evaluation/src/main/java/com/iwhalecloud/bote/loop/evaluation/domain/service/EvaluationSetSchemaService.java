package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;

import java.util.List;

/**
 * 评估集模式服务接口
 * 对应Go: EvaluationSetSchemaService
 */
public interface EvaluationSetSchemaService {

  /**
   * 更新评估集模式
   * 对应Go方法: UpdateEvaluationSetSchema
   *
   * @param spaceId 工作空间ID
   * @param evaluationSetId 评估集ID
   * @param fieldSchemas 字段模式列表
   */
  void updateEvaluationSetSchema(Long spaceId, Long evaluationSetId, List<FieldSchema> fieldSchemas);
}
