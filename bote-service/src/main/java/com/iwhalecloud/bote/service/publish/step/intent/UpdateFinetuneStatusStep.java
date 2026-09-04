package com.iwhalecloud.bote.service.publish.step.intent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.enums.PublishStepType;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.PublishRecordDTO;
import com.iwhalecloud.bote.dto.base.PublishStepDTO;
import com.iwhalecloud.bote.dto.model.ModelFinetuneDTO;
import com.iwhalecloud.bote.dto.model.request.FinetuneRequest;
import com.iwhalecloud.bote.dto.model.response.UpdatePublishStatusResponse;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.model.ModelFinetuneManageMapper;
import com.iwhalecloud.bote.service.publish.step.AbstractPublishStep;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.util.Date;

/**
 * 步骤执行器：更新微调训练信息
 *
 * @author chen.linfa
 * @since 2025-02-18
 */
public class UpdateFinetuneStatusStep extends AbstractPublishStep<UpdatePublishStatusResponse> {

  private static final ModelFinetuneManageMapper finetuneMapper = SpringUtil.getBean(ModelFinetuneManageMapper.class);
  private static final BoteFileInfoMapper fileInfoMapper = SpringUtil.getBean(BoteFileInfoMapper.class);
  private static final FileInfoManageMapper fileInfoManageMapper = SpringUtil.getBean(FileInfoManageMapper.class);

  public UpdateFinetuneStatusStep(PublishRecordDTO record, PublishStepDTO step) {
    super(record, step);
  }

  @Override
  public UpdatePublishStatusResponse convertInputParams(Object params) {
    if (params == null) {
      return null;
    }
    return JsonUtil.parseJson(JsonUtil.toJsonString(params), UpdatePublishStatusResponse.class);
  }

  @Override
  protected ResultVO<String> doExecute(boolean auto, UpdatePublishStatusResponse response) {
    if (auto) {
      // 手工环节，跳过
      return ResultVO.success("break");
    }
    if (!response.isSuccess()) {
      return ResultVO.fail(response.getResultMsg());
    }
    FinetuneRequest request = getOutputParams(PublishStepType.INITIALIZE_FINETUNE, new TypeReference<FinetuneRequest>() {
    });
    // 记录文件信息
    Long responseFileId = IDUtils.nextId();
    FileInfoVO fileInfo = new FileInfoVO();
    fileInfo.setFileId(responseFileId);
    fileInfo.setStoreType(response.getResultObject().getFileServer());
    fileInfo.setFilePathInServer(response.getResultObject().getFilePath());
    fileInfo.setFileName(responseFileId + ".tar.gz");
    fileInfo.setStatusCd(BaseConsts.STATUS_CD_VALID);
    fileInfoMapper.insertFileInfo(fileInfo);
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
    dto.setCreatorId(1L);
    dto.setUpdatorId(1L);
    fileInfoManageMapper.insertFileInfo(dto);

    // 记录训练产物
    ModelFinetuneDTO finetune = finetuneMapper.getFinetune(record.getObjId());
    finetune.setStatus(BaseConsts.FINETUNE_STATUS_UNPUBLISH);
    finetune.setRequestFileId(request.getFileId());
    finetune.setResponseFileId(dto.getFileInfoId());
    finetune.setThreshold(request.getThreshold());
    finetuneMapper.updateFinetune(finetune);

    step.setOutputJson(JsonUtil.toJsonString(response.getResultObject()));
    return ResultVO.success("finished");
  }
}
