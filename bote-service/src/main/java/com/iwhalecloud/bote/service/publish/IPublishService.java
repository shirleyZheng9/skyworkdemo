package com.iwhalecloud.bote.service.publish;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.base.query.CopyRecordQueryParams;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 发布管理服务
 *
 * @author chen.linfa
 * @since 2025-02-17
 */
public interface IPublishService {

  /**
   * 构造记录
   *
   * @param type 业务分类
   * @param steps 步骤列表
   * @return 记录
   */
  PublishRecordDTO buildRecord(String type, List<PublishStepType> steps);

  /**
   * 获取发布记录
   *
   * @param publishId 记录 ID
   * @return 记录
   */
  PublishRecordDTO getRecord(Long publishId);

  /**
   * 修改记录状态
   *
   * @param record 记录
   */
  void updatePublishRecord(PublishRecordDTO record);

  /**
   * 修改步骤状态
   *
   * @param step 步骤
   */
  void updateStepStatus(PublishStepDTO step);

  /**
   * 查询同步记录列表（分页）
   *
   * @param params 查询条件
   * @return 同步记录（分页）
   */
  PageInfo<PublishRecordDTO> queryRecordPage(RecordQueryParams params);

  /**
   * 查询复制记录列表（分页）
   *
   * @param params 查询条件
   * @return 同步记录（分页）
   */
  PageInfo<PublishRecordDTO> queryCopyRecordPage(CopyRecordQueryParams params);

  /**
   * 执行步骤
   *
   * @param publishId 记录 ID
   * @param auto 是否自动执行首个非自动步骤
   * @param params 入参
   * @return 结果
   */
  ResultVO<Void> start(Long publishId, boolean auto, @Nullable Object params);

  /**
   * 清理长时间处于发布中的数据
   */
  void clearRunningPublishStatus();

  /**
   * 查询发布状态
   *
   * @param record 记录
   * @param stepList 步骤列表
   * @return 状态
   */
  Integer updateOnlinePublishStatus(PublishRecordDTO record, @Nullable List<PublishStepDTO> stepList);

  /**
   * 查询在线发布记录
   *
   * @return 记录
   */
  List<PublishRecordDTO> queryOnlinePublishRecordWithRunning();
}
