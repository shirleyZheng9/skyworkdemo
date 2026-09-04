package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.entity.datasync.DataSyncBackupEntity;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnRecordDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncBackupPageQuery;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncReturnRecordPageQuery;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据同步备份管理
 */
public interface DataSyncBackupMapper {
  /**
   * 新增备份记录
   *
   * @param dto 备份记录
   * @return 结果
   */
  int insertBackup(@Param("dto") DataSyncBackupDTO dto);

  /**
   * 根据主键查询备份记录
   *
   * @param backupId 备份记录 ID
   * @return 备份记录
   */
  DataSyncBackupDTO getBackupById(@Param("backupId") Long backupId);

  /**
   * 分页查询备份记录
   *
   * @param query 查询条件
   * @param rowBounds 分页参数
   * @return 备份记录分页数据
   */
  Page<DataSyncBackupDTO> queryBackupPage(@Param("query") DataSyncBackupPageQuery query, RowBounds rowBounds);

  /**
   * 更新备份文件 ID
   *
   * @param backupId 备份记录 ID
   * @param backupFileId 备份文件 ID
   * @param updatorId 修改人 ID
   * @return 结果
   */
  int updateBackupFileId(@Param("backupId") Long backupId, @Param("backupFileId") Long backupFileId, @Param("updatorId") Long updatorId);

  /**
   * 更新备份文件 ID
   *
   * @param backupId 备份记录 ID
   * @param updatorId 修改人 ID
   * @param backupStatus 备份状态
   * @return 结果
   */
  int updateBackupStatus(@Param("backupId") Long backupId, @Param("updatorId") Long updatorId, @Param("backupStatus") Integer backupStatus);

  /**
   * 更新备份记录信息
   *
   * @param backupId 备份记录 ID
   * @param backupFileId 备份文件 ID
   * @param updatorId 修改人 ID
   * @param backupStatus 备份状态
   * @return 结果
   */
  int updateBackupInfo(@Param("backupId") Long backupId, @Param("backupFileId") Long backupFileId, @Param("updatorId") Long updatorId,
    @Param("backupStatus") Integer backupStatus);

  /**
   * 新增回退记录
   *
   * @param dto 回退记录
   * @return 结果
   */
  int insertReturnLog(@Param("dto") DataSyncReturnRecordDTO dto);

  /**
   * 分页查询回退日志
   *
   * @param query 查询条件
   * @param rowBounds 分页参数
   * @return 回退日志分页数据
   */
  Page<DataSyncReturnRecordDTO> queryReturnLogPage(@Param("query") DataSyncReturnRecordPageQuery query, RowBounds rowBounds);

  /**
   * 查询待清理的备份记录
   *
   * @param maxCreatedTime 最大创建时间
   * @return 备份记录列表
   */
  List<DataSyncBackupEntity> queryBackupForClear(@Param("maxCreatedTime") Date maxCreatedTime);

  /**
   * 逻辑删除备份记录
   *
   * @param backupIds 备份记录 ID 集合
   * @param updatorId 修改人 ID
   * @return 结果
   */
  int clearBackupByIds(@Param("backupIds") List<Long> backupIds, @Param("updatorId") Long updatorId);

  /**
   * 根据主键查询回退记录
   *
   * @param returnId 回退记录 ID
   * @return 回退记录
   */
  DataSyncReturnRecordDTO getReturnRecordById(@Param("returnId") Long returnId);

  /**
   * 更新回退记录状态
   *
   * @param returnId 回退记录 ID
   * @param status 回退状态
   * @return 影响行数
   */
  int updateReturnRecordStatus(@Param("returnId") Long returnId, @Param("status") Integer status);
}
