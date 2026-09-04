package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterKeyMappingEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实验轮次结果过滤键映射Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_result_filter_key_mapping.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptTurnResultFilterKeyMappingMapper {

  /**
   * 根据实验ID获取过滤键映射
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMappingDAOImpl.GetByExptID
   */
  List<ExptTurnResultFilterKeyMappingEntity> getByExptId(@Param("spaceId") Long spaceId,
                                                         @Param("exptId") Long exptId);

  /**
   * 插入过滤键映射
   * 迁移对应关系: Go语言ExptTurnResultFilterKeyMappingDAOImpl.Insert
   */
  int insert(@Param("exptTurnResultFilterKeyMappings") List<ExptTurnResultFilterKeyMappingEntity> exptTurnResultFilterKeyMappings);

  /**
   * 更新单个过滤键映射
   */
  int updateOne(@Param("mapping") ExptTurnResultFilterKeyMappingEntity mapping);

  /**
   * 根据ID检查是否存在
   */
  boolean existsById(@Param("id") Long id, @Param("spaceId") Long spaceId, @Param("exptId") Long exptId);
}
