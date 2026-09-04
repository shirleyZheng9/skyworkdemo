package com.iwhalecloud.bote.controller.plugin;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.plugin.PluginExecuteParams;
import com.iwhalecloud.bote.service.plugin.IPluginEngine;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 插件执行 controller
 *
 * @author qian.sisheng
 * @since 2025-04-09
 */

@RequestMapping(path = BaseConsts.API_PREFIX + "plugin", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
public class PluginController {

  private final IPluginEngine pluginEngine;

  @Operation(summary = "执行插件")
  @PostMapping("execute")
  public ResultVO<Object> execute(@RequestBody PluginExecuteParams params) {
    return ResultVO.success(pluginEngine.execute(params));
  }
}
