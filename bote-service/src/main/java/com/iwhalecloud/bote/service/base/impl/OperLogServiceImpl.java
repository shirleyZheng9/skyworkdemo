package com.iwhalecloud.bote.service.base.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.dto.base.OperLogDTO;
import com.iwhalecloud.bote.dto.base.OperLogDetailDTO;
import com.iwhalecloud.bote.dto.base.query.OperLogQueryParams;
import com.iwhalecloud.bote.mapper.base.OperLogQueryMapper;
import com.iwhalecloud.bote.service.base.IOperLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 *
 * @author auto
 * @since 2024-10-26
 */
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl implements IOperLogService {

  private final OperLogQueryMapper operLogQueryMapper;

  @Override
  public PageInfo<OperLogDTO> queryOperLogPage(OperLogQueryParams params) {
    RowBounds rowBounds = params.buildRowBounds();
    if (OperClassEnum.SCENE.name().equals(params.getOperClass())) {
      params.setOperType("M");
    }
    //noinspection resource
    return operLogQueryMapper.selectOperLogPage(params, rowBounds).toPageInfo();
  }

  @Override
  public List<OperLogDetailDTO> queryOperLogDetail(Long logId) {
    return operLogQueryMapper.selectOperLogDetail(logId);
  }
}
