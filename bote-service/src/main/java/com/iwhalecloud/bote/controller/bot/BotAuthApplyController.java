package com.iwhalecloud.bote.controller.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.bot.BotAuthApplyDTO;
import com.iwhalecloud.bote.dto.bot.query.BotAuthApplyParams;
import com.iwhalecloud.bote.service.bot.IBotAuthApplyService;
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
 * 智能应用授权申请 Controller
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/botAuthApply", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "应用：授权申请")
public class BotAuthApplyController {

  private final IBotAuthApplyService botAuthApplyService;

  @Operation(summary = "申请应用发布到广场")
  @PostMapping("applyBotAuth")
  public ResultVO<Boolean> applyBotPublish(@RequestBody BotAuthApplyDTO applyDTO) {
    return ResultVO.success(botAuthApplyService.applyBotAuth(applyDTO));
  }

  @Operation(summary = "分页查询应用发布申请列表")
  @PostMapping("queryBotApplyPage")
  public ResultVO<PageInfo<BotAuthApplyDTO>> queryBotApplyPage(@RequestBody BotAuthApplyParams params) {
    return ResultVO.success(botAuthApplyService.getBotApplyPage(params));
  }

  @Operation(summary = "分页查询应用发布审核列表")
  @PostMapping("queryBotAuditPage")
  public ResultVO<PageInfo<BotAuthApplyDTO>> queryBotAuditPage(@RequestBody BotAuthApplyParams params) {
    return ResultVO.success(botAuthApplyService.getBotAuditPage(params));
  }

  @Operation(summary = "查询应用发布详情")
  @GetMapping("queryBotAuthDetail")
  public ResultVO<BotAuthApplyDTO> queryBotAuthDetail(@RequestParam Long applyId) {
    return ResultVO.success(botAuthApplyService.getBotAuthApplyDetail(applyId));
  }

  @Operation(summary = "审核应用发布申请")
  @PostMapping("auditBotAuthApply")
  public ResultVO<Void> auditBotAuthApply(@RequestBody BotAuthApplyParams params) {
    botAuthApplyService.auditBotAuthApply(params);
    return ResultVO.success();
  }

  @Operation(summary = "查询应用发布申请详情")
  @GetMapping("getBotAuthApplyDetail")
  public ResultVO<BotAuthApplyDTO> queryBotAuthApplyDetail(@RequestParam("applyId") Long applyId) {
    return ResultVO.success(botAuthApplyService.getBotAuthApply(applyId));
  }

}
