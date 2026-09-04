package com.iwhalecloud.bote.service.datasync.backup.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.DataSyncConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.ExportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.ImportDataParams;
import com.iwhalecloud.bote.mapper.base.DataSyncBackupMapper;
import com.iwhalecloud.bote.mapper.base.PublishManageMapper;
import com.iwhalecloud.bote.service.datasync.IDataSyncService;
import com.iwhalecloud.bote.service.datasync.backup.IDataSyncBackupService;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupCreateDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnByBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnRecordDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncBackupPageQuery;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncReturnRecordPageQuery;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


/**
 * 数据同步备份服务实现
 *
 * @author qian.sisheng
 * @since 2026-04-17
 */
@Service
@RequiredArgsConstructor
public class DataSyncBackupServiceImpl implements IDataSyncBackupService {

  private static final Logger logger = LoggerFactory.getLogger(DataSyncBackupServiceImpl.class);

  private final IDataSyncService dataSyncService;
  private final IFileStoreService fileStoreService;
  private final DataSyncBackupMapper backupMapper;
  private final PublishManageMapper publishManageMapper;

  @Override
  public ResultVO<Long> createBackup(DataSyncBackupCreateDTO request) {
    Assert.notNull(request.getTenantId(), "tenantId 不能为空");
    ExportDataParams exportParams = new ExportDataParams();
    exportParams.setTenantId(request.getTenantId());
    exportParams.setSyncAll(Boolean.TRUE);
    exportParams.setBackUp(Boolean.TRUE);
    String fileName = generateFileName();
    exportParams.setExportFileName(fileName);
    ResultVO<Long> exportResult = dataSyncService.publishExport(exportParams, BaseConsts.PUBLISH_TYPE_BACKUP);
    if (exportResult.getResultObject() == null) {
      return ResultVO.fail("备份失败，导出流程未启动");
    }
    PublishRecordDTO record = getRecordById(exportResult.getResultObject());
    if (record == null) {
      return ResultVO.fail("备份失败，发布记录不存在");
    }
    // 保存备份记录
    DataSyncBackupDTO backup = new DataSyncBackupDTO();
    backup.setBackupId(IDUtils.nextId());
    backup.setTenantId(request.getTenantId());
    backup.setBackupName(fileName);
    backup.setBackupType(DataSyncConsts.PUBLISH_TYPE_ALL);
    backup.setRecordId(exportResult.getResultObject());
    backup.setBackupFileId(null);
    backup.setRemark(request.getRemark());
    backup.setStatusCd(BaseConsts.STATUS_CD_VALID);
    backup.setBackupStatus(record.getPublishStatus());
    Long userId = SessionUtil.getOptionalUserId();
    backup.setCreatorId(userId);
    backup.setUpdatorId(userId);
    backupMapper.insertBackup(backup);
    return ResultVO.success(exportResult.getResultObject());
  }

  /**
   * 生成备份文件名称
   *
   * @return 文件名称
   */
  private String generateFileName() {
    return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
  }

  @Override
  public PageInfo<DataSyncBackupDTO> queryBackupPage(DataSyncBackupPageQuery query) {
    RowBounds rowBounds = query.buildRowBounds();
    // noinspection resource
    PageInfo<DataSyncBackupDTO> page = backupMapper.queryBackupPage(query, rowBounds).toPageInfo();
    if (CollectionUtils.isEmpty(page.getList())) {
      return page;
    }
    // 刷新备份记录状态
    List<DataSyncBackupDTO> needRefreshList = page.getList().stream().filter(
      dto -> Objects.equals(dto.getBackupStatus(), BaseConsts.PUBLISH_STATUS_RUNNING) || Objects.equals(dto.getBackupStatus(),
        BaseConsts.PUBLISH_STATUS_NOT_STARTED) || dto.getBackupFileId() == null).toList();
    for (DataSyncBackupDTO dto : needRefreshList) {
      refreshBackupStatus(dto);
    }
    return page;
  }

  /**
   * 刷新备份记录状态，并在成功后回填导出文件 ID
   */
  private void refreshBackupStatus(DataSyncBackupDTO backup) {
    if (backup.getRecordId() == null) {
      return;
    }
    PublishRecordDTO record = getRecordById(backup.getRecordId());
    if (record == null) {
      return;
    }
    boolean needUpdateFileId = backup.getBackupFileId() == null && Objects.equals(BaseConsts.PUBLISH_STATUS_SUCCESS, record.getPublishStatus());
    boolean needUpdateStatus = !Objects.equals(backup.getBackupStatus(), record.getPublishStatus());
    if (!needUpdateFileId && !needUpdateStatus) {
      return;
    }
    Long backupFileId = needUpdateFileId ? resolveFileId(record) : null;
    Integer newStatus = needUpdateStatus ? record.getPublishStatus() : null;
    backupMapper.updateBackupInfo(backup.getBackupId(), backupFileId, SessionUtil.getOptionalUserId(), newStatus);
  }

  /**
   * 根据 ID 获取发布记录
   */
  @Nullable
  public PublishRecordDTO getRecordById(Long id) {
    PublishRecordDTO record = publishManageMapper.getRecord(id);
    if (record == null) {
      return null;
    }
    List<PublishStepDTO> publishSteps = publishManageMapper.selectStepList(id);
    for (PublishStepDTO step : CollectionUtils.emptyIfNull(publishSteps)) {
      step.setOutput(step.getOutputJson());
    }
    record.setSteps(publishSteps);
    return record;
  }

  /**
   * 从导出发布记录的 WRAP_EXPORT 步骤中提取备份文件 ID。
   */
  private Long resolveFileId(PublishRecordDTO record) {
    if (record == null || CollectionUtils.isEmpty(record.getSteps())) {
      return null;
    }
    for (PublishStepDTO step : record.getSteps()) {
      if (step.getStepType() == PublishStepType.WRAP_EXPORT.getValue() && StringUtils.isNotBlank(step.getOutputJson())) {
        Map<String, Object> output = JsonUtil.parseJsonRequired(step.getOutputJson(), new TypeReference<>() {
        });
        Object fileId = output.get("fileId");
        if (fileId != null) {
          return Long.parseLong(String.valueOf(fileId));
        }
      }
    }
    return null;
  }

  @Override
  public PageInfo<DataSyncReturnRecordDTO> queryReturnLogPage(DataSyncReturnRecordPageQuery query) {
    RowBounds rowBounds = query.buildRowBounds();
    // noinspection resource
    PageInfo<DataSyncReturnRecordDTO> pageInfo = backupMapper.queryReturnLogPage(query, rowBounds).toPageInfo();
    if (CollectionUtils.isEmpty(pageInfo.getList())) {
      return pageInfo;
    }
    // 刷新回退记录状态
    List<DataSyncReturnRecordDTO> needRefreshList = pageInfo.getList().stream().filter(
      record -> Objects.equals(BaseConsts.PUBLISH_STATUS_RUNNING, record.getReturnStatus()) || Objects.equals(BaseConsts.PUBLISH_STATUS_NOT_STARTED,
        record.getReturnStatus())).toList();
    for (DataSyncReturnRecordDTO record : needRefreshList) {
      refreshReturnRecordStatus(record);
    }
    return pageInfo;
  }

  /**
   * 刷新回退记录状态
   */
  private void refreshReturnRecordStatus(DataSyncReturnRecordDTO record) {
    if (record.getRecordId() == null) {
      return;
    }
    PublishRecordDTO publishRecord = getRecordById(record.getRecordId());
    if (publishRecord == null) {
      return;
    }
    Integer publishStatus = publishRecord.getPublishStatus();
    if (!Objects.equals(record.getReturnStatus(), publishStatus)) {
      backupMapper.updateReturnRecordStatus(record.getReturnId(), publishStatus);
    }
  }

  @Override
  public ResultVO<Long> returnByBackup(DataSyncReturnByBackupDTO request) {
    DataSyncBackupDTO backup = backupMapper.getBackupById(request.getBackupId());
    Assert.notNull(backup, "备份记录不存在");
    Assert.notNull(backup.getBackupFileId(), "该备份尚未备份完成，请稍后重试");
    FileInfoVO targetFileInfo = fileStoreService.getFileInfoById(backup.getBackupFileId());
    Assert.notNull(targetFileInfo, "备份文件不存在");
    // 构建回退导入参数
    ImportDataParams importDataParams = new ImportDataParams();
    importDataParams.setFileId(targetFileInfo.getFileId());
    importDataParams.setFileName(targetFileInfo.getFileName());
    importDataParams.setTenantId(request.getTenantId());
    importDataParams.setAutoConfirm(Boolean.TRUE);
    // 启动回退导入流程
    ResultVO<Long> publishResult = dataSyncService.publishImport(importDataParams, BaseConsts.PUBLISH_TYPE_RETURN);
    if (publishResult.getResultObject() == null) {
      return ResultVO.fail("回退失败");
    }
    PublishRecordDTO record = getRecordById(publishResult.getResultObject());
    if (record == null) {
      return ResultVO.fail("回退失败，发布记录不存在");
    }
    DataSyncReturnRecordDTO returnRecord = new DataSyncReturnRecordDTO();
    returnRecord.setReturnId(IDUtils.nextId());
    returnRecord.setTenantId(request.getTenantId());
    returnRecord.setBackupId(request.getBackupId());
    returnRecord.setRemark(request.getRemark());
    returnRecord.setReturnFileId(targetFileInfo.getFileId());
    returnRecord.setStatusCd(BaseConsts.STATUS_CD_VALID);
    returnRecord.setReturnStatus(record.getPublishStatus());
    returnRecord.setReturnName(backup.getBackupName());
    returnRecord.setRecordId(publishResult.getResultObject());
    Long userId = SessionUtil.getOptionalUserId();
    returnRecord.setCreatorId(userId);
    returnRecord.setUpdatorId(userId);
    backupMapper.insertReturnLog(returnRecord);
    return ResultVO.success(publishResult.getResultObject());
  }

  @Override
  public DataSyncBackupDTO getBackupById(Long id) {
    DataSyncBackupDTO backupFile = backupMapper.getBackupById(id);
    Assert.notNull(backupFile, "回退记录不存在");
    Assert.notNull(backupFile.getBackupFileId(), "回退前版本备份文件尚未生成，请稍后重试");
    return backupFile;
  }

  @Override
  public DataSyncReturnRecordDTO getReturnRecordById(Long id) {
    DataSyncReturnRecordDTO returnRecord = backupMapper.getReturnRecordById(id);
    Assert.notNull(returnRecord, "回退记录不存在");
    Assert.notNull(returnRecord.getReturnFileId(), "回退版本文件不存在");
    return returnRecord;
  }
}
