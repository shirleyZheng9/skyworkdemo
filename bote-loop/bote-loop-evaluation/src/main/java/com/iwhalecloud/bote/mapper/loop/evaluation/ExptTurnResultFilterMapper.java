package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultFilterEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.repo.dto.ExptTurnResultFilterQueryCond;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果过滤器Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/ck/expt_turn_result_filter.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptTurnResultFilterMapper {

  /**
   * 保存过滤器数据
   * 迁移对应关系: Go语言exptTurnResultFilterDAOImpl.Save
   */
  int save(@Param("filter") List<ExptTurnResultFilterEntity> filter);

  /**
   * 查询项目ID状态
   * 迁移对应关系: Go语言exptTurnResultFilterDAOImpl.QueryItemIDStates
   */
  List<Map<String, Object>> queryItemIdStates(@Param("cond") ExptTurnResultFilterQueryCond cond);

  /**
   * 根据实验ID和项目ID获取数据
   * 迁移对应关系: Go语言exptTurnResultFilterDAOImpl.GetByExptIDItemIDs
   */
  List<ExptTurnResultFilterEntity> getByExptIdItemIds(@Param("spaceId") String spaceId,
                                                      @Param("exptId") String exptId,
                                                      @Param("createdDate") String createdDate,
                                                      @Param("itemIds") List<String> itemIds);
}
