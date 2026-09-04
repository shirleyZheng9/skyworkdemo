package com.iwhalecloud.bote.service.publish.step.intent;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.intent.IntentQuestionDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.request.FinetuneRequest;
import com.iwhalecloud.bote.intent.IIntentQuestionManageService;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.intent.IntentQuestionManageMapper;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * 步骤执行器：初始化微调训练
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
public class InitializeFinetuneStep extends AbstractPublishStep<ModelFinetuneDTO> {

  private static final FileInfoManageMapper fileInfoManageMapper = SpringUtil.getBean(FileInfoManageMapper.class);

  private static final IntentQuestionManageMapper mapper = SpringUtil.getBean(IntentQuestionManageMapper.class);

  private static final IIntentQuestionManageService intentQuestionManageService = SpringUtil.getBean(IIntentQuestionManageService.class);

  private static final IDocumentContentManageService documentContentManageService = SpringUtil.getBean(IDocumentContentManageService.class);

  public InitializeFinetuneStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public ModelFinetuneDTO convertInputParams(Object params) {
    if (params instanceof ModelFinetuneDTO) {
      return (ModelFinetuneDTO) params;
    }
    return null;
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, ModelFinetuneDTO finetune) {
    FinetuneRequest request = new FinetuneRequest();
    File file;
    if (BaseConsts.FINETUNE_USE_TYPE_INTENT.equals(finetune.getUseType())) {
      List<IntentQuestionDTO> questions = mapper.selectQuestionsByTenantId(finetune.getTenantId());
      if (CollectionUtils.isEmpty(questions)) {
        return ResultVO.fail("当前租户未配置意图语料");
      }
      file = intentQuestionManageService.createIntentQuestionFile(finetune.getTenantId());
      computeThreshold(questions, request);
    }
    else {
      Pair<File, BigDecimal> pair = documentContentManageService.createCorpusQuestionFile(finetune.getCorpusInfo(), finetune.getTenantId());
      file = pair.getLeft();
      request.setThreshold(pair.getRight());
    }

    request.setPublishId(record.getObjId().toString());
    request.setTenantId(record.getTenantId().toString());
    request.setBaseModelName(finetune.getBaseModelSubType());
    // 附件
    UploadConfigVO config = new UploadConfigVO();
    config.setSubFolder(BaseConsts.FINETUNE_FILE_PATH + finetune.getTenantId());
    config.setOriginalFileName(file.getName());
    FileInfoVO fileInfo = fileService.uploadFile(file, config);
    // 构造关联文件信息
    FileInfoDTO dto = new FileInfoDTO();
    dto.setFileInfoId(Sequences.FILE_INFO_ID.next());
    dto.setFileId(fileInfo.getFileId());
    dto.setFileName(fileInfo.getFileName());
    dto.setTenantId(record.getTenantId());
    dto.setBusiType(BaseConsts.FILE_BUSI_TYPE_FINETUNE);
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatedTime(new Date());
    dto.setCatalogItemId(CatalogConsts.DEFAULT_CATALOG_ITEM_PARENT_ID);
    dto.setCreatorId(finetune.getCreatorId());
    dto.setUpdatorId(finetune.getCreatorId());
    fileInfoManageMapper.insertFileInfo(dto);

    request.setFileId(dto.getFileInfoId());
    request.setFilePath(fileInfo.getFilePathInServer());
    request.setFileServer(fileInfo.getStoreType());
    // 超参
    request.setNumTrainEpochs(finetune.getNumTrainEpochs());
    request.setLearningRate(finetune.getLearningRate());
    request.setBatchSize(finetune.getBatchSize());
    request.setMaxLength(finetune.getMaxLength());
    request.setWeightDecay(finetune.getWeightDecay());

    step.setOutputJson(JsonUtil.toJsonString(request));
    return ResultVO.success();
  }

  /**
   * 阈值指数，关系到微调模型推理的准确率，需要基于场景问题数计算出合理值
   * <p>规则：1.25 除以 output 去重数（维度：场景 + 扩展）</p>
   */
  private void computeThreshold(List<IntentQuestionDTO> questions, FinetuneRequest request) {
    int labelNum = questions.stream().map(p -> p.getSceneId() + "-" + p.getAttribute()).collect(Collectors.toSet()).size();
    BigDecimal result = BigDecimal.valueOf(1.25).divide(BigDecimal.valueOf(labelNum), 2, RoundingMode.HALF_UP);
    request.setThreshold(result);
  }
}
