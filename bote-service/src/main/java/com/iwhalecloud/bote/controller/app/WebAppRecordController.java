package com.iwhalecloud.bote.controller.app;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.dto.app.WebAppRecordDTO;
import com.iwhalecloud.bote.dto.app.query.WebAppRecordQueryParams;
import com.iwhalecloud.bote.service.app.IWebAppRecordService;
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

import java.util.List;

/**
 * 网页应用访问记录 Controller
 *
 * @author wang.tingyun
 * @since 2025-09-23
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/webAppRecord", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "运行态：网页应用访问记录")
public class WebAppRecordController {

  private final IWebAppRecordService recordService;

  @PostMapping("queryRecordPage")
  @Operation(summary = "分页查询应用访问记录")
  public ResultVO<PageInfo<WebAppRecordDTO>> queryAppRecordPage(@RequestBody WebAppRecordQueryParams params) {
    return ResultVO.success(recordService.queryRecordPage(params));
  }

  @PostMapping("queryRecordList")
  @Operation(summary = "查询应用访问记录列表")
  public ResultVO<List<WebAppRecordDTO>> queryAppRecordList(@RequestBody WebAppRecordQueryParams params) {
    return ResultVO.success(recordService.queryRecordList(params));
  }

  @PostMapping("addRecord")
  @Operation(summary = "添加应用访问记录")
  public ResultVO<Void> addAppRecord(@RequestBody WebAppRecordDTO appRecordDTO) {
    return recordService.addAppRecord(appRecordDTO);
  }

  @GetMapping("removeRecord")
  @Operation(summary = "移除应用访问记录")
  public ResultVO<Void> removeAppRecord(@RequestParam("recordId") Long recordId) {
    return recordService.removeAppRecord(recordId);
  }

}