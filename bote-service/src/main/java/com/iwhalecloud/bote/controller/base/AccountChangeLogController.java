package com.iwhalecloud.bote.controller.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.base.AccountEventLog;
import com.iwhalecloud.bote.dto.base.query.AccountEventLogQueryParams;
import com.iwhalecloud.bote.service.base.IAccountEventLogService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号事件日志 controller
 *
 * @author tingyun.wang
 * @since 2025-07-15
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/accountChangeLog", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "基础：账号事件日志")
public class AccountChangeLogController {

  private final IAccountEventLogService accountChangeLogService;

  @Operation(summary = "查询账号事件日志列表（分页）")
  @PostMapping("queryPage")
  public ResultVO<PageInfo<AccountEventLog>> queryAccountChangeLogPage(@RequestBody AccountEventLogQueryParams params) {
    return ResultVO.success(accountChangeLogService.queryLogPage(params));
  }

}
