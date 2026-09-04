package com.iwhalecloud.bote.service.app.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppQueryParams;
import com.iwhalecloud.bote.mapper.app.WebAppMapper;
import com.iwhalecloud.bote.service.app.IWebAppService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 网页应用服务实现
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Service
@RequiredArgsConstructor
public class WebAppServiceImpl implements IWebAppService {

  private final WebAppMapper webAppMapper;

  @Override
  @Transactional
  public ResultVO<WebAppDTO> saveWebApp(WebAppDTO webAppDTO) {
    Assert.notNull(webAppDTO.getSpaceId(), "企业空间 ID 不能为空");
    // 补充数据
    Long userId = SessionUtil.getLoginInfo().getUserId();
    webAppDTO.setUpdatorId(userId);
    if (webAppDTO.getWebAppId() == null) {
      webAppDTO.setCreatorId(userId);
      webAppDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }

    // 处理更新操作
    WebAppDTO oldApp = null;
    if (webAppDTO.getWebAppId() != null) {
      oldApp = getWebApp(webAppDTO.getWebAppId(), webAppDTO.getSpaceId());
      Assert.notNull(oldApp, "应用不存在，webAppId=" + webAppDTO.getWebAppId());
      // 校验是否被已启用的工作台应用关联
      if (webAppMapper.checkUsedForBenchApp(webAppDTO.getWebAppId())) {
        throw new BssException("应用已被其他上架的应用关联，请停用关联的应用再修改。");
      }
    }

    // 保存应用数据
    DataDifference<WebAppDTO> difference = DataDifferenceStarter.computeSaveAndLog(oldApp, webAppDTO, false,
      webAppDTO.getSpaceId(), OperClassEnum.WEB_APP);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }

    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public PageInfo<WebAppDTO> queryWebAppPage(WebAppQueryParams queryParams) {
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return webAppMapper.selectWebAppPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public List<WebAppDTO> queryWebAppList(Long spaceId) {
    return webAppMapper.selectWebAppList(spaceId);
  }

  @Override
  public WebAppDTO getWebApp(Long webAppId, Long spaceId) {
    return webAppMapper.selectWebApp(webAppId, spaceId);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteWebApp(Long webAppId, Long spaceId) {
    // 校验是否被已启用的工作台应用关联
    if (webAppMapper.checkUsedForBenchApp(webAppId)) {
      throw new BssException("应用已被其他上架的应用关联，请停用关联的应用再删除");
    }
    webAppMapper.deleteWebApp(webAppId, spaceId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  @Override
  public String getWebAppIcon(Long webAppId, Long spaceId) {
    String appIcon = webAppMapper.selectWebAppIcon(spaceId, webAppId);
    return appIcon == null ? "" : appIcon;
  }

}
