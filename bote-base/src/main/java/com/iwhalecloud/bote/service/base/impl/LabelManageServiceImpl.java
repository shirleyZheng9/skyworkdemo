package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.LabelDTO;
import com.iwhalecloud.bote.dto.base.query.LabelQueryParams;
import com.iwhalecloud.bote.dto.base.LabelObjectRelDTO;
import com.iwhalecloud.bote.mapper.base.LabelManageMapper;
import com.iwhalecloud.bote.service.base.ILabelManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标签管理服务
 *
 * @author auto
 * @since 2024-09-13
 */
@Service
@RequiredArgsConstructor
public class LabelManageServiceImpl implements ILabelManageService {

  private final LabelManageMapper labelManageMapper;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<LabelDTO> saveLabel(LabelDTO label) {
    // 校验属性编码唯一性
    if (labelManageMapper.existsLabelName(label)) {
      return BaseErrorConstant.CHECK_LABEL_NAME.toResult(label.getLabelName());
    }
    label.setStatusCd(BaseConsts.STATUS_CD_VALID);
    LabelDTO old = label.getLabelId() == null ? null : getLabel(label.getTenantId(), label.getLabelId());
    DataDifference<LabelDTO> difference = DataDifferenceStarter.computeSave(old, label, false, label.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteLabel(Long tenantId, Long labelId) {
    LabelDTO label = getLabel(tenantId, labelId);
    if (label == null) {
      return BaseErrorConstant.NOT_EXIST.toResult();
    }
    if (resourceElementService.existsRelatedResource(tenantId, labelId, DataSyncCodeEnum.LABEL.getCode())) {
      return ResultVO.fail("标签已存在关联配置数据，不允许删除");
    }
    labelManageMapper.deleteLabel(labelId, SessionUtil.getLoginInfo().getUserId(), tenantId);
    return ResultVO.success();
  }

  @Override
  public List<LabelDTO> queryLabelList(LabelQueryParams params) {
    return labelManageMapper.selectLabelList(params);
  }

  @Override
  public List<LabelObjectRelDTO> queryLabelObjectRelList(List<Long> objectIds, String objectType, Long tenantId) {
    return labelManageMapper.selectLabelObjectRelList(objectIds, objectType, tenantId);
  }

  @Override
  @Transactional
  public ResultVO<Void> saveLabelObjectRel(List<LabelObjectRelDTO> labelObjectRels) {
    labelManageMapper.batchInsertLabelObjectRel(labelObjectRels);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteLabelObjectRel(List<Long> relIds, Long tenantId) {
    labelManageMapper.deleteLabelObjectRel(relIds, SessionUtil.getLoginInfo().getUserId(), tenantId);
    return ResultVO.success();
  }

  @Nullable
  private LabelDTO getLabel(Long tenantId, Long labelId) {
    return labelManageMapper.getLabel(labelId, tenantId);
  }
}
