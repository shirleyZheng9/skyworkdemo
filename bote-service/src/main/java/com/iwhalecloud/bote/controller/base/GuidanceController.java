package com.iwhalecloud.bote.controller.base;

import com.iwhalecloud.bote.common.annotation.RequestCacheable;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.GuidanceCfgDTO;
import com.iwhalecloud.bote.service.base.IGuidanceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 平台使用引导 Controller
 *
 * @author wangtingyun
 * @since 2025-11-11
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "guidance", name = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@RestController
@Tag(name = "基础：使用指引")
public class GuidanceController {

  private final IGuidanceManageService guidanceManageService;

  @Operation(summary = "获取步骤指引信息")
  @GetMapping("getStepGuidance")
  public ResultVO<List<GuidanceCfgDTO>> getStepGuidance(@RequestParam(name = "roleCode") String roleCode) {
    return ResultVO.success(guidanceManageService.queryStepGuidance(roleCode));
  }

  @GetMapping("getBeginnerGuidance")
  @Operation(summary = "获取新手入门指引")
  @RequestCacheable(sql = "SELECT updated_time FROM bt_guidance_cfg WHERE id = 1", cacheOnNotFound = true)
  public ResultVO<List<GuidanceCfgDTO>> getBeginnerGuidance() {
    return ResultVO.success(guidanceManageService.getBeginnerGuidance());
  }

  @GetMapping("existsBeginnerGuidance")
  @Operation(summary = "查询是否存在新手入门指引")
  public ResultVO<Boolean> existsBeginnerGuidance() {
    return ResultVO.success(guidanceManageService.existsBeginnerGuidance());
  }

}
