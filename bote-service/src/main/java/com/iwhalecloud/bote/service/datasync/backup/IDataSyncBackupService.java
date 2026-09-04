package com.iwhalecloud.bote.service.datasync.backup;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupCreateDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnByBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnRecordDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncBackupPageQuery;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncReturnRecordPageQuery;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

/**
 * 数据同步备份服务
 */
public interface IDataSyncBackupService {
  /**
   * 创建全量备份
   *
   * @param request 创建请求
   * @return 备份结果
   */
  ResultVO<Long> createBackup(DataSyncBackupCreateDTO request);

  /**
   * 分页查询备份记录
   *
   * @param query 查询条件
   * @return 备份记录分页数据
   */
  PageInfo<DataSyncBackupDTO> queryBackupPage(DataSyncBackupPageQuery query);

  /**
   * 分页查询回退记录
   *
   * @param query 查询条件
   * @return 回退记录分页数据
   */
  PageInfo<DataSyncReturnRecordDTO> queryReturnLogPage(DataSyncReturnRecordPageQuery query);

  /**
   * 根据备份记录发起回退（异步：先落库并启动回退前全量备份导出，由定时任务轮询后继续导入目标备份）
   *
   * @param request 回退请求
   * @return 回退日志 ID（非发布 recordId）
   */
  ResultVO<Long> returnByBackup(DataSyncReturnByBackupDTO request);

  /**
   * 查询备份文件信息
   *
   * @param backupId 备份记录 ID
   * @return 文件信息
   */
  DataSyncBackupDTO getBackupById(Long backupId);

  /**
   * 根据回退记录 ID 查询回退记录
   *
   * @param id 回退记录 ID
   * @return 回退记录
   */
  DataSyncReturnRecordDTO getReturnRecordById(Long id);
}
