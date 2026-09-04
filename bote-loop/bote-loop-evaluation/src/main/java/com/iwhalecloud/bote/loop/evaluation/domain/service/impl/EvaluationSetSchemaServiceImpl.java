package com.iwhalecloud.bote.loop.evaluation.domain.service.impl;

import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.IDatasetRPCAdapter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.FieldSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.service.EvaluationSetSchemaService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 评估集模式服务实现类
 * 迁移对应关系: Go语言backend/modules/evaluation/domain/service/evaluation_set_schema_impl.go
 * - 功能: 评估集模式业务逻辑实现
 * - 主要方法:
 * * updateEvaluationSetSchema - 更新评估集模式
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluationSetSchemaServiceImpl结构体
 * - 使用Spring Service注解
 * - 依赖数据集RPC适配器
 * - 统一异常处理
 * <p>
 * 技术栈迁移:
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go sync.Once -> Java单例模式
 * - Go rpc.IDatasetRPCAdapter -> Java IDatasetRPCAdapter
 */
@Service
@RequiredArgsConstructor
public class EvaluationSetSchemaServiceImpl implements EvaluationSetSchemaService {

  private final IDatasetRPCAdapter datasetRPCAdapter;

  @Override
  public void updateEvaluationSetSchema(Long spaceId, Long evaluationSetId, List<FieldSchema> fieldSchema) {
    try {
      // 依赖数据集服务
      datasetRPCAdapter.updateDatasetSchema(spaceId, evaluationSetId, fieldSchema);
    }
    catch (Exception e) {
      throw new BssException("更新评估集模式失败: " + e.getMessage(), e);
    }
  }
}
