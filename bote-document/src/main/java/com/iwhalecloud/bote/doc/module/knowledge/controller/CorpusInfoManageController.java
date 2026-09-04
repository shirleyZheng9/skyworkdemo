package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.knowledge.CorpusInfoDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.CorpusQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.ICorpusInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档基本信息管理 controller
 *
 * @author auto
 * @since 2025-01-13
 */
@RestController
@RequestMapping(path = CommonConsts.API_PREFIX + "manager/corpus", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "语料基本信息管理")
public class CorpusInfoManageController {

  private final ICorpusInfoManageService corpusInfoManageService;

  @Operation(summary = "查询单个语料基本信息")
  @GetMapping("findCorpusInfo")
  public ResultVO<CorpusInfoDTO> findCorpusInfo(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "corpusId") Long corpusId) {
    Assert.notNull(corpusId, "主键 ID 不能为空");
    return ResultVO.success(corpusInfoManageService.findCorpusInfo(tenantId, corpusId));
  }

  @Operation(summary = "保存语料基本信息")
  @PostMapping("saveCorpusInfo")
  public ResultVO<CorpusInfoDTO> saveCorpusInfo(@RequestPart("corpusInfo") CorpusInfoDTO corpusInfo,
    @RequestPart(name = "file", required = false) MultipartFile file) {
    return corpusInfoManageService.saveCorpusInfo(corpusInfo, file);
  }

  @Operation(summary = "删除语料基本信息")
  @GetMapping("deleteCorpusInfo")
  public ResultVO<Void> deleteCorpusInfo(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam(name = "corpusId") Long corpusId) {
    Assert.notNull(corpusId, "主键 ID 不能为空");
    return corpusInfoManageService.deleteCorpusInfo(tenantId, corpusId);
  }

  @Operation(summary = "查询语料基本信息列表")
  @PostMapping("queryCorpusInfoList")
  public ResultVO<List<CorpusInfoDTO>> queryCorpusInfoList(@RequestBody CorpusQueryParams queryParams) {
    return ResultVO.success(corpusInfoManageService.queryCorpusInfoList(queryParams));
  }

  @Operation(summary = "分页查询语料基本信息")
  @PostMapping("queryCorpusInfoPage")
  public ResultVO<PageInfo<CorpusInfoDTO>> queryCorpusInfoPage(@RequestBody CorpusQueryParams queryParams) {
    return ResultVO.success(corpusInfoManageService.queryCorpusInfoPage(queryParams));
  }
}
