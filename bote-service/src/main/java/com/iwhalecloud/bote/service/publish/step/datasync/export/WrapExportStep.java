package com.iwhalecloud.bote.service.publish.step.datasync.export;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 步骤执行器：封装导出信息
 *
 * @author chen.linfa
 * @since 2024-10-21
 */
public class WrapExportStep extends AbstractDataSyncStep<Object> {

  public WrapExportStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public DataSyncParams convertInputParams(Object params) {
    if (params == null) {
      return null;
    }
    return JsonUtil.parseJson(JsonUtil.toJsonString(params), DataSyncParams.class);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    DataSyncParams params = getOutputParams(PublishStepType.INITIALIZE_EXPORT, new TypeReference<DataSyncParams>() {
    });
    DataSyncDirUtil.compress(params);
    File file = DataSyncDirUtil.getZipFile(params);
    UploadConfigVO config = new UploadConfigVO();
    config.setSubFolder(BooleanUtils.isTrue(params.getBackUp()) ? params.getTenantId() + "/backup" : params.getTenantId() + "/datasync");
    config.setSaveName("");
    config.setOriginalFileName(file.getName());
    FileInfoVO fileInfo = fileService.uploadFile(file, config);
    step.setOutputJson(JsonUtil.toJsonString(ImmutableMap.of("fileId", fileInfo.getFileId().toString())));
    DataSyncDirUtil.clearWorkspace(params);
    return ResultVO.success("finished");
  }

}
