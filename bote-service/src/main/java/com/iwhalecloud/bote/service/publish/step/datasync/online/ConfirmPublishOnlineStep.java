package com.iwhalecloud.bote.service.publish.step.datasync.online;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncParams;
import com.iwhalecloud.bote.dto.publish.PublishConfirmStepParams;
import com.iwhalecloud.bote.dto.publish.PublishGatewaySimpleDTO;
import com.iwhalecloud.bote.service.datasync.util.DataSyncDirUtil;
import com.iwhalecloud.bote.service.publish.step.datasync.AbstractDataSyncStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * 步骤执行器：发布并确认发布（在线发布）
 *
 * @author lizuyin
 * @since 2026-01-21
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class ConfirmPublishOnlineStep extends AbstractDataSyncStep<Object> {

  /** 目标环境 importByStep 接口相对路径（不含网关前缀） */
  private static final String IMPORT_BY_STEP_PATH = "/bote/manager/datasync/importByStep";
  /** 目标环境 queryStepLog 接口相对路径（不含网关前缀） */
  private static final String QUERY_STEP_LOG_PATH = "/bote/manager/datasync/queryStepLog";

  /** 轮询状态占位标记：表示“仍在处理中，需要继续轮询” */
  private static final String POLLING_FLAG = "__POLLING__";

  public ConfirmPublishOnlineStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, Object object) {
    try {
      // 1. 从初始化步骤获取数据同步参数和网关信息
      Map<String, Object> initOutput = getOutputParams(PublishStepType.INITIALIZE_ONLINE, new TypeReference<>() {
      });
      Assert.notNull(initOutput, "初始化步骤的输出信息解析失败");
      Object dataSyncParamsObj = initOutput.get("dataSyncParams");
      Assert.notNull(dataSyncParamsObj, "数据同步参数解析失败");
      DataSyncParams datasyncParams = JsonUtil.parseJson(JsonUtil.toJsonString(dataSyncParamsObj), DataSyncParams.class);
      Assert.notNull(datasyncParams, "数据同步参数解析失败");
      PublishGatewaySimpleDTO gateway = JsonUtil.parseJson(JsonUtil.toJsonString(initOutput.get("gateway")), PublishGatewaySimpleDTO.class);
      Assert.notNull(gateway, "网关信息解析失败");

      // 2. 进行压缩操作
      DataSyncDirUtil.compress(datasyncParams);
      File file = DataSyncDirUtil.getZipFile(datasyncParams);
      if (!file.exists() || !file.isFile()) {
        return ResultVO.fail("压缩文件不存在: " + file.getAbsolutePath());
      }

      try {
        // 3. 调用目标环境的importByStep接口进行发布（自动确认）
        ResultVO<Long> importResult = callImportByStepApi(gateway, datasyncParams, file);
        if (!importResult.isSuccess()) {
          return ResultVO.fail(importResult.getResultMsg());
        }

        // 4. 保存发布结果
        Long targetPublishId = importResult.getResultObject();

        // 5. 轮询目标环境发布状态，等待自动确认完成
        String waitResult = waitForTargetPublishFinished(targetPublishId, gateway);
        // 6. 如果轮询结果为超时，则返回成功
        if ("timeout".equals(waitResult)) {
          PublishConfirmStepParams confirmStepParams = new PublishConfirmStepParams();
          confirmStepParams.setGatewayId(gateway.getGatewayId());
          confirmStepParams.setPublishId(targetPublishId);
          confirmStepParams.setGatewayUrl(gateway.getGatewayUrl());
          confirmStepParams.setGatewayToken(gateway.getGatewayToken());
          confirmStepParams.setTenantId(gateway.getTenantId());
          return ResultVO.success("timeout:" + JsonUtil.toJsonString(confirmStepParams));
        }
        if (waitResult != null) {
          return ResultVO.fail(waitResult);
        }

        return ResultVO.success("finished");
      }
      finally {
        // 6. 清理工作空间
        DataSyncDirUtil.clearWorkspace(datasyncParams);
      }
    }
    catch (HttpStatusCodeException e) {
      logger.error("HTTP request failed: status={}, response={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
      return ResultVO.fail("HTTP请求失败: status=" + e.getStatusCode() + ", error=" + e.getResponseBodyAsString());
    }
    catch (Exception e) {
      logger.error("Publish and confirm failed", e);
      return ResultVO.fail("发布确认失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
    }
  }

  /**
   * 构造multipart/form-data请求并调用importByStep接口
   */
  private ResultVO<Long> callImportByStepApi(PublishGatewaySimpleDTO gateway, DataSyncParams datasyncParams, File tempFile) {
    // 构造multipart/form-data请求
    MultiValueMap<String, Object> requestBody = buildMultipartRequestBody(datasyncParams, tempFile);
    HttpHeaders headers = buildMultipartRequestHeaders(gateway);
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

    // 构建目标URL
    String targetUrl = StringUtils.stripEnd(gateway.getGatewayUrl(), "/") + IMPORT_BY_STEP_PATH;

    // 调用接口
    RestTemplate restTemplate = HttpUtil.getRestTemplate();
    ResponseEntity<ResultVO<Long>> responseEntity = restTemplate.exchange(targetUrl, HttpMethod.POST, requestEntity,
      new ParameterizedTypeReference<>() {
      });

    ResultVO<Long> response = responseEntity.getBody();
    if (response != null && response.isSuccess()) {
      Long targetPublishId = response.getResultObject();
      return ResultVO.success(targetPublishId);
    }
    else {
      String errorMsg = response != null ? response.getResultMsg() : "未知错误";
      logger.error("importByStep failed: gatewayId={}, error={}", gateway.getGatewayId(), errorMsg);
      return ResultVO.fail("发布失败: " + errorMsg);
    }
  }

  /**
   * 轮询目标环境中本次在线发布事务的状态，直到完成（成功或失败）或超时
   *
   * @param targetPublishId 目标环境发布记录 ID
   * @param gateway 目标网关信息
   * @return {@code null} 表示成功完成；否则返回错误提示信息
   */
  @SuppressWarnings("BusyWait")
  private String waitForTargetPublishFinished(Long targetPublishId, PublishGatewaySimpleDTO gateway) {
    // 轮询间隔（毫秒）
    int pollInterval = 3000;
    // 超时时间（毫秒），默认 20 秒
    int maxWaitTime = 20000;
    long startTime = System.currentTimeMillis();

    String queryUrl = StringUtils.stripEnd(gateway.getGatewayUrl(), "/") + QUERY_STEP_LOG_PATH + "?publishId=" + targetPublishId;
    RestTemplate restTemplate = HttpUtil.getRestTemplate();
    HttpEntity<Void> requestEntity = buildRequestEntity(gateway);

    try {
      do {
        String result = checkPublishStatus(restTemplate, queryUrl, requestEntity, targetPublishId, gateway);
        // result 为 null 表示发布成功；非空但不为轮询标记表示失败信息；等于轮询标记则继续轮询
        if (result == null || !Objects.equals(result, POLLING_FLAG)) {
          return result;
        }

        Thread.sleep(pollInterval);
      }
      while (System.currentTimeMillis() - startTime < maxWaitTime);

      logger.warn("Waiting for target publish finished timeout: targetPublishId={}, gatewayId={}, waitTime={}ms", targetPublishId,
        gateway.getGatewayId(), System.currentTimeMillis() - startTime);
      return "timeout";
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("Waiting for target publish finished interrupted: targetPublishId={}, gatewayId={}", targetPublishId, gateway.getGatewayId(), e);
      return "等待目标环境在线发布结果被中断，请稍后重试";
    }
    catch (Exception e) {
      logger.error("Waiting for target publish finished error: targetPublishId={}, gatewayId={}", targetPublishId, gateway.getGatewayId(), e);
      return "等待目标环境在线发布结果异常: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
    }
  }

  /**
   * 构建HTTP请求实体
   *
   * @param gateway 网关信息
   * @return HTTP请求实体
   */
  private HttpEntity<Void> buildRequestEntity(PublishGatewaySimpleDTO gateway) {
    HttpHeaders headers = new HttpHeaders();
    if (StringUtils.isNotEmpty(gateway.getGatewayToken())) {
      headers.set(BaseConsts.HEADER_AUTHORIZATION, gateway.getGatewayToken());
    }
    return new HttpEntity<>(headers);
  }

  /**
   * 检查发布状态
   *
   * @param restTemplate REST模板
   * @param queryUrl 查询URL
   * @param requestEntity 请求实体
   * @param targetPublishId 目标发布ID
   * @param gateway 网关信息
   * @return {@code null} 表示发布成功；非空字符串表示失败原因；{@link #POLLING_FLAG} 表示仍在处理中需要继续轮询
   */
  private String checkPublishStatus(RestTemplate restTemplate, String queryUrl, HttpEntity<Void> requestEntity, Long targetPublishId,
    PublishGatewaySimpleDTO gateway) {
    ResponseEntity<ResultVO<PublishRecordDTO>> responseEntity = restTemplate.exchange(queryUrl, HttpMethod.GET, requestEntity,
      new ParameterizedTypeReference<>() {
      });
    ResultVO<PublishRecordDTO> body = responseEntity.getBody();
    if (body != null && body.isSuccess() && body.getResultObject() != null) {
      PublishRecordDTO record = body.getResultObject();
      Integer status = record.getPublishStatus();

      if (Objects.equals(BaseConsts.PUBLISH_STATUS_SUCCESS, status)) {
        logger.info("Target publish finished successfully: targetPublishId={}, gatewayId={}", targetPublishId, gateway.getGatewayId());
        // 成功：返回 null，由外层方法识别为成功结束
        return null;
      }
      if (Objects.equals(BaseConsts.PUBLISH_STATUS_FAILED, status)) {
        String failReason = record.getFailReason();
        logger.error("Target publish failed: targetPublishId={}, gatewayId={}, failReason={}", targetPublishId, gateway.getGatewayId(), failReason);
        return "目标环境在线发布失败: " + (failReason != null ? failReason : "未知原因");
      }
      // 其他状态继续轮询
      return POLLING_FLAG;
    }
    else if (body != null && !body.isSuccess()) {
      logger.warn("Query target publish status failed: targetPublishId={}, gatewayId={}, error={}", targetPublishId, gateway.getGatewayId(),
        body.getResultMsg());
      // 查询失败，但不是明确的发布失败，继续轮询
      return POLLING_FLAG;
    }
    // 没有获取到有效响应，继续轮询
    return POLLING_FLAG;
  }

  /**
   * 构造multipart请求体
   */
  private MultiValueMap<String, Object> buildMultipartRequestBody(DataSyncParams datasyncParams, File tempFile) {
    MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
    requestBody.add("file", new FileSystemResource(tempFile));
    if (datasyncParams.getTenantId() != null) {
      requestBody.add("tenantId", String.valueOf(datasyncParams.getTenantId()));
    }
    requestBody.add("spaceId", String.valueOf(datasyncParams.getSpaceId()));
    requestBody.add("autoConfirm", "true");
    return requestBody;
  }

  /**
   * 构造multipart请求头
   */
  private HttpHeaders buildMultipartRequestHeaders(PublishGatewaySimpleDTO gateway) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    if (StringUtils.isNotEmpty(gateway.getGatewayToken())) {
      headers.set(BaseConsts.HEADER_AUTHORIZATION, gateway.getGatewayToken());
    }
    return headers;
  }
}

