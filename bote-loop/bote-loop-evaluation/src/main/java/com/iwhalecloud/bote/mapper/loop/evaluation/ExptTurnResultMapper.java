package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnEvaluatorResultRefEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ExptTurnResultFilter;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ItemTurnID;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

/**
 * 实验轮次结果Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_turn_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptTurnResultMapper {

  /**
   * 分页查询轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.ListTurnResult
   */
  List<ExptTurnResultEntity> listTurnResult(@Param("spaceId") Long spaceId,
                                            @Param("exptId") Long exptId,
                                            @Param("filter") ExptTurnResultFilter filter,
                                            @Param("desc") Boolean desc,
                                            RowBounds rowBounds);

  /**
   * 根据数据项ID列表分页查询轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.ListTurnResultByItemIDs
   */
  List<ExptTurnResultEntity> listTurnResultByItemIds(@Param("spaceId") Long spaceId,
                                                     @Param("exptId") Long exptId,
                                                     @Param("itemIds") List<Long> itemIds,
                                                     @Param("filter") ExptTurnResultFilter filter,
                                                     @Param("desc") Boolean desc,
                                                     RowBounds rowBounds);

  /**
   * 批量获取轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.BatchGet
   */
  List<ExptTurnResultEntity> batchGet(@Param("spaceId") Long spaceId,
                                      @Param("exptId") Long exptId,
                                      @Param("itemIds") List<Long> itemIds);

  /**
   * 创建轮次评估器引用
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.CreateTurnEvaluatorRefs
   */
  int createTurnEvaluatorRefs(@Param("refs") List<ExptTurnEvaluatorResultRefEntity> refs);

  /**
   * 批量创建轮次结果（如果不存在）
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.BatchCreateNX
   */
  int batchCreateNx(@Param("turnResults") List<ExptTurnResultEntity> turnResults);

  /**
   * 获取数据项轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.GetItemTurnResults
   */
  List<ExptTurnResultEntity> getItemTurnResults(@Param("exptId") Long exptId,
                                                @Param("itemId") Long itemId,
                                                @Param("spaceId") Long spaceId);

  List<ExptTurnResultEntity> getItemTurnResults(@Param("exptId") Long exptId,
                                                @Param("itemId") Long itemId,
                                                @Param("spaceId") Long spaceId,
                                                @Param("exptRunId") Long exptRunId);

  /**
   * 更新单个轮次结果
   * 根据 expt_id、item_id 和 turn_id 更新记录
   */
  int saveTurnResult(@Param("result") ExptTurnResultEntity result);

  /**
   * 扫描轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.ScanTurnResults
   */
  Page<ExptTurnResultEntity> scanTurnResults(@Param("exptId") Long exptId,
                                             @Param("status") List<Integer> status,
                                             @Param("spaceId") Long spaceId,
                                             RowBounds rowBounds);

  /**
   * 更新轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.UpdateTurnResults
   */
  int updateTurnResults(@Param("exptId") Long exptId,
                        @Param("itemTurnIds") List<ItemTurnID> itemTurnIds,
                        @Param("spaceId") Long spaceId,
                        @Param("ufields") Map<String, Object> ufields);

  /**
   * 根据数据项ID更新轮次结果
   * 迁移对应关系: Go语言ExptTurnResultDAOImpl.UpdateTurnResultsWithItemIDs
   */
  int updateTurnResultsWithItemIds(@Param("exptId") Long exptId,
                                   @Param("itemIds") List<Long> itemIds,
                                   @Param("spaceId") Long spaceId,
                                   @Param("ufields") Map<String, Object> ufields);
}
