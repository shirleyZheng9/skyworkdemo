package com.iwhalecloud.bote.common.diffc;

import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bss.litchi.diffc.ICustomizedService;
import com.iwhalecloud.bote.common.util.SessionUtil;
import org.springframework.stereotype.Component;

/**
 * 实现差异计算定制化服务
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public class CustomizedService implements ICustomizedService {
  @Override
  public Long getUserId() {
    return SessionUtil.getOptionalUserId(-1L);
  }

  @Override
  public String getValidStatusCd() {
    return CommonConsts.STATUS_CD_VALID;
  }

  @Override
  public String getInvalidStatusCd() {
    return CommonConsts.STATUS_CD_INVALID;
  }
}
