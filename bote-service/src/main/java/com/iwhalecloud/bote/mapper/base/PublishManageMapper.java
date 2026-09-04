package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.base.query.CopyRecordQueryParams;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bote.dto.model.EvalPublishRecordDTO;
import com.iwhalecloud.bote.dto.model.FinetunePublishRecordDTO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 发布记录管理
 *
 * @author auto
 * @since 2025-02-17
 */
public interface PublishManageMapper {

  /**
   * 检查是否存在运行中的发布记录
   *
   * @param tenantId 租户 ID
   * @param publishType 类型
   * @return 结果
   */
  boolean existsRunningRecord(@Param("tenantId") Long tenantId, @Param("publishType") String publishType);

  /**
   * 根据主键获取发布记录
   *
   * @param id 记录主键
   * @return 记录
   */
  PublishRecordDTO getRecord(@Param("id") Long id);

  /**
   * 根据 objId 获取发布记录
   *
   * @param objId objId
   * @return 记录
   */
  PublishRecordDTO getRecordByObjId(@Param("objId") Long objId);

  /**
   * 新增记录
   *
   * @param record 记录
   * @return 结果
   */
  int insertRecord(@Param("dto") PublishRecordDTO record);

  /**
   * 批量新增步骤信息
   *
   * @param steps 步骤信息列表
   * @return 结果
   */
  int batchInsertStep(@Param("list") List<PublishStepDTO> steps);

  /**
   * 获取步骤信息列表
   *
   * @param publishId 记录 ID
   * @return 步骤信息列表
   */
  List<PublishStepDTO> selectStepList(@Param("publishId") Long publishId);

  /**
   * 更新记录信息
   *
   * @param record 记录信息
   * @return 结果
   */
  int updateRecord(@Param("dto") PublishRecordDTO record);

  /**
   * 更新记录信息，用于重试
   *
   * @param id 记录 ID
   * @return 结果
   */
  int updateRecordForRetry(@Param("id") Long id);

  /**
   * 更新记录步骤信息，用于重试
   *
   * @param publishId 记录 ID
   * @return 结果
   */
  int updateRecordStepForRetry(@Param("publishId") Long publishId);

  /**
   * 更新步骤执行结果
   *
   * @param step 步骤
   * @return 结果
   */
  int updateStepStatus(@Param("dto") PublishStepDTO step);

  /**
   * 查询发布记录
   *
   * @param params 查询参数
   * @param rowBounds 分页参数
   * @return 发布记录分页数据
   */
  Page<PublishRecordDTO> selectRecordPage(@Param("query") RecordQueryParams params, RowBounds rowBounds);

  /**
   * 查询复制记录
   *
   * @param params 查询参数
   * @param rowBounds 分页参数
   * @return 复制记录分页数据
   */
  Page<PublishRecordDTO> selectCopyRecordPage(@Param("query") CopyRecordQueryParams params, RowBounds rowBounds);

  /**
   * 查询微调发布记录
   *
   * @param params 查询参数
   * @param rowBounds 分页参数
   * @return 发布记录分页数据
   */
  Page<FinetunePublishRecordDTO> selectFinetuneRecordPage(@Param("query") RecordQueryParams params, RowBounds rowBounds);

  /**
   * 查询评测发布记录
   *
   * @param params 查询参数
   * @param rowBounds 分页参数
   * @return 评测记录分页数据
   */
  Page<EvalPublishRecordDTO> selectEvalRecordPage(@Param("query") RecordQueryParams params, RowBounds rowBounds);

  /**
   * 更新发布状态，将处于运行中状态且长时间未更新的标记为执行失败
   */
  int updateRecordForClear(@Param("maxUpdatedTime") Date maxUpdatedTime);

  /**
   * 查询在线发布记录中处于运行中状态的记录
   *
   * @return 记录列表
   */
  List<PublishRecordDTO> selectOnlinePublishRecordWithRunning();
}
