package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.base.ApiAuthDTO;
import com.iwhalecloud.bote.mapper.base.ApiAuthManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：鉴权
 *
 * @author auto
 * @since 2024-09-19
 */
@Component
public final class ApiAuthDifferencePersistence extends BaseRootPersistence<ApiAuthDTO> {

  public ApiAuthDifferencePersistence(ApiAuthManageMapper apiAuthManageMapper) {
    setAddConsumer(apiAuthManageMapper::insertApiAuth);
    setModifyConsumer(apiAuthManageMapper::updateApiAuth);
  }

}
