package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.TargetRecordEntity;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评估目标记录Mapper接口
 */

public interface EvalTargetRecordMapper {

  /**
   * 创建评估目标记录
   */
  Long create(@Param("record") TargetRecordEntity record);

  /**
   * 根据ID和空间ID获取评估目标记录
   */
  TargetRecordEntity getByIdAndSpaceId(@Param("recordId") Long recordId,
                                       @Param("spaceId") Long spaceId);

  /**
   * 根据ID列表和空间ID列出评估目标记录
   */
  List<TargetRecordEntity> listByIdsAndSpaceId(@Param("recordIds") List<Long> recordIds,
                                               @Param("spaceId") Long spaceId);
}
