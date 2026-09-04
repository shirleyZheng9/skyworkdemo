package com.iwhalecloud.bote.service.bot.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.dto.bot.PlatSceneInfoDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatSceneInfoQueryParams;
import com.iwhalecloud.bote.dto.datasync.DataSyncNodeDTO;
import com.iwhalecloud.bote.dto.datasync.query.CopyDataParams;
import com.iwhalecloud.bote.dto.datasync.query.DataSyncQueryParams;
import com.iwhalecloud.bote.dto.scene.SimpleSceneDTO;
import com.iwhalecloud.bote.mapper.bot.PlatSceneInfoManageMapper;
import com.iwhalecloud.bote.service.base.ILabelManageService;
import com.iwhalecloud.bote.service.bot.IBotSceneManageService;
import com.iwhalecloud.bote.service.bot.IPlatSceneInfoManageService;
import com.iwhalecloud.bote.service.datasync.IDataSyncService;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板智能体管理服务实现
 *
 * @author auto
 * @since 2025-06-21
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class PlatSceneInfoManageServiceImpl implements IPlatSceneInfoManageService {
  // @formatter:off
  private final Logger logger = LoggerFactory.getLogger(PlatSceneInfoManageServiceImpl.class);
  private final PlatSceneInfoManageMapper platSceneInfoManageMapper;
  private final IDataSyncService dataSyncService;
  private final ILabelManageService labelManageService;
  private final IBotSceneManageService sceneManageService;
  private final IPublishService publishService;
  // @formatter:on

  @Override
  public PlatSceneInfoDTO findPlatSceneInfo(Long platSceneId) {
    PlatSceneInfoDTO platSceneInfo = platSceneInfoManageMapper.getPlatSceneInfo(platSceneId, BaseConsts.PLATFORM_TENANT_ID);

    if (platSceneInfo != null && platSceneInfo.getSceneId() != null) {
      // 补充标签信息 - 查询平台租户和资产租户的标签关联
      List<LabelObjectRelDTO> labels = new ArrayList<>();
      labels.addAll(labelManageService.queryLabelObjectRelList(Collections.singletonList(platSceneInfo.getSceneId()), BaseConsts.LABEL_TYPE_SCENE,
        BaseConsts.PLATFORM_TENANT_ID));
      labels.addAll(labelManageService.queryLabelObjectRelList(Collections.singletonList(platSceneInfo.getSceneId()), BaseConsts.LABEL_TYPE_SCENE,
        BaseConsts.ASSET_TENANT_ID));
      // 将多个标签名用逗号连接
      platSceneInfo.setLabels(labels);
    }

    return platSceneInfo;
  }

  @Override
  @Transactional
  public ResultVO<PlatSceneInfoDTO> savePlatSceneInfo(PlatSceneInfoDTO platSceneInfo) {
    // 第一步：校验
    // 平台级功能：强制设置租户ID为-1
    platSceneInfo.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    platSceneInfo.setStatusCd(BaseConsts.STATUS_CD_VALID);

    boolean isUpdate = platSceneInfo.getPlatSceneId() != null;
    PlatSceneInfoDTO old = null;

    // 校验逻辑
    if (isUpdate) {
      // 更新校验：检查记录是否存在
      old = platSceneInfoManageMapper.getPlatSceneInfo(platSceneInfo.getPlatSceneId(), BaseConsts.PLATFORM_TENANT_ID);
      if (old == null) {
        return ResultVO.fail("未找到对应的模板智能体，无法更新");
      }
    }
    else {
      // 新增校验：检查场景是否已存在有效的模板
      if (platSceneInfoManageMapper.existsPlatSceneInfo(platSceneInfo.getSceneId(), BaseConsts.PLATFORM_TENANT_ID, null)) {
        return ResultVO.fail("该智能体已存在模板，请勿重复添加");
      }

      // 检查是否有已删除的记录，如果有则恢复
      PlatSceneInfoDTO deletedRecord = platSceneInfoManageMapper.findDeletedPlatSceneInfo(platSceneInfo.getSceneId(), BaseConsts.PLATFORM_TENANT_ID);
      if (deletedRecord != null) {
        old = deletedRecord;
        // 恢复已删除的记录：使用原有的模板ID
        platSceneInfo.setPlatSceneId(deletedRecord.getPlatSceneId());
      }
    }

    // 第二步：赋值
    // 设置更新者ID
    platSceneInfo.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    // 处理排序字段
    if (isUpdate) {
      if (platSceneInfo.getSortOrder() == null) {
        platSceneInfo.setSortOrder(old.getSortOrder());
      }
    }
    else {
      if (platSceneInfo.getSortOrder() == null) {
        platSceneInfo.setSortOrder(0);
      }
    }

    // 第三步：数据库执行
    DataDifference<PlatSceneInfoDTO> difference = DataDifferenceStarter.computeSave(old, platSceneInfo, false, null);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePlatSceneInfo(Long platSceneId) {
    platSceneInfoManageMapper.deletePlatSceneInfo(platSceneId, BaseConsts.PLATFORM_TENANT_ID, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> topPlatSceneInfo(Long platSceneId) {
    PlatSceneInfoDTO template = platSceneInfoManageMapper.getPlatSceneInfo(platSceneId, BaseConsts.PLATFORM_TENANT_ID);
    if (template == null) {
      return BaseErrorConstant.NOT_EXIST.toResult(platSceneId);
    }

    // 获取当前最大排序值并+1，实现置顶
    Integer maxSortOrder = platSceneInfoManageMapper.getMaxSortOrder(BaseConsts.PLATFORM_TENANT_ID);
    template.setSortOrder(maxSortOrder + 1);
    template.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    platSceneInfoManageMapper.updatePlatSceneInfo(template);
    return ResultVO.success();
  }

  @Override
  public List<PlatSceneInfoDTO> queryPlatSceneInfoList(PlatSceneInfoQueryParams queryParams) {
    // 租户ID为-1
    queryParams.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    List<PlatSceneInfoDTO> list = platSceneInfoManageMapper.selectPlatSceneInfoList(queryParams);

    if (CollectionUtils.isNotEmpty(list)) {
      // 补充标签信息 - 查询平台租户和资产租户的标签关联
      List<Long> sceneIds = list.stream().map(PlatSceneInfoDTO::getSceneId).collect(Collectors.toList());
      List<LabelObjectRelDTO> allLabels = new ArrayList<>();
      allLabels.addAll(labelManageService.queryLabelObjectRelList(sceneIds, BaseConsts.LABEL_TYPE_SCENE, BaseConsts.PLATFORM_TENANT_ID));
      allLabels.addAll(labelManageService.queryLabelObjectRelList(sceneIds, BaseConsts.LABEL_TYPE_SCENE, BaseConsts.ASSET_TENANT_ID));

      Map<Long, List<LabelObjectRelDTO>> labelGroup = CollectionUtils.emptyIfNull(allLabels).stream()
        .collect(Collectors.groupingBy(LabelObjectRelDTO::getObjectId));

      for (PlatSceneInfoDTO item : list) {
        item.setLabels(labelGroup.get(item.getSceneId()));
      }
    }

    return list;
  }

  @Override
  public PageInfo<PlatSceneInfoDTO> queryPlatSceneInfoPage(PlatSceneInfoQueryParams queryParams) {
    // 租户ID为-1
    queryParams.setTenantId(BaseConsts.PLATFORM_TENANT_ID);
    RowBounds rowBounds = queryParams.buildRowBounds();
    // noinspection resource
    PageInfo<PlatSceneInfoDTO> pageInfo = platSceneInfoManageMapper.selectPlatSceneInfoPage(queryParams, rowBounds).toPageInfo();

    if (CollectionUtils.isNotEmpty(pageInfo.getList())) {
      // 补充标签信息 - 查询平台租户和资产租户的标签关联
      List<Long> sceneIds = pageInfo.getList().stream().map(PlatSceneInfoDTO::getSceneId).collect(Collectors.toList());
      List<LabelObjectRelDTO> allLabels = new ArrayList<>();
      allLabels.addAll(labelManageService.queryLabelObjectRelList(sceneIds, BaseConsts.LABEL_TYPE_SCENE, BaseConsts.PLATFORM_TENANT_ID));
      allLabels.addAll(labelManageService.queryLabelObjectRelList(sceneIds, BaseConsts.LABEL_TYPE_SCENE, BaseConsts.ASSET_TENANT_ID));

      Map<Long, List<LabelObjectRelDTO>> labelGroup = CollectionUtils.emptyIfNull(allLabels).stream()
        .collect(Collectors.groupingBy(LabelObjectRelDTO::getObjectId));

      for (PlatSceneInfoDTO item : pageInfo.getList()) {
        item.setLabels(labelGroup.get(item.getSceneId()));
      }
    }

    return pageInfo;
  }

  @Override
  public List<PlatSceneInfoDTO> queryAvailableScenes(String searchContent) {
    // 资产租户默认是2 只能选择资产租户的智能体
    return platSceneInfoManageMapper.selectAvailableScenes(BaseConsts.ASSET_TENANT_ID, searchContent);
  }

  @Override
  public ResultVO<BotSceneDTO> copyPlatScene(Long tenantId, Long platSceneId) {
    PlatSceneInfoDTO platSceneInfo = findPlatSceneInfo(platSceneId);
    if (platSceneInfo == null) {
      return ResultVO.fail("模板智能体不存在");
    }
    Map<String, String> codeAndIds = new HashMap<>();
    buildCompleteParams(platSceneInfo, codeAndIds);
    codeAndIds.put(DataSyncCodeEnum.SCENE.getCode(), platSceneInfo.getSceneId().toString());
    CopyDataParams params = new CopyDataParams();
    params.setTenantId(BaseConsts.ASSET_TENANT_ID);
    params.setResetTenantId(tenantId);
    params.setSyncAll(false);
    params.setRelatable(true);
    params.setCodeAndIds(codeAndIds);
    params.setResetPrimaryKey(true);
    ResultVO<Long> result = dataSyncService.publishCopy(params);
    if (!result.isSuccess()) {
      return ResultVO.fail(result.getResultMsg());
    }
    // 轮询方式查看进度
    boolean finished = waitCopyFinish(result.getResultObject(), 60000);
    if (!finished) {
      return ResultVO.fail("复制处理时间超时，请稍后重试");
    }
    // 获取复制后的主键映射关系
    PublishRecordDTO record = publishService.getRecord(result.getResultObject());
    PublishStepDTO step = IterableUtils.find(record.getSteps(), p -> Objects.equals(PublishStepType.SAVE_DATA_FOR_COPY.getValue(), p.getStepType()));
    Map<String, Map<Long, Long>> primaryKeyMappings = new HashMap<>();
    if (StringUtils.isNotEmpty(step.getOutput())) {
      primaryKeyMappings = JsonUtil.parseJson(step.getOutput(), new TypeReference<Map<String, Map<Long, Long>>>() {
      });
    }
    // 提取复制后的智能体信息
    BotSceneDTO scene = null;
    String tableCode = DataSyncCodeEnum.SCENE.getTableCode();
    if (MapUtils.isNotEmpty(primaryKeyMappings) && primaryKeyMappings.containsKey(tableCode)) {
      Long newSceneId = primaryKeyMappings.get(tableCode).get(platSceneInfo.getSceneId());
      if (newSceneId != null) {
        scene = sceneManageService.getScene(tenantId, newSceneId, false);
      }
    }
    return scene != null ? ResultVO.success(scene) : ResultVO.fail("复制过程出现异常，请联系系统管理员");
  }

  /** 构建导出所需完整的入参 */
  private void buildCompleteParams(PlatSceneInfoDTO platSceneInfo, Map<String, String> codeAndIds) {
    // 查询关联配置
    DataSyncQueryParams queryParams = new DataSyncQueryParams();
    queryParams.setCode(DataSyncCodeEnum.SCENE.getCode());
    queryParams.setTenantId(BaseConsts.ASSET_TENANT_ID);
    queryParams.setValues(Collections.singletonList(platSceneInfo.getSceneId()));
    ResultVO<List<DataSyncNodeDTO>> listResultVO = dataSyncService.queryRelatedResource(queryParams);

    if (listResultVO.isSuccess() && listResultVO.getResultObject() != null) {
      List<DataSyncNodeDTO> nodes = listResultVO.getResultObject();

      for (DataSyncNodeDTO node : nodes) {
        if (node.getRecords() != null && !node.getRecords().isEmpty()) {
          List<String> ids = new ArrayList<>();
          for (Map<String, Object> record : node.getRecords()) {
            Object id = record.get("id");
            if (id != null) {
              ids.add(id.toString());
            }
          }
          if (!ids.isEmpty()) {
            codeAndIds.put(node.getCode(), String.join("/", ids));
          }
        }
      }
    }
  }

  @SuppressWarnings("BusyWait")
  private boolean waitCopyFinish(Long publishId, int maxWaitTime) {
    boolean isFinish = false;
    // 轮询间隔（毫秒）
    int pollInterval = 3000;
    // 超时时间（毫秒）
    if (maxWaitTime <= 0) {
      maxWaitTime = 60000;
    }
    long startTime = System.currentTimeMillis();
    try {
      do {
        PublishRecordDTO record = publishService.getRecord(publishId);
        isFinish = Objects.equals(BaseConsts.PUBLISH_STATUS_SUCCESS, record.getPublishStatus()) || Objects.equals(BaseConsts.PUBLISH_STATUS_FAILED,
          record.getPublishStatus());
        if (isFinish) {
          break;
        }
        Thread.sleep(pollInterval);
      }
      while (System.currentTimeMillis() - startTime < maxWaitTime);
    }
    catch (InterruptedException e) {
      logger.error("Failed to poll query copy state, interrupted: publishId={}", publishId, e);
      Thread.currentThread().interrupt();
    }
    catch (BssException | IllegalStateException | IllegalArgumentException e) {
      logger.error("Failed to poll query copy state: interrupted={}, error={}", publishId, e.getMessage());
    }
    catch (RuntimeException e) {
      logger.error("Failed to poll query copy state: publishId={}, error={}", publishId, e.getMessage(), e);
    }
    return isFinish;
  }

  @Override
  public PageInfo<SimpleSceneDTO> querySimpleScenePage(PlatSceneInfoQueryParams queryParams) {
    // noinspection resource
    return platSceneInfoManageMapper.selectSimpleScenePage(queryParams, queryParams.buildRowBounds()).toPageInfo();
  }
}
