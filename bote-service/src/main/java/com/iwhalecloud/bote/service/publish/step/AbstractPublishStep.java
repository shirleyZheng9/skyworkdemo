package com.iwhalecloud.bote.service.publish.step;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.service.datasync.IDataSyncService;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 步骤执行器：抽象类
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractPublishStep<T> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected static final IPublishService service = SpringUtil.getBean(IPublishService.class);
  protected static final IFileStoreService fileService = SpringUtil.getBean(IFileStoreService.class);
  protected static final IDataSyncService dataSyncService = SpringUtil.getBean(IDataSyncService.class);
  protected final Long publishId;
  protected final PublishRecordDTO record;
  protected final PublishStepDTO step;

  protected AbstractPublishStep(PublishRecordDTO record, PublishStepDTO step) {
    this.publishId = record.getId();
    this.record = record;
    this.step = step;
  }

  /**
   * 步骤入参格式化转换，子类实现
   */
  @Nullable
  public T convertInputParams(@Nullable Object params) {
    return null;
  }

  /**
   * 执行步骤
   *
   * @param auto 是否自动执行
   * @param params 步骤入参
   * @return 是否跳出
   */
  public boolean execute(boolean auto, T params) {
    // 更新状态为运行中
    step.setStepStatus(BaseConsts.PUBLISH_STATUS_RUNNING);
    service.updateStepStatus(step);

    Date startTime = new Date();
    int status = BaseConsts.PUBLISH_STATUS_SUCCESS;
    String failReason = "";
    boolean finished = false;
    try {
      ResultVO<String> result = doExecute(auto, params);
      StepResult stepResult = handleExecuteResult(result);
      status = stepResult.status;
      failReason = stepResult.failReason;
      finished = stepResult.finished;
    }
    catch (Exception e) {
      status = BaseConsts.PUBLISH_STATUS_FAILED;
      failReason = ExpUtil.getMsg(e);
      logger.error("Failed to execute import app. stepId={}, error={}", step.getId(), e.getMessage(), e);
    }
    finally {
      step.setStartTime(startTime);
      step.setEndTime(new Date());
      step.setSpentTime((int) (step.getEndTime().getTime() - step.getStartTime().getTime()));
      step.setStepStatus(status);
      step.setFailReason(failReason);
      service.updateStepStatus(step);

      int publishStatus = status;
      if (BaseConsts.PUBLISH_STATUS_FAILED == status) {
        record.setFailedStepName(step.getStepName());
        record.setFailReason(failReason);
      }
      else if (BaseConsts.PUBLISH_STATUS_NOT_STARTED == status || !finished) {
        // 纠正发布记录状态
        publishStatus = BaseConsts.PUBLISH_STATUS_RUNNING;
      }
      record.setPublishStatus(publishStatus);
      record.setEndTime(new Date());
      record.setSpentTime((int) (record.getEndTime().getTime() - record.getStartTime().getTime()));
      service.updatePublishRecord(record);
    }
    return !Objects.equals(BaseConsts.PUBLISH_STATUS_SUCCESS, status);
  }

  /**
   * 处理执行结果
   */
  private StepResult handleExecuteResult(ResultVO<String> result) {
    if (!result.isSuccess()) {
      String failReason = StringUtils.isNotEmpty(result.getStack()) ? result.getStack() : result.getResultMsg();
      return new StepResult(BaseConsts.PUBLISH_STATUS_FAILED, failReason, false);
    }

    String resultObject = result.getResultObject();
    if ("break".equals(resultObject)) {
      // 手工环节，自动执行前时机的事件后，需要自动跳出，更新状态为未处理
      return new StepResult(BaseConsts.PUBLISH_STATUS_NOT_STARTED, "", false);
    }

    if ("finished".equals(resultObject)) {
      // 流程结束标识
      return new StepResult(BaseConsts.PUBLISH_STATUS_SUCCESS, "", true);
    }

    if (StringUtils.isNotEmpty(resultObject) && resultObject.startsWith("timeout:")) {
      // 超时正常结束，回填数据
      step.setOutputJson(StringUtils.substringAfter(resultObject, "timeout:"));
      return new StepResult(BaseConsts.PUBLISH_STATUS_RUNNING, "", false);
    }

    return new StepResult(BaseConsts.PUBLISH_STATUS_SUCCESS, "", false);
  }

  /**
   * 执行步骤，子类实现
   */
  protected abstract ResultVO<String> doExecute(boolean auto, T params);

  /**
   * 从指定环节，提取出参信息
   *
   * @return 出参信息
   */
  protected <V> V getOutputParams(PublishStepType type, TypeReference<V> typeReference) {
    PublishStepDTO step = IterableUtils.find(CollectionUtils.emptyIfNull(record.getSteps()), p -> Objects.equals(type.getValue(), p.getStepType()));
    if (step != null && StringUtils.isNotEmpty(step.getOutputJson())) {
      return JsonUtil.parseJson(step.getOutputJson(), typeReference);
    }
    return null;
  }

  /**
   * 导入场景，从节点 解析应用数据包，提取出参信息
   *
   * @return 出参信息
   */
  protected DataSyncParams getDataSyncParams() {
    PublishStepDTO step = IterableUtils.find(CollectionUtils.emptyIfNull(record.getSteps()),
      p -> Objects.equals(PublishStepType.PARSE_FILE.getValue(), p.getStepType()));
    if (step != null && StringUtils.isNotEmpty(step.getOutputJson())) {
      DataSyncParams params = JsonUtil.parseJson(step.getOutputJson(), new TypeReference<DataSyncParams>() {
      });
      if (params != null) {
        // 多服务器节点场景，工作空间可能为空，按需重新初始化
        File f = new File(params.getDecompressDir());
        if (!f.exists()) {
          // 文件服务器下载备份数据包，并解压
          File tempFile;
          try {
            tempFile = Files.createTempDirectory("bote-datasync-").resolve(params.getTenantId().toString()).toFile();
            fileService.downloadFile(params.getFileId(), tempFile.getAbsolutePath());
            DataSyncParams datasyncParams = new DataSyncParams();
            ResultVO<Void> result = dataSyncService.parseImportFile(datasyncParams, tempFile);
            if (result.isSuccess()) {
              params.setCompressDir(datasyncParams.getCompressDir());
              params.setDecompressDir(datasyncParams.getDecompressDir());
              // 回填调整后的出参信息
              step.setOutputJson(JsonUtil.toJsonString(params));
            }
          }
          catch (IOException e) {
            logger.error("Failed to parse import file.", e);
          }
        }
        return params;
      }
    }
    return null;
  }

  /**
   * 步骤执行结果
   */
  private record StepResult(int status, String failReason, boolean finished) {

  }
}
