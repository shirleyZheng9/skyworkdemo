package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaDocumentChunkRefernceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordNoticeBoardDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateBtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordNoticeBoardQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.BtDcQaRecordQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IBtDcQaRecordManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 问答记录表管理 controller
 *
 * @author linmengfan
 * @since 2025-09-13
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/bkquestions", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "问答记录表表管理")
public class BtDcQaRecordManageController {

  private final IBtDcQaRecordManageService btDcQaRecordManageService;

  @Operation(summary = "查询单个问答记录表")
  @GetMapping("findBtDcQaRecord")
  public ResultVO<BtDcQaRecordDTO> findBtDcQaRecord(@RequestParam(name = "qaId") Long qaId) {
    Assert.notNull(qaId, "主键 ID 不能为空");
    return ResultVO.success(btDcQaRecordManageService.findBtDcQaRecord(qaId));
  }

  @Operation(summary = "分页查询问答记录表")
  @PostMapping("queryBtDcQaRecordPage")
  public ResultVO<PageInfo<BtDcQaRecordDTO>> queryBtDcQaRecordPage(@RequestBody BtDcQaRecordQueryParams queryParams) {
    return ResultVO.success(btDcQaRecordManageService.queryBtDcQaRecordPage(queryParams));
  }

  @Operation(summary = "分页查询问答引用记录表")
  @GetMapping("queryBtDcQaDocumentChunkRefernceDTOList")
  public ResultVO<List<BtDcQaDocumentChunkRefernceDTO>> queryBtDcQaDocumentChunkRefernceDTOList(
    @RequestParam(name = "qaId") Long qaId, @RequestParam(name = "tenantId") Long tenantId) {
    return ResultVO.success(btDcQaRecordManageService.queryBtDcQaDocumentChunkRefernceDTOList(qaId, tenantId));
  }

  @Operation(summary = "查询问答记录看板信息")
  @PostMapping("findBoticeBoardBtDcQaRecord")
  public ResultVO<BtDcQaRecordNoticeBoardDTO> findNoticeBoardBtDcQaRecord(@RequestBody BtDcQaRecordNoticeBoardQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户id不能为空");
    Assert.notNull(params.getTimeType(), "查询周期维度不能为空");
    return ResultVO.success(btDcQaRecordManageService.findNoticeBoardBtDcQaRecord(params));
  }

  @PostMapping("updateBtDcQaRecord")
  @Operation(summary = "更新问答记录的点评或者欢喜")
  @IgnoreTenant
  public ResultVO<Void> updateBtDcQaRecord(@RequestBody UpdateBtDcQaRecordDTO request) {
    Assert.notNull(request.getClientId(), "问答记录id");
    return btDcQaRecordManageService.updateBtDcQaRecord(request);
  }

  @Operation(summary = "删除知识库问答记录")
  @GetMapping("deleteBtDcQaRecordInfo")
  public ResultVO<Void> deleteBtDcQaRecordInfo(
    @RequestParam(name = "qaId") Long qaId, @RequestParam(name = "tenantId") Long tenantId) {
    btDcQaRecordManageService.deleteBtDcQaRecordInfo(qaId, tenantId);
    return ResultVO.success();
  }

}
