package com.iwhalecloud.bote.controller.intent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.intent.IntentLogDTO;
import com.iwhalecloud.bote.dto.intent.query.IntentQueryParams;
import com.iwhalecloud.bote.intent.IIntentLogManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 意图识别日志管理 controller
 *
 * @author auto
 * @since 2024-12-18
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/intentLog", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "意图：意图识别日志管理")
public class IntentLogManageController {

  private final IIntentLogManageService intentLogManageService;

  @Operation(summary = "分页查询意图识别日志")
  @PostMapping("queryIntentLogPage")
  public ResultVO<PageInfo<IntentLogDTO>> queryIntentLogPage(@RequestBody IntentQueryParams queryParams) {
    return ResultVO.success(intentLogManageService.queryIntentLogPage(queryParams));
  }

  @Operation(summary = "标记意图识别日志")
  @PostMapping("markIntentLog")
  public ResultVO<Void> markIntentLog(@RequestBody IntentLogDTO log) {
    return intentLogManageService.markIntentLog(log);
  }

  @Operation(summary = "取消意图识别日志标记")
  @GetMapping("cancelIntentLog")
  public ResultVO<Void> cancelIntentLog(@RequestParam(value = "tenantId", required = false) Long tenantId, @RequestParam("logId") Long logId) {
    return intentLogManageService.cancelIntentLog(tenantId, logId);
  }
}
