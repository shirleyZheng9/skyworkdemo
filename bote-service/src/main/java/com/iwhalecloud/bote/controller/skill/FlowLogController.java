package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.skill.FlowRunLogVO;
import com.iwhalecloud.bote.dto.skill.query.FlowRunLogQueryParams;
import com.iwhalecloud.bote.service.skill.IFlowRunLogService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 流程执行日志控制器
 *
 * @author bianjp
 * @since 2025-03-05
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "flowLog", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：流程运行日志")
public class FlowLogController {
  private final IFlowRunLogService flowTraceLogService;

  @PostMapping("qryFlowLogPage")
  @Operation(summary = "分页查询流程执行日志")
  public ResultVO<PageInfo<FlowRunLogVO>> qryFlowLogPage(@RequestBody FlowRunLogQueryParams queryParams) {
    Assert.notNull(queryParams.getTenantId(), "租户 ID 不能为空");
    return ResultVO.success(flowTraceLogService.qryFlowLogPage(queryParams));
  }

  @GetMapping("getFlowLog")
  @Operation(summary = "查询流程执行日志详情")
  public ResultVO<FlowRunLogVO> getFlowLog(@RequestParam(name = "tenantId", required = false) Long tenantId, @RequestParam("logId") Long logId) {
    return ResultVO.success(flowTraceLogService.getFlowLog(tenantId, logId));
  }

}
