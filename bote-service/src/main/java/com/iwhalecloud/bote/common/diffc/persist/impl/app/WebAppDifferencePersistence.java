package com.iwhalecloud.bote.common.diffc.persist.impl.app;

import com.iwhalecloud.bote.dto.app.WebAppDTO;
import com.iwhalecloud.bote.mapper.app.WebAppMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异服务：网页应用
 *
 * @author tingyun.wang
 * @since 2025-09-05
 */
@Component
public final class WebAppDifferencePersistence extends BaseRootPersistence<WebAppDTO> {

  public WebAppDifferencePersistence(WebAppMapper webAppMapper) {
    // 新增情况
    this.setAddConsumer(webAppMapper::insertWebApp);
    // 修改情况
    this.setModifyConsumer(webAppMapper::updateWebApp);
  }

}
