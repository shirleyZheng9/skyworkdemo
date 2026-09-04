package com.iwhalecloud.bote.service.app.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.app.WebAppRecordDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppRecordQueryParams;
import com.iwhalecloud.bote.mapper.app.WebAppRecordMapper;
import com.iwhalecloud.bote.service.app.IWebAppRecordService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 工作台应用访问记录服务
 *
 * @author wang.tingyun
 * @since 2025-09-22
 */
@Service
@RequiredArgsConstructor
public class WebAppRecordServiceImpl implements IWebAppRecordService {

  private final WebAppRecordMapper appRecordMapper;

  @Override
  public PageInfo<WebAppRecordDTO> queryRecordPage(WebAppRecordQueryParams params) {
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    // noinspection resource
    return appRecordMapper.selectPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public List<WebAppRecordDTO> queryRecordList(WebAppRecordQueryParams params) {
    params.setUserId(SessionUtil.getLoginInfo().getUserId());
    return appRecordMapper.selectList(params);
  }

  @Override
  @Transactional
  public ResultVO<Void> addAppRecord(WebAppRecordDTO recordDTO) {
    Assert.notNull(recordDTO.getWebAppId(), "应用ID不能为空");

    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 检查记录是否已存在
    boolean exists = appRecordMapper.checkExists(recordDTO.getWebAppId(), userId);
    if (exists) {
      recordDTO.setUserId(userId);
      appRecordMapper.updateRecordTime(recordDTO);
      return ResultVO.success();
    }
    // 新增访问记录
    recordDTO.setRecordId(Sequences.WEB_APP_RECORD_ID.next());
    recordDTO.setUserId(userId);
    recordDTO.setCreatorId(userId);
    recordDTO.setUpdatorId(userId);
    recordDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    appRecordMapper.insertRecord(recordDTO);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> removeAppRecord(Long recordId) {
    appRecordMapper.deleteByRecordId(recordId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

}
