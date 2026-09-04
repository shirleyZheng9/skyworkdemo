package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.PythonUtil;
import com.iwhalecloud.bote.dto.base.PythonEnvInfo;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Python 相关接口
 *
 * @author wangtingyun
 * @since 2026-03-04
 */
@RestController
@RequestMapping(value = BaseConsts.API_PREFIX + "manager/python/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Python")
public class PythonController {

  @GetMapping("checkPythonEnv")
  @Operation(description = "检查 Python 环境信息")
  public ResultVO<PythonEnvInfo> checkPythonEnv() {
    return ResultVO.success(PythonUtil.checkPythonEnv());
  }

}
