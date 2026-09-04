package com.iwhalecloud.bote.loop.evaluation.domain.service;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.ArgsSchema;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTarget;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetExecuteResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetInputData;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExecuteEvalTargetParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceResult;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ListSourceVersionResult;

import java.util.List;

/**
 * 源评估目标操作服务接口
 * 对应Go: ISourceEvalTargetOperateService
 */
public interface ISourceEvalTargetOperateService {

  /**
   * 获取评估目标类型
   * 对应Go: EvalType
   */
  EvalTargetType evalType();

  /**
   * 根据source target构建eval target实体
   * 对应Go: BuildBySource
   */
  EvalTarget buildBySource(Long spaceId, String sourceTargetId, String sourceTargetVersion);

  /**
   * 查询source target列表
   * 对应Go: ListSource
   */
  ListSourceResult listSource(ListSourceParam param);

  /**
   * 查询source target列表
   * 对应Go: BatchGetSource
   */
  List<EvalTarget> batchGetSource(Long spaceId, List<String> ids);

  /**
   * 查询source target版本列表
   * 对应Go: ListSourceVersion
   */
  ListSourceVersionResult listSourceVersion(ListSourceVersionParam param);

  /**
   * 拼装源信息
   * 对应Go: PackSourceInfo
   */
  void packSourceInfo(Long spaceId, List<EvalTarget> dos);

  /**
   * 拼装源版本信息
   * 对应Go: PackSourceVersionInfo
   */
  void packSourceVersionInfo(Long spaceId, List<EvalTarget> dos);

  /**
   * 验证输入
   * 对应Go: ValidateInput
   */
  void validateInput(Long spaceId, List<ArgsSchema> inputSchema, EvalTargetInputData input);

  /**
   * 执行
   * 对应Go: Execute
   */
  EvalTargetExecuteResult execute(Long spaceId, ExecuteEvalTargetParam param);
}
