package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.service.base.IEnvInfoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环境信息控制器
 *
 * @author tingyun.wang
 * @since 2025-03-23
 */
@RestController
@IgnoreSession
@IgnoreSign
@RequiredArgsConstructor
@RequestMapping(path = BaseConsts.API_PREFIX)
public class EnvInfoController {

  private final IEnvInfoService envInfoService;

  @GetMapping(path = "env/info.js", produces = "application/javascript")
  public String getEnvInfo(HttpServletResponse response) {
    // 补充必要的响应头，避免接口在浏览器中取到本地缓存
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store");
    return envInfoService.getEnvInfo();
  }

}
