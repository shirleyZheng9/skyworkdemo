package com.iwhalecloud.bote.service.publish.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.base.query.CopyRecordQueryParams;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bote.dto.publish.PublishConfirmStepParams;
import com.iwhalecloud.bote.mapper.base.PublishManageMapper;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bote.service.publish.PublishStepFactory;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 发布管理服务实现
 *
 * @author chen.linfa
 * @since 2025-02-17
 */
@Service
@RequiredArgsConstructor
public class PublishServiceImpl implements IPublishService {

  private final PublishManageMapper mapper;

  private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);

  @Override
  public PublishRecordDTO buildRecord(String type, List<PublishStepType> steps) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long publishId = IDUtils.nextId();
    PublishRecordDTO record = new PublishRecordDTO();
    record.setId(publishId);
    record.setPublishType(type);
    record.setPublishStatus(BaseConsts.PUBLISH_STATUS_NOT_STARTED);
    record.setStartTime(new Date());

    record.setStatusCd(BaseConsts.STATUS_CD_VALID);
    record.setCreatorId(userId);
    record.setUpdatorId(userId);

    List<PublishStepDTO> list = new ArrayList<>();
    for (int i = 0; i < steps.size(); i++) {
      PublishStepType stepType = steps.get(i);
      PublishStepDTO step = new PublishStepDTO();
      step.setId(IDUtils.nextId());
      step.setPublishId(publishId);
      step.setStepStatus(BaseConsts.PUBLISH_STATUS_NOT_STARTED);
      step.setStepType(stepType.getValue());
      step.setStepName(stepType.getName());
      step.setStepIndex(i + 1);

      step.setStatusCd(BaseConsts.STATUS_CD_VALID);
      step.setCreatorId(userId);
      step.setUpdatorId(userId);
      list.add(step);
    }
    record.setSteps(list);
    return record;
  }

  @Override
  public PublishRecordDTO getRecord(Long publishId) {
    PublishRecordDTO record = mapper.getRecord(publishId);
    Assert.notNull(record, "发布记录不存在");
    List<PublishStepDTO> publishSteps = mapper.selectStepList(publishId);
    for (PublishStepDTO p : publishSteps) {
      p.setOutput(p.getOutputJson());
    }
    record.setSteps(publishSteps);
    record.setPublishStatus(updateOnlinePublishStatus(record, publishSteps));
    return record;
  }

  @Override
  @Transactional
  public void updatePublishRecord(PublishRecordDTO record) {
    mapper.updateRecord(record);
  }

  @Override
  @Transactional
  public void updateStepStatus(PublishStepDTO step) {
    mapper.updateStepStatus(step);
  }

  @Override
  @Transactional
  public PageInfo<PublishRecordDTO> queryRecordPage(RecordQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    //noinspection resource
    PageInfo<PublishRecordDTO> page = mapper.selectRecordPage(params, rowBounds).toPageInfo();
    for (PublishRecordDTO dto : CollectionUtils.emptyIfNull(page.getList())) {
      if (StringUtils.isNotEmpty(dto.getFileInfo())) {
        Map<String, Object> info = JsonUtil.parseJson(dto.getFileInfo(), new TypeReference<Map<String, Object>>() {
        });
        dto.setFileInfo(MapUtils.getString(info, "fileId"));
      }
      if (StringUtils.isNotEmpty(dto.getInput())) {
        Map<String, Object> info = JsonUtil.parseJson(dto.getInput(), new TypeReference<Map<String, Object>>() {
        });
        dto.setInputJson(info);
        dto.setInput(null);
      }
      dto.setPublishStatus(updateOnlinePublishStatus(dto, null));
    }
    return page;
  }

  /**
   * 查询在线发布数据实时状态
   */
  @Override
  public Integer updateOnlinePublishStatus(PublishRecordDTO record, @Nullable List<PublishStepDTO> stepList) {
    Long publishId = record.getId();
    Integer currentStatus = record.getPublishStatus();
    // 判断是否需要更新在线发布状态
    if (!shouldUpdateOnlineStatus(record, currentStatus)) {
      return currentStatus;
    }
    // 获取步骤列表
    List<PublishStepDTO> steps = CollectionUtils.isNotEmpty(stepList) ? stepList : mapper.selectStepList(publishId);
    // 提取确认发布参数
    PublishConfirmStepParams params = extractConfirmParams(steps);
    if (params == null) {
      return currentStatus;
    }
    // 查询网关端发布状态
    PublishRecordDTO remotePublishRecord = queryGatewayPublishStatus(params);
    if (remotePublishRecord == null) {
      return currentStatus;
    }
    Integer remoteStatus = remotePublishRecord.getPublishStatus();
    if (!isFinalStatus(remoteStatus)) {
      return currentStatus;
    }
    updatePublishRecordStatus(record, publishId, remoteStatus, remotePublishRecord);
    return remoteStatus;
  }

  /**
   * 判断是否需要更新在线发布状态
   */
  private boolean shouldUpdateOnlineStatus(PublishRecordDTO record, Integer currentStatus) {
    if (!Objects.equals(record.getPublishType(), BaseConsts.PUBLISH_TYPE_ONLINE)) {
      return false;
    }
    return Objects.equals(currentStatus, BaseConsts.PUBLISH_STATUS_RUNNING);
  }

  /**
   * 从步骤列表中提取确认发布参数
   */
  @Nullable
  private PublishConfirmStepParams extractConfirmParams(List<PublishStepDTO> steps) {
    if (CollectionUtils.isEmpty(steps)) {
      return null;
    }
    PublishStepDTO confirmStep = IterableUtils.find(steps,
      step -> Objects.equals(PublishStepType.CONFIRM_PUBLISH_ONLINE.getValue(), step.getStepType()));
    if (confirmStep == null || StringUtils.isEmpty(confirmStep.getOutputJson())) {
      return null;
    }
    return JsonUtil.parseJson(confirmStep.getOutputJson(), PublishConfirmStepParams.class);
  }

  /**
   * 判断是否为最终状态（成功或失败）
   */
  private boolean isFinalStatus(Integer status) {
    return Objects.equals(status, BaseConsts.PUBLISH_STATUS_SUCCESS)
      || Objects.equals(status, BaseConsts.PUBLISH_STATUS_FAILED);
  }

  /**
   * 更新发布记录状态
   */
  private void updatePublishRecordStatus(PublishRecordDTO record, Long publishId, Integer remoteStatus,
    PublishRecordDTO remotePublishRecord) {
    record.setId(publishId);
    record.setPublishStatus(remoteStatus);
    record.setEndTime(remotePublishRecord.getEndTime());
    record.setSpentTime((int) (remotePublishRecord.getEndTime().getTime() - record.getStartTime().getTime()));
    mapper.updateRecord(record);
    PublishStepDTO confirmStep = IterableUtils.find(mapper.selectStepList(publishId),
      step -> Objects.equals(PublishStepType.CONFIRM_PUBLISH_ONLINE.getValue(), step.getStepType()));
    if (confirmStep != null) {
      confirmStep.setStepStatus(remoteStatus);
      confirmStep.setEndTime(remotePublishRecord.getEndTime());
      confirmStep.setSpentTime((int) (remotePublishRecord.getEndTime().getTime() - confirmStep.getStartTime().getTime()));
      mapper.updateStepStatus(confirmStep);
    }
  }


  @Override
  public List<PublishRecordDTO> queryOnlinePublishRecordWithRunning() {
    return mapper.selectOnlinePublishRecordWithRunning();
  }

  /**
   * 查询网关端发布状态
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  private PublishRecordDTO queryGatewayPublishStatus(PublishConfirmStepParams params) {
    String queryUrl = StringUtils.stripEnd(params.getGatewayUrl(), "/")
      + "/bote/manager/datasync/queryStepLog?publishId="
      + params.getPublishId();
    HttpHeaders headers = new HttpHeaders();
    if (StringUtils.isNotEmpty(params.getGatewayToken())) {
      headers.set(BaseConsts.HEADER_AUTHORIZATION, params.getGatewayToken());
    }
    ResultVO<PublishRecordDTO> result = HttpUtil.get(queryUrl, null, new ParameterizedTypeReference<>() {
    }, headers);

    if (result == null) {
      logger.debug("Query publish status failed, result = null");
      return null;
    }
    if (!result.isSuccess()) {
      logger.debug("Failed to query publish status: {}", result.getResultMsg());
      return null;
    }
    return result.getResultObject();
  }

  @Override
  public PageInfo<PublishRecordDTO> queryCopyRecordPage(CopyRecordQueryParams params) {
    // 只查询当前用户的复制记录
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    //noinspection resource
    PageInfo<PublishRecordDTO> page = mapper.selectCopyRecordPage(params, params.buildRowBounds()).toPageInfo();
    for (PublishRecordDTO dto : CollectionUtils.emptyIfNull(page.getList())) {
      if (StringUtils.isNotEmpty(dto.getInput())) {
        Map<String, Object> info = JsonUtil.parseJson(dto.getInput(), new TypeReference<Map<String, Object>>() {
        });
        dto.setInputJson(info);
        dto.setInput(null);
      }
    }
    return page;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public ResultVO<Void> start(Long publishId, boolean auto, @Nullable Object params) {
    PublishRecordDTO record = getRecord(publishId);
    Assert.notNull(record, "查询不到有效的记录");
    Assert.isTrue(Arrays.asList(BaseConsts.PUBLISH_STATUS_NOT_STARTED, BaseConsts.PUBLISH_STATUS_RUNNING).contains(record.getPublishStatus()),
      "流程正在处理中，无需重复操作");

    boolean flag = IterableUtils.matchesAny(CollectionUtils.emptyIfNull(record.getSteps()),
      p -> Objects.equals(BaseConsts.PUBLISH_STATUS_RUNNING, p.getStepStatus()));
    Assert.isTrue(!flag, "流程正在处理中，无需重复操作");

    List<PublishStepDTO> steps = CollectionUtils.emptyIfNull(record.getSteps()).stream()
      .filter(p -> Objects.equals(BaseConsts.PUBLISH_STATUS_NOT_STARTED, p.getStepStatus())).collect(Collectors.toList());
    Assert.notEmpty(steps, "流程已处理，无需重复操作");

    for (int i = 0; i < steps.size(); i++) {
      AbstractPublishStep runner = PublishStepFactory.create(record, steps.get(i));
      boolean skip = runner.execute(i != 0 || auto, i == 0 ? runner.convertInputParams(params) : null);
      if (skip) {
        break;
      }
    }
    return ResultVO.success();
  }

  @Transactional
  @Override
  public void clearRunningPublishStatus() {
    Date maxUpdatedTime = DateUtils.addMinutes(new Date(), -30);
    mapper.updateRecordForClear(maxUpdatedTime);
  }
}
