package com.iwhalecloud.bote.loop.evaluation.infra.repo.experiment.mysql;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterKeyMappingEntity;
import java.util.List;

/**
 * 实验轮次结果过滤键映射DAO接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_result_filter_key_mapping.go
 *
 * @author Generated
 * @since 2025-01-27
 */
public interface IExptTurnResultFilterKeyMappingDAO {

  /**
   * 根据实验ID获取过滤键映射
   * 迁移对应关系: Go语言IExptTurnResultFilterKeyMappingDAO.GetByExptID
   *
   * @param spaceId 空间ID
   * @param exptId 实验ID
   * @return 过滤键映射列表
   */
  List<ExptTurnResultFilterKeyMappingEntity> getByExptId(Long spaceId, Long exptId);

  /**
   * 插入过滤键映射
   * 迁移对应关系: Go语言IExptTurnResultFilterKeyMappingDAO.Insert
   *
   * @param exptTurnResultFilterKeyMappings 过滤键映射列表
   */
  void insert(List<ExptTurnResultFilterKeyMappingEntity> exptTurnResultFilterKeyMappings);
}
