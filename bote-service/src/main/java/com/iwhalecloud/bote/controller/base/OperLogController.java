package com.iwhalecloud.bote.controller.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.OperLogDTO;
import com.iwhalecloud.bote.dto.base.OperLogDetailDTO;
import com.iwhalecloud.bote.dto.base.query.OperLogQueryParams;
import com.iwhalecloud.bote.service.base.IOperLogService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志 controller
 *
 * @author auto
 * @since 2024-10-26
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/operLog", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：操作日志")
public class OperLogController {

  private final IOperLogService operLogService;

  @Operation(summary = "查询操作日志列表（分页）")
  @PostMapping("queryOperLogPage")
  public ResultVO<PageInfo<OperLogDTO>> queryOperLogPage(@RequestBody OperLogQueryParams params) {
    return ResultVO.success(operLogService.queryOperLogPage(params));
  }

  @Operation(summary = "根据操作日志 ID 查询日志详情列表")
  @GetMapping("queryOperLogDetail")
  public ResultVO<List<OperLogDetailDTO>> queryOperLogDetail(@Parameter(description = "日志 ID", required = true) @RequestParam("logId") Long logId) {
    Assert.notNull(logId, "日志 ID 不能为空");
    return ResultVO.success(operLogService.queryOperLogDetail(logId));
  }
}
