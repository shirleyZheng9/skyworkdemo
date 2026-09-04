package com.iwhalecloud.bote.controller.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.EnvVariableDTO;
import com.iwhalecloud.bote.dto.base.query.EnvVariableQueryParams;
import com.iwhalecloud.bote.service.base.IEnvVariableManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 环境变量管理 controller
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/envVariable", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "环境变量管理")
public class EnvVariableManageController {
  private final IEnvVariableManageService envVariableManageService;

  @PostMapping("saveEnvVariable")
  @Operation(summary = "保存环境变量")
  public ResultVO<EnvVariableDTO> saveEnvVariable(@RequestBody EnvVariableDTO variable) {
    return envVariableManageService.saveEnvVariable(variable);
  }

  @PostMapping("queryEnvVariableList")
  @Operation(summary = "查询环境变量列表")
  public ResultVO<List<EnvVariableDTO>> queryEnvVariableList(@RequestBody EnvVariableQueryParams param) {
    return ResultVO.success(envVariableManageService.queryEnvVariableList(param));
  }

  @PostMapping("queryEnvVariablePage")
  @Operation(summary = "查询环境变量列表(分页)")
  public ResultVO<PageInfo<EnvVariableDTO>> queryEnvVariablePage(@RequestBody EnvVariableQueryParams param) {
    return ResultVO.success(envVariableManageService.queryEnvVariablePage(param));
  }

  @GetMapping("getEnvVariableParamList")
  @Operation(summary = "查询环境变量参数列表")
  public ResultVO<List<EnvVariableDTO>> getEnvVariableParamList(@RequestParam("tenantId") Long tenantId) {
    return ResultVO.success(envVariableManageService.getEnvVariableParamList(tenantId));
  }

  @GetMapping("findEnvVariable")
  @Operation(summary = "根据ID查询环境变量")
  public ResultVO<EnvVariableDTO> findEnvVariable(@RequestParam(name = "variableId") Long variableId, @RequestParam(name = "tenantId") Long tenantId) {
    return ResultVO.success(envVariableManageService.findEnvVariable(variableId, tenantId));
  }

  @GetMapping("deleteEnvVariableById")
  @Operation(summary = "根据ID删除环境变量")
  public ResultVO<Void> deleteEnvVariableById(@RequestParam(name = "variableId") Long variableId, @RequestParam(name = "tenantId") Long tenantId) {
    return envVariableManageService.deleteEnvVariableById(variableId, tenantId);
  }
}
