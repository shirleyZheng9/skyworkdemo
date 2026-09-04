package com.iwhalecloud.bote.service.portal.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.portal.PrivDTO;
import com.iwhalecloud.bote.dto.portal.query.PrivQueryParams;
import com.iwhalecloud.bote.mapper.portal.PrivManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.portal.IPrivManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限定义管理服务实现
 *
 * @author auto
 * @since 2024-10-14
 */
@Service
@RequiredArgsConstructor
public class PrivManageServiceImpl implements IPrivManageService {

  private final PrivManageMapper privManageMapper;
  private final ICatalogManageService catalogManageService;

  @Override
  public PrivDTO getPriv(Long privId) {
    return privManageMapper.getPriv(privId);
  }

  @Override
  @Transactional
  public ResultVO<PrivDTO> savePriv(PrivDTO priv) {
    PrivDTO old = priv.getPrivId() == null ? null : getPriv(priv.getPrivId());
    DataDifference<PrivDTO> difference = DataDifferenceStarter.computeSave(old, priv, false, null);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Transactional
  public ResultVO<Void> deletePriv(Long privId) {
    privManageMapper.deletePriv(privId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public List<PrivDTO> queryPrivList(PrivQueryParams params) {
    String catalogType = BaseConsts.PRIV_TYPE_MENU.equals(params.getPrivType()) ? CatalogConsts.TYPE_PRIV_MENU : CatalogConsts.TYPE_PRIV_COMPONENT;
    List<Long> catalogItemList = catalogManageService.queryChildrenCatalogIds(BaseConsts.PLATFORM_TENANT_ID, params.getCatalogItemId(), catalogType);
    params.setCatalogItemList(catalogItemList);
    return privManageMapper.selectPrivList(params);
  }

  @Override
  public PageInfo<PrivDTO> queryPrivPage(PrivQueryParams params) {
    String catalogType = BaseConsts.PRIV_TYPE_MENU.equals(params.getPrivType()) ? CatalogConsts.TYPE_PRIV_MENU : CatalogConsts.TYPE_PRIV_COMPONENT;
    List<Long> catalogItemList = catalogManageService.queryChildrenCatalogIds(BaseConsts.PLATFORM_TENANT_ID, params.getCatalogItemId(), catalogType);
    params.setCatalogItemList(catalogItemList);
    RowBounds rowBounds = params.buildRowBounds();
    //noinspection resource
    return privManageMapper.selectPrivPage(params, rowBounds).toPageInfo();
  }
}
