package com.iwhalecloud.bote.controller.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.dto.bot.CopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.SimpleCopilotPointDTO;
import com.iwhalecloud.bote.dto.bot.query.CopilotPointParams;
import com.iwhalecloud.bote.dto.bot.query.PointQueryParams;
import com.iwhalecloud.bote.service.bot.ICopilotPointManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 副驾指令管理 controller
 *
 * @author chen.linfa
 * @since 2025-01-20
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/copilotPoint", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "机器人：副驾指令管理")
public class CopilotPointManageController {

  private final ICopilotPointManageService service;

  @Operation(summary = "查询单个鉴权指令")
  @GetMapping("getPoint")
  public ResultVO<CopilotPointDTO> getPoint(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "pointId") Long pointId) {
    Assert.notNull(pointId, "主键 ID 不能为空");
    return ResultVO.success(service.getPoint(tenantId, pointId));
  }

  @Operation(summary = "保存鉴权指令")
  @PostMapping("savePoint")
  public ResultVO<CopilotPointDTO> savePoint(@RequestBody @Valid CopilotPointDTO point) {
    return service.savePoint(point);
  }

  @Operation(summary = "删除鉴权指令")
  @GetMapping("deletePoint")
  public ResultVO<Void> deletePoint(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam(name = "pointId") Long pointId) {
    Assert.notNull(pointId, "主键 ID 不能为空");
    return service.deletePoint(tenantId, pointId);
  }

  @Operation(summary = "分页查询鉴权指令")
  @PostMapping("queryPointPage")
  public ResultVO<PageInfo<CopilotPointDTO>> queryPointPage(@RequestBody PointQueryParams queryParams) {
    return ResultVO.success(service.queryPointPage(queryParams));
  }

  @Operation(summary = "查询鉴权指令列表")
  @PostMapping("queryPointList")
  public ResultVO<List<CopilotPointDTO>> queryPointList(@RequestBody PointQueryParams queryParams) {
    return ResultVO.success(service.queryPointList(queryParams));
  }

  @Operation(summary = "副驾模式，根据指令编码，进行 API 鉴权，并返回组装后的指令")
  @PostMapping("auth")
  public ResultVO<SimpleCopilotPointDTO> auth(@RequestBody CopilotPointParams params) {
    if (MapUtils.isEmpty(params.getParams())) {
      params.setParams(new HashMap<>(2));
    }
    if (params.getTenantId() == null) {
      params.setTenantId(TenantIdUtil.getTenantId());
    }
    return service.auth(params);
  }
}
