package com.iwhalecloud.bote.mapper.loop.evaluation;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptItemResultEntity;
import com.iwhalecloud.bote.entity.loop.evaluation.ExptTurnResultEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

/**
 * 实验数据项结果Mapper接口
 * 迁移对应关系: Go语言backend/modules/evaluation/infra/repo/experiment/mysql/expt_item_result.go
 *
 * @author Generated
 * @since 2025-01-27
 */

public interface ExptItemResultMapper {

  /**
   * 批量获取实验数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.BatchGet
   */
  List<ExptItemResultEntity> batchGet(@Param("spaceId") Long spaceId,
                                      @Param("exptId") Long exptId,
                                      @Param("itemIds") List<Long> itemIds);

  /**
   * 批量创建实验数据项结果（如果不存在）
   * 迁移对应关系: Go语言IExptItemResultDAO.BatchCreateNx
   */
  int batchCreateNx(@Param("itemResults") List<ExptItemResultEntity> itemResults);

  /**
   * 扫描实验数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.ScanItemResults
   */
  List<ExptItemResultEntity> scanItemResults(@Param("exptId") Long exptId,
                                             @Param("cursor") Long cursor,
                                             @Param("limit") Long limit,
                                             @Param("status") List<Integer> status,
                                             @Param("spaceId") Long spaceId);

  /**
   * 根据实验ID获取数据项ID列表
   * 迁移对应关系: Go语言IExptItemResultDAO.GetItemIdListByExptID
   */
  List<Long> getItemIdListByExptId(@Param("exptId") Long exptId, @Param("spaceId") Long spaceId);

  /**
   * 根据实验ID分页查询数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.ListItemResultsByExptID
   */
  Page<ExptItemResultEntity> listItemResultsByExptId(@Param("exptId") Long exptId,
                                                     @Param("spaceId") Long spaceId,
                                                     @Param("desc") Boolean desc,
                                                     RowBounds rowBounds);

  /**
   * 保存实验数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.SaveItemResults
   */
  int saveItemResults(@Param("itemResults") List<ExptItemResultEntity> itemResults);

  /**
   * 获取数据项轮次结果
   * 迁移对应关系: Go语言IExptItemResultDAO.GetItemTurnResults
   */
  List<ExptTurnResultEntity> getItemTurnResults(@Param("spaceId") Long spaceId,
                                                @Param("exptId") Long exptId,
                                                @Param("itemId") Long itemId);

  /**
   * 更新数据项结果
   * 迁移对应关系: Go语言IExptItemResultDAO.UpdateItemsResult
   */
  int updateItemsResult(@Param("spaceId") Long spaceId,
                        @Param("exptId") Long exptId,
                        @Param("itemIds") List<Long> itemIds,
                        @Param("ufields") Map<String, Object> ufields);

  /**
   * 根据实验ID获取最大数据项序号
   * 迁移对应关系: Go语言IExptItemResultDAO.GetMaxItemIdxByExptID
   */
  Integer getMaxItemIdxByExptId(@Param("exptId") Long exptId, @Param("spaceId") Long spaceId);

  // 新增方法：拆分MySQL特殊语法

  /**
   * 检查数据项结果是否存在
   * 根据spaceId、exptId、exptRunId、itemId判断记录是否存在
   */
  boolean existsByKeys(@Param("spaceId") Long spaceId,
                       @Param("exptId") Long exptId,
                       @Param("exptRunId") Long exptRunId,
                       @Param("itemId") Long itemId);

  /**
   * 插入数据项结果
   * 直接插入新记录
   */
  int insertItemResult(ExptItemResultEntity itemResult);

  /**
   * 更新数据项结果（保持原有条件）
   * 根据spaceId、exptId、exptRunId、itemId更新记录
   */
  int updateItemResult(ExptItemResultEntity itemResult);
}
