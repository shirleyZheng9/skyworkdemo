package com.iwhalecloud.bote.service.model.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.cache.SceneCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.query.RecordQueryParams;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.model.EvalPublishRecordDTO;
import com.iwhalecloud.bote.dto.model.FinetunePublishRecordDTO;
import com.iwhalecloud.bote.dto.model.ModelEvalDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.SimpleIntentFinetuneDTO;
import com.iwhalecloud.bote.dto.model.query.CorpusParams;
import com.iwhalecloud.bote.dto.model.query.ModelFinetuneQueryParams;
import com.iwhalecloud.bote.dto.model.request.CommonRequest;
import com.iwhalecloud.bote.dto.model.request.EvalRequest;
import com.iwhalecloud.bote.dto.model.request.FinetuneRequest;
import com.iwhalecloud.bote.dto.model.request.PredictRequest;
import com.iwhalecloud.bote.dto.model.response.PredictResponse.PredictInfo;
import com.iwhalecloud.bote.dto.model.response.UpdatePublishStatusResponse;
import com.iwhalecloud.bote.mapper.base.PublishManageMapper;
import com.iwhalecloud.bote.mapper.intent.IntentQuestionManageMapper;
import com.iwhalecloud.bote.mapper.model.ModelFinetuneManageMapper;
import com.iwhalecloud.bote.service.model.IModelFinetuneManageService;
import com.iwhalecloud.bote.service.model.helper.SmallModelAnswerHelper;
import com.iwhalecloud.bote.service.publish.IPublishService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.database.util.TransactionUtil;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 模型微调管理服务
 *
 * @author auto
 * @since 2025-02-19
 */
@Service
@RequiredArgsConstructor
public class ModelFinetuneManageServiceImpl implements IModelFinetuneManageService {
  // @formatter:off
  private final ModelFinetuneManageMapper mapper;
  private final PublishManageMapper publishMapper;
  private final IntentQuestionManageMapper intentQuestionMapper;
  private final IDocumentContentManageService documentContentManageService;
  private final IPublishService publishService;
  private final SmallModelAnswerHelper helper;
  private final SceneCache sceneCache;
  // @formatter:on

  @Override
  public ModelFinetuneDTO getFinetune(Long id) {
    ModelFinetuneDTO finetune = mapper.getFinetune(id);
    Assert.notNull(finetune, "模型微调不存在");
    return finetune;
  }

  @Override
  @Transactional
  public ResultVO<ModelFinetuneDTO> saveFinetune(ModelFinetuneDTO finetune) {
    // 校验名称唯一性
    if (mapper.existsFinetuneName(finetune)) {
      return BaseErrorConstant.CHECK_NAME.toResult(finetune.getModelName());
    }
    ModelFinetuneDTO old = finetune.getId() == null ? null : getFinetune(finetune.getId());
    if (old != null) {
      // 修改场景，限制修改字段
      finetune.setUseType(old.getUseType());
      finetune.setBaseModelType(old.getBaseModelType());
      finetune.setBaseModelSubType(old.getBaseModelSubType());
      finetune.setStatus(old.getStatus());
    }
    else {
      // 校验意图的合规性
      ResultVO<Void> result = validate(finetune);
      if (!result.isSuccess()) {
        return ResultVO.fail(result.getResultMsg());
      }
      finetune.setStatus(BaseConsts.FINETUNE_STATUS_WAIT);
    }
    // 初始化超参
    initHyperParameter(finetune);
    DataDifference<ModelFinetuneDTO> difference = DataDifferenceStarter.computeSave(old, finetune, false, finetune.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    if (old == null) {
      publish(null, finetune.getId(), finetune, BaseConsts.PUBLISH_TYPE_FINETUNE, finetune.getTenantId());
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteFinetune(Long id) {
    ModelFinetuneDTO finetune = getFinetune(id);
    // 删除部署的微调模型
    CommonRequest request = new CommonRequest();
    request.setTenantId(finetune.getTenantId().toString());
    request.setBaseModelName(finetune.getBaseModelSubType());
    request.setFineTuningModelId(finetune.getId().toString());
    request.setFileServer(FileStoreUtils.getDefaultStoreType());
    helper.delete(request);

    mapper.deleteFinetune(id, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> publishFinetune(Long tenantId, Long id, String status) {
    ModelFinetuneDTO finetune = getFinetune(id);
    CommonRequest request = new CommonRequest();
    request.setTenantId(finetune.getTenantId().toString());
    request.setBaseModelName(finetune.getBaseModelSubType());
    request.setFineTuningModelId(finetune.getId().toString());
    request.setFileServer(FileStoreUtils.getDefaultStoreType());

    Long userId = SessionUtil.getLoginInfo().getUserId();
    boolean online = BaseConsts.FINETUNE_STATUS_PUBLISH.equals(status);
    if (online) {
      // 部署微调模型
      helper.load(request);
      // 一个租户最多只有一个上架的意图用途模型
      if (BaseConsts.FINETUNE_USE_TYPE_INTENT.equals(finetune.getUseType())) {
        mapper.updateFinetuneStatusByTenantId(tenantId, userId);
      }
    }
    else {
      // 下线微调模型
      helper.unload(request);
    }
    mapper.updateFinetuneStatus(tenantId, id, userId, status);
    return ResultVO.success();
  }

  @Override
  public PageInfo<ModelFinetuneDTO> queryFinetunePage(ModelFinetuneQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    PageInfo<ModelFinetuneDTO> pageInfo = mapper.selectFinetunePage(queryParams, rowBounds).toPageInfo();
    for (ModelFinetuneDTO finetune : CollectionUtils.emptyIfNull(pageInfo.getList())) {
      if (BaseConsts.FINETUNE_USE_TYPE_CORPUS.equals(finetune.getUseType())) {
        finetune.setCorpus(JsonUtil.parseJson(finetune.getCorpusInfo(), new TypeReference<List<CorpusParams>>() {
        }));
      }
    }
    return pageInfo;
  }

  @Override
  public List<ModelFinetuneDTO> queryFinetuneList(ModelFinetuneQueryParams queryParams) {
    return mapper.selectFinetuneList(queryParams);
  }

  @Override
  public ResultVO<Void> updatePublishStatus(UpdatePublishStatusResponse response) {
    PublishRecordDTO record = publishMapper.getRecordByObjId(response.getResultObject().getPublishId());
    Assert.notNull(record, "查询不到有效的发布记录");
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(record.getId(), false, response));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("更新微调训练状态异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("更新微调训练状态异常，" + e.getMessage(), e);
    }
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> loadAllFinetun() {
    List<SimpleIntentFinetuneDTO> finetunes = mapper.selectOnlineFinetune();
    for (SimpleIntentFinetuneDTO finetune : CollectionUtils.emptyIfNull(finetunes)) {
      CommonRequest request = new CommonRequest();
      request.setTenantId(finetune.getTenantId().toString());
      request.setBaseModelName(finetune.getBaseModelName());
      request.setFineTuningModelId(finetune.getModelId().toString());
      request.setFileServer(FileStoreUtils.getDefaultStoreType());
      helper.load(request);
    }
    return ResultVO.success();
  }

  @Override
  public PageInfo<FinetunePublishRecordDTO> queryFinetuneRecordPage(RecordQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    //noinspection resource
    return publishMapper.selectFinetuneRecordPage(params, rowBounds).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<ModelEvalDTO> saveEval(ModelEvalDTO eval) {
    // 暂时只支持新增评测
    eval.setId(Sequences.MODEL_EVAL_ID.next());
    eval.setStatus(BaseConsts.EVAL_STATUS_RUNNING);
    eval.setStatusCd(BaseConsts.STATUS_CD_VALID);
    Long userId = SessionUtil.getLoginInfo().getUserId();
    eval.setCreatorId(userId);
    eval.setUpdatorId(userId);
    mapper.insertEval(eval);

    publish(null, eval.getId(), eval, BaseConsts.PUBLISH_TYPE_EVAL, eval.getTenantId());
    return ResultVO.success(eval);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteEval(Long id) {
    mapper.deleteEval(id, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public PageInfo<EvalPublishRecordDTO> queryEvalRecordPage(RecordQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    //noinspection resource
    return publishMapper.selectEvalRecordPage(params, rowBounds).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> cancel(Long publishId, String type) {
    PublishRecordDTO record = publishService.getRecord(publishId);
    if (!Objects.equals(BaseConsts.PUBLISH_STATUS_RUNNING, record.getPublishStatus())) {
      return ResultVO.fail("流程不在处理中，无需中断");
    }
    CommonRequest request = new CommonRequest();
    request.setFileServer(FileStoreUtils.getDefaultStoreType());
    if (BaseConsts.PUBLISH_TYPE_FINETUNE.equals(type)) {
      FinetuneRequest finetune = JsonUtil.parseJson(record.getSteps().get(0).getOutputJson(), FinetuneRequest.class);
      if (finetune == null) {
        return ResultVO.fail("查询不到流程相关的微调信息");
      }
      request.setPublishId(finetune.getPublishId());
      request.setTenantId(finetune.getTenantId());
      request.setBaseModelName(finetune.getBaseModelName());
    }
    else {
      EvalRequest eval = JsonUtil.parseJson(record.getSteps().get(0).getOutputJson(), EvalRequest.class);
      if (eval == null) {
        return ResultVO.fail("查询不到流程相关的评测信息");
      }
      request.setPublishId(eval.getPublishId());
      request.setTenantId(eval.getTenantId());
      request.setBaseModelName(eval.getBaseModelName());
      request.setFineTuningModelId(eval.getFineTuningModelId());
    }
    ResultVO<Void> result = helper.cancel(request, type);
    if (result.isSuccess()) {
      record.setPublishStatus(BaseConsts.PUBLISH_STATUS_FAILED);
      record.setFailReason("中断");
      record.setEndTime(new Date());
      record.setSpentTime((int) (record.getEndTime().getTime() - record.getStartTime().getTime()));
      publishService.updatePublishRecord(record);
    }
    return result;
  }

  @Override
  public ResultVO<Void> retry(Long tenantId, Long publishId, String type) {
    PublishRecordDTO record = publishService.getRecord(publishId);
    if (!Objects.equals(BaseConsts.PUBLISH_STATUS_FAILED, record.getPublishStatus())) {
      return ResultVO.fail("非失败状态流程，无需重试");
    }
    if (BaseConsts.PUBLISH_TYPE_FINETUNE.equals(type)) {
      ModelFinetuneDTO finetune = getFinetune(record.getObjId());
      publish(record.getId(), record.getObjId(), finetune, BaseConsts.PUBLISH_TYPE_FINETUNE, tenantId);
    }
    else {
      ModelEvalDTO eval = mapper.getEval(record.getObjId());
      publish(record.getId(), record.getObjId(), eval, BaseConsts.PUBLISH_TYPE_EVAL, tenantId);
    }
    return ResultVO.success();
  }

  @Override
  public ResultVO<PredictInfo> predict(ModelFinetuneDTO finetune) {
    ModelFinetuneDTO dto = getFinetune(finetune.getId());
    Assert.notNull(dto, "查询不到有效的微调模型");

    // 避免推理出现异常，先部署微调模型
    CommonRequest request = new CommonRequest();
    request.setTenantId(dto.getTenantId().toString());
    request.setBaseModelName(dto.getBaseModelSubType());
    request.setFineTuningModelId(dto.getId().toString());
    request.setFileServer(FileStoreUtils.getDefaultStoreType());
    helper.load(request);

    PredictRequest predictRequest = new PredictRequest();
    predictRequest.setTenantId(dto.getTenantId().toString());
    predictRequest.setBaseModelName(dto.getBaseModelSubType());
    predictRequest.setFineTuningModelId(dto.getId().toString());
    predictRequest.setText(finetune.getText());
    predictRequest.setThreshold(finetune.getThreshold() == null ? dto.getThreshold() : finetune.getThreshold());
    predictRequest.setMaxLength(finetune.getMaxLength() == null ? dto.getMaxLength() : finetune.getMaxLength());

    ResultVO<PredictInfo> result = helper.chat(predictRequest);
    if (result.isSuccess() && StringUtils.isNotEmpty(result.getResultObject().getLabel())) {
      if (BaseConsts.FINETUNE_USE_TYPE_INTENT.equals(dto.getUseType())) {
        Long sceneId = Long.valueOf(Arrays.asList(result.getResultObject().getLabel().split("-")).get(0));
        String sceneName = sceneCache.getSceneName(Long.parseLong(request.getTenantId()), sceneId);
        if (sceneName != null) {
          result.getResultObject().setLabel("命中智能体：" + sceneName);
        }
      }
    }
    return result;
  }

  private ResultVO<Long> publish(@Nullable Long id, Long objId, Object inst, String publishType, Long tenantId) {
    boolean exists = publishMapper.existsRunningRecord(tenantId, publishType);
    Assert.isTrue(!exists, "资源有限，当前租户存在运行中的发布流程，请稍后再试");
    PublishRecordDTO record;
    if (id == null) {
      // 创建发布
      List<PublishStepType> steps;
      if (BaseConsts.PUBLISH_TYPE_FINETUNE.equals(publishType)) {
        steps = Arrays.asList(PublishStepType.INITIALIZE_FINETUNE, PublishStepType.CALL_FINETUNE, PublishStepType.UPDATE_FINETUNE_STATUS);
      }
      else {
        steps = Arrays.asList(PublishStepType.CALL_EVAL, PublishStepType.UPDATE_EVAL_STATUS);
      }
      record = publishService.buildRecord(publishType, steps);
      record.setTenantId(tenantId);
      record.setObjId(objId);
      // 独立事务，保存到数据库
      TransactionUtil.executeNew(() -> {
        publishMapper.insertRecord(record);
        publishMapper.batchInsertStep(record.getSteps());
      });
    }
    else {
      // 重试场景，清空发布记录信息
      record = publishService.getRecord(id);
      TransactionUtil.executeNew(() -> {
        publishMapper.updateRecordForRetry(record.getId());
        publishMapper.updateRecordStepForRetry(record.getId());
      });
    }

    // 执行发布
    Future<?> future = ThreadPools.getCommon().submit(() -> publishService.start(record.getId(), true, inst));
    try {
      // 等待 3s, 以便发现部分错误。如果未完成，不再等待，但执行流程还在继续
      future.get(3, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      // ignore timeout
    }
    catch (ExecutionException e) {
      throw new BssException("步骤化微调评测异常，" + e.getMessage(), e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BssException("步骤化微调评测异常，" + e.getMessage(), e);
    }
    return ResultVO.success(record.getId());
  }

  private ResultVO<Void> validate(ModelFinetuneDTO finetune) {
    if (BaseConsts.FINETUNE_USE_TYPE_INTENT.equals(finetune.getUseType())) {
      return validateIntent(finetune.getTenantId());
    }
    else {
      return validateCorpus(finetune);
    }
  }

  private ResultVO<Void> validateIntent(Long tenantId) {
    List<IntentQuestionDTO> questions = intentQuestionMapper.selectQuestionsByTenantId(tenantId);
    if (CollectionUtils.isEmpty(questions)) {
      return ResultVO.fail("当前租户未配置意图语料（只处理上架智能体意图语料）");
    }
    Map<Long, List<IntentQuestionDTO>> group = questions.stream().collect(Collectors.groupingBy(IntentQuestionDTO::getSceneId));
    boolean legal = true;
    for (Entry<Long, List<IntentQuestionDTO>> entry : group.entrySet()) {
      if (entry.getValue().size() < 10) {
        legal = false;
        break;
      }
    }
    if (!legal) {
      return new ResultVO<>(BaseConsts.RESULT_CODE_WARNING, "只处理上架智能体意图语料，为了提高微调模型的质量，要求每个智能体的意图问句不少于 10 条");
    }
    if (group.size() == 1) {
      return new ResultVO<>(BaseConsts.RESULT_CODE_WARNING, "用于训练的智能体数量需要大于 1");
    }
    return ResultVO.success();
  }

  private ResultVO<Void> validateCorpus(ModelFinetuneDTO finetune) {
    if (CollectionUtils.isEmpty(finetune.getCorpus())) {
      return ResultVO.fail("语料参数不能为空");
    }
    finetune.setCorpusInfo(JsonUtil.toJsonString(finetune.getCorpus()));
    List<Map<String, String>> datas = documentContentManageService.collectCorpusQuestion(finetune.getCorpusInfo(), finetune.getTenantId());
    if (CollectionUtils.isEmpty(datas)) {
      return ResultVO.fail("语料问答数据不能为空");
    }
    Map<String, List<Map<String, String>>> group = datas.stream().collect(Collectors.groupingBy(p -> MapUtils.getString(p, "output")));
    boolean legal = true;
    for (Entry<String, List<Map<String, String>>> entry : group.entrySet()) {
      if (entry.getValue().size() < 10) {
        legal = false;
        break;
      }
    }
    if (!legal) {
      return new ResultVO<>(BaseConsts.RESULT_CODE_WARNING, "为了提高微调模型的质量，要求每个问题归类问句不少于 10 条，请检查语料回答列的内容。");
    }
    if (group.size() == 1) {
      return new ResultVO<>(BaseConsts.RESULT_CODE_WARNING, "用于训练的语料问题归类数量需要大于 1");
    }
    return ResultVO.success();
  }

  private void initHyperParameter(ModelFinetuneDTO finetune) {
    if (finetune.getNumTrainEpochs() == null) {
      finetune.setNumTrainEpochs(16);
    }
    if (finetune.getLearningRate() == null) {
      finetune.setLearningRate(new BigDecimal("0.00002"));
    }
    if (finetune.getBatchSize() == null) {
      finetune.setBatchSize(8);
    }
    if (finetune.getMaxLength() == null) {
      finetune.setMaxLength(128);
    }
    if (finetune.getWeightDecay() == null) {
      finetune.setWeightDecay(new BigDecimal("0.01"));
    }
  }
}
