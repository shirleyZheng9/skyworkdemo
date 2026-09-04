package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetVersionEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评估目标版本Mapper接口
 */

public interface EvalTargetVersionMapper {

  /**
   * 创建评估目标版本
   */
  void createEvalTargetVersion(@Param("target") TargetVersionEntity target);

  /**
   * 获取评估目标版本
   */
  TargetVersionEntity getEvalTargetVersion(@Param("spaceId") Long spaceId,
                                           @Param("versionId") Long versionId);

  /**
   * 根据目标获取评估目标版本
   */
  TargetVersionEntity getEvalTargetVersionByTarget(@Param("spaceId") Long spaceId,
                                                   @Param("targetId") Long targetId,
                                                   @Param("sourceTargetVersion") String sourceTargetVersion);

  /**
   * 批量获取评估目标版本
   */
  List<TargetVersionEntity> batchGetEvalTargetVersion(@Param("spaceId") Long spaceId,
                                                      @Param("versionIds") List<Long> versionIds);
}
