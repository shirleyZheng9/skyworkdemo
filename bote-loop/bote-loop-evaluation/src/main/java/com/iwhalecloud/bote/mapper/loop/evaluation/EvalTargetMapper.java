package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评估目标Mapper接口
 */

public interface EvalTargetMapper {

  /**
   * 创建评估目标
   */
  void createEvalTarget(@Param("target") TargetEntity target);

  /**
   * 获取评估目标
   */
  TargetEntity getEvalTarget(@Param("targetId") Long targetId);

  /**
   * 根据来源ID获取评估目标
   */
  TargetEntity getEvalTargetBySourceId(@Param("spaceId") Long spaceId,
                                       @Param("sourceTargetId") String sourceTargetId,
                                       @Param("targetType") Integer targetType);

  /**
   * 根据来源ID批量获取评估目标
   */
  List<TargetEntity> batchGetEvalTargetBySource(@Param("spaceId") Long spaceId,
                                                @Param("sourceTargetIds") List<String> sourceTargetIds,
                                                @Param("targetType") Integer targetType);

  /**
   * 批量获取评估目标
   */
  List<TargetEntity> batchGetEvalTarget(@Param("spaceId") Long spaceId,
                                        @Param("targetIds") List<Long> targetIds);
}
