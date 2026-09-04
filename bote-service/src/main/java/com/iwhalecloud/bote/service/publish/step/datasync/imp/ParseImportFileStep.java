package com.iwhalecloud.bote.service.publish.step.datasync.imp;

import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.datasync.query.ImportDataParams;
import com.iwhalecloud.bote.dto.portal.TenantDTO;
import com.iwhalecloud.bote.dto.workspace.WorkspaceDTO;
import com.iwhalecloud.bote.service.portal.ITenantManageService;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bote.service.workspace.IWorkspaceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import org.springframework.util.Assert;

/**
 * 步骤执行器：解析应用数据包
 *
 * @author chen.linfa
 * @since 2024-10-22
 */

public class ParseImportFileStep extends AbstractDataSyncStep<ImportDataParams> {

  public ParseImportFileStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public ImportDataParams convertInputParams(Object params) {
    if (params instanceof ImportDataParams) {
      return (ImportDataParams) params;
    }
    return null;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, ImportDataParams params) {
    Assert.notNull(params, "入参格式异常");
    DataSyncParams datasyncParams = new DataSyncParams();
    File tempFile;
    try {
      tempFile = Files.createTempDirectory("bote-datasync-").resolve(params.getFileName()).toFile();
      fileService.downloadFile(params.getFileId(), tempFile.getAbsolutePath());
    }
    catch (IOException e) {
      return ResultVO.fail("解析数据包内容出现异常");
    }

    ResultVO<Void> result = dataSyncService.parseImportFile(datasyncParams, tempFile);
    if (result.isSuccess()) {
      if (!Objects.equals(params.getTenantId(), datasyncParams.getTenantId())) {
        String validationError = validateTenantImport(params, datasyncParams);
        if (validationError != null) {
          return ResultVO.fail(validationError);
        }
      }
      else if (params.getSpaceId() != null) {
        String validationError = validateSpaceAndTenant(params);
        if (validationError != null) {
          return ResultVO.fail(validationError);
        }
      }
      datasyncParams.setFileId(params.getFileId());
      datasyncParams.setSpaceId(params.getSpaceId());
      datasyncParams.setAutoConfirm(params.getAutoConfirm());
      step.setOutputJson(JsonUtil.toJsonString(datasyncParams));
      return ResultVO.success();
    }
    return ResultVO.fail(result.getResultMsg());
  }

  /**
   * 校验租户导入参数
   *
   * @param params 导入数据参数
   * @param datasyncParams 数据同步参数
   * @return 如果通过校验返回 null，如果未通过校验返回错误信息
   */
  private String validateTenantImport(ImportDataParams params, DataSyncParams datasyncParams) {
    // 管理平台，导入项目时，不带入参 tenantId
    if (params.getTenantId() != null) {
      return "非当前项目数据包，不允许导入，请切换到对应项目再操作";
    }
    TenantDTO tenant = SpringUtil.getBean(ITenantManageService.class).getTenant(datasyncParams.getTenantId());
    if (tenant != null && !Objects.equals(tenant.getSpaceId(), params.getSpaceId())) {
      // 管理平台，导入项目时，限制跨空间导入
      return "检查到数据包对应的项目已加入其他空间，请切换到对应空间再操作";
    }
    return null;
  }

  /**
   * 校验工作空间和租户参数
   *
   * @param params 导入数据参数
   * @return 如果通过校验返回 null，如果未通过校验返回错误信息
   */
  private String validateSpaceAndTenant(ImportDataParams params) {
    // 校验 spaceId 是否存在
    IWorkspaceManageService workspaceManageService = SpringUtil.getBean(IWorkspaceManageService.class);
    WorkspaceDTO workspace = workspaceManageService.getWorkspace(params.getSpaceId());
    if (workspace == null) {
      return "工作空间不存在，请检查 spaceId 是否正确";
    }

    // 如果提供了 tenantId，校验 tenantId 是否挂载在 spaceId 下
    if (params.getTenantId() != null) {
      ITenantManageService tenantManageService = SpringUtil.getBean(ITenantManageService.class);
      TenantDTO tenant = tenantManageService.getTenant(params.getTenantId());
      if (tenant == null) {
        return null;
      }
      if (!Objects.equals(tenant.getSpaceId(), params.getSpaceId())) {
        return "租户不属于指定的工作空间，请检查 tenantId 和 spaceId 的对应关系";
      }
    }
    return null;
  }
}
