package com.iwhalecloud.bote.service.security;

import com.iwhalecloud.bassc.basiccenter.service.ILitchiSysParamService;
import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LitchiSysParamServiceImpl implements ILitchiSysParamService {
  private final DcParamCache paramCache;

  @Override
  public String getSysParamValue(String paramCode) {
    // 博特暂未在数据库中配置任何错误信息，直接返回，避免频繁查询数据库
    // com.iwhalecloud.bassc.basiccenter.base.exception.ext.BasicErrorConstant#newCustomErrorConstant
    if (paramCode.endsWith("_ERROR_MSG")) {
      return "";
    }
    String paramValue = paramCache.getDcParamValByCode(paramCode, "");
    // auth-litchi 开关配置只支持 true/false
    if (BaseConsts.TRUE.equals(paramValue)) {
      return "true";
    }
    else if (BaseConsts.FALSE.equals(paramValue)) {
      return "false";
    }
    return paramValue;
  }

}
