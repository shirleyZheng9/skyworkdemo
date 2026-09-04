package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.base.DcCfgDTO;
import com.iwhalecloud.bote.mapper.base.DcCfgMapper;
import com.iwhalecloud.bote.service.base.IDcCfgQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 在线配置查询服务
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
@Service
@RequiredArgsConstructor
public class DcCfgQueryServiceImpl implements IDcCfgQueryService {

  private final DcCfgMapper dcCfgMapper;

  @Override
  public List<DcCfgDTO> querySwitchList() {
    return dcCfgMapper.selectAllSwitches(CommonConsts.DC_CFG_TYPE_1);
  }
}
