package com.iwhalecloud.bote.controller.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.query.CopyRecordQueryParams;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bote.dto.datasync.DataSyncGroupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import com.iwhalecloud.bote.dto.datasync.query.CopyDataParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncQueryParams;
import com.iwhalecloud.bote.dto.datasync.query.ExportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.ImportDataParams;
import com.iwhalecloud.bote.dto.datasync.query.JumpStepParams;
import com.iwhalecloud.bote.dto.datasync.query.OnlinePublishParams;
import com.iwhalecloud.bote.service.datasync.IDataSyncService;
import com.iwhalecloud.bote.service.datasync.backup.IDataSyncBackupService;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupCreateDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnByBackupDTO;
import com.iwhalecloud.bote.dto.datasync.DataSyncReturnRecordDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncBackupPageQuery;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncReturnRecordPageQuery;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据同步管理 controller
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/datasync", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：数据同步管理")
public class DataSyncController {

  private final IDataSyncService dataSyncService;

  private final IFileStoreService fileStoreService;

  private final IPublishService publishService;
  private final IDataSyncBackupService dataSyncBackupService;

  @Operation(summary = "步骤化数据导出")
  @PostMapping("exportByStep")
  public ResultVO<Long> exportByStep(@RequestBody ExportDataParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    if (params.getSyncAll() != null && BooleanUtils.isFalse(params.getSyncAll())) {
      Assert.notEmpty(params.getCodeAndIds(), "请指定自定义导出数据范围");
    }
    else {
      params.setSyncAll(Boolean.TRUE);
    }
    return dataSyncService.publishExport(params);
  }

  @Operation(summary = "步骤化数据导入")
  @PostMapping("importByStep")
  public ResultVO<Long> importByStep(@RequestParam("file") MultipartFile file, @RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(value = "spaceId", required = false) Long spaceId,
    @RequestParam(value = "autoConfirm", required = false, defaultValue = "false") Boolean autoConfirm) {
    try {
      // 获取并校验文件类型: 仅限zip类型
      String fileType = FileTypeUtil.getType(file);
      if (!"zip".equals(fileType)) {
        return BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toResult(fileType);
      }
      // 导出的数据包，先上传，避免在后续异步处理，出现文件丢失问题
      UploadConfigVO config = new UploadConfigVO();
      config.setSubFolder(tenantId + "/datasync");
      config.setSaveName("");
      config.setOriginalFileName(StringUtils.trimToNull(file.getOriginalFilename()));
      config.setFileSize(file.getSize());
      config.setFileType(fileType);
      FileInfoVO fileInfo = fileStoreService.uploadFile(file, config);

      ImportDataParams params = new ImportDataParams();
      params.setFileId(fileInfo.getFileId());
      params.setFileName(fileInfo.getFileName());
      params.setTenantId(tenantId);
      params.setSpaceId(spaceId);
      params.setAutoConfirm(BooleanUtils.isTrue(autoConfirm));
      return dataSyncService.publishImport(params);
    }
    catch (IOException | RuntimeException e) {
      return ResultVO.fail("步骤化数据导入失败：" + e.getMessage());
    }
  }

  @Operation(summary = "步骤化数据复制")
  @PostMapping("copyByStep")
  public ResultVO<Long> copyByStep(@RequestBody CopyDataParams params) {
    Assert.notNull(params.getTenantId(), "源租户 ID 不能为空");
    Assert.notNull(params.getResetTenantId(), "目标租户 ID 不能为空");
    Assert.isTrue(!Objects.equals(params.getTenantId(), params.getResetTenantId()), "源租户 ID 不能为空");
    if (params.getSyncAll() != null && BooleanUtils.isFalse(params.getSyncAll())) {
      Assert.notEmpty(params.getCodeAndIds(), "请指定自定义复制数据范围");
    }
    else {
      params.setSyncAll(Boolean.TRUE);
    }
    return dataSyncService.publishCopy(params);
  }

  @Operation(summary = "在线数据发布")
  @PostMapping("publishOnline")
  public ResultVO<Long> publishOnline(@RequestBody OnlinePublishParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(params.getGatewayId(), "网关 ID 不能为空");
    Assert.notNull(params.getSpaceId(), "空间 ID 不能为空");
    if (params.getSyncAll() != null && BooleanUtils.isFalse(params.getSyncAll())) {
      Assert.notEmpty(params.getCodeAndIds(), "请指定自定义导出数据范围");
    }
    else {
      params.setSyncAll(Boolean.TRUE);
    }
    return dataSyncService.publishOnline(params);
  }

  @SuppressWarnings("PMD.PreserveStackTrace")
  @Operation(summary = "流转非自动环节")
  @PostMapping("jumpDataSyncStep")
  public ResultVO<Long> jumpDataSyncStep(@RequestBody JumpStepParams params) {
    Assert.notNull(params.getPublishId(), "日志 ID 不能为空");
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(params.getPublishId(), false, params.getObject()));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("步骤化数据导入异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("步骤化数据导入异常，" + e.getMessage(), e);
    }
    return ResultVO.success(params.getPublishId());
  }

  @Operation(summary = "查看流程进度")
  @GetMapping("queryStepLog")
  public ResultVO<PublishRecordDTO> queryStepLog(@RequestParam("publishId") Long publishId) {
    Assert.notNull(publishId, "日志 ID 不能为空");
    return ResultVO.success(publishService.getRecord(publishId));
  }

  @Operation(summary = "查询同步记录列表（分页）")
  @PostMapping("queryRecordPage")
  public ResultVO<PageInfo<PublishRecordDTO>> queryRecordPage(@RequestBody RecordQueryParams params) {
    return ResultVO.success(publishService.queryRecordPage(params));
  }

  @Operation(summary = "查询复制记录列表（分页）")
  @PostMapping("queryCopyRecordPage")
  public ResultVO<PageInfo<PublishRecordDTO>> queryCopyRecordPage(@RequestBody CopyRecordQueryParams params) {
    return ResultVO.success(publishService.queryCopyRecordPage(params));
  }

  @Operation(summary = "查询数据同步节点信息，用于增量导出")
  @PostMapping("queryDataSyncNodeData")
  public ResultVO<List<DataSyncGroupDTO>> queryDataSyncNodeData(@RequestBody DataSyncQueryParams queryParams) {
    return ResultVO.success(dataSyncService.queryDataSyncNode(queryParams));
  }

  @Operation(summary = "查询选中项关联的配置，用于增量导出")
  @PostMapping("queryRelatedResource")
  public ResultVO<List<DataSyncNodeDTO>> queryRelatedResource(@RequestBody DataSyncQueryParams queryParams) {
    Assert.notEmpty(queryParams.getValues(), "请勾选导出配置项");
    return dataSyncService.queryRelatedResource(queryParams);
  }

  @Operation(summary = "创建数据备份")
  @PostMapping("backup")
  public ResultVO<Long> backup(@RequestBody DataSyncBackupCreateDTO request) {
    return dataSyncBackupService.createBackup(request);
  }

  @Operation(summary = "查询备份记录分页")
  @PostMapping("queryBackupPage")
  public ResultVO<PageInfo<DataSyncBackupDTO>> queryBackupPage(@RequestBody DataSyncBackupPageQuery query) {
    return ResultVO.success(dataSyncBackupService.queryBackupPage(query));
  }

  @Operation(summary = "查询回退记录分页")
  @PostMapping("queryReturnRecordPage")
  public ResultVO<PageInfo<DataSyncReturnRecordDTO>> queryReturnLogPage(@RequestBody DataSyncReturnRecordPageQuery query) {
    return ResultVO.success(dataSyncBackupService.queryReturnLogPage(query));
  }

  @Operation(summary = "按备份回退")
  @PostMapping("returnByBackup")
  public ResultVO<Long> returnByBackup(@RequestBody DataSyncReturnByBackupDTO request) {
    return dataSyncBackupService.returnByBackup(request);
  }
}
