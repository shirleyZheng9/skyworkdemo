package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.BtDcQaRecordItemDto;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsFeedBackRatioDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsComprehensiveDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsContributorDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsHotQuestionDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeOpsOverviewDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeReferenceDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeOpsQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowlegeOpsService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库运营看板 Controller
 *
 * @author qian.sisheng
 * @since 2026/02/27
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "manager/knowledge/ops", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "知识库：运营看板")
public class KnowledgeOpsController {

  private final IKnowlegeOpsService knowledgeOpsService;

  @Operation(summary = "查询知识库运营总览")
  @PostMapping("queryKnowledgeOpsOverview")
  public ResultVO<KnowledgeOpsOverviewDTO> queryKnowledgeOpsOverview(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryKnowledgeOpsOverview(params));
  }

  @Operation(summary = "分页查询知识库引用频次排名")
  @PostMapping("queryKnowledgeReferenceRank")
  public ResultVO<PageInfo<KnowledgeReferenceDTO>> queryKnowledgeReferenceRank(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryKnowledgeReferenceRank(params));
  }

  @Operation(summary = "分页查询文档引用频次排名")
  @PostMapping("queryDocumentReferenceRank")
  public ResultVO<PageInfo<DocumentReferenceDTO>> queryDocumentReferenceRank(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryDocumentReferenceRank(params));
  }

  @Operation(summary = "分页查询高频议题统计")
  @PostMapping("queryHotQuestionRank")
  public ResultVO<PageInfo<KnowledgeOpsHotQuestionDTO>> queryHotQuestionRank(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryHotQuestionRank(params));
  }

  @Operation(summary = "查询点赞/点踩比例")
  @PostMapping("queryFeedbackRatio")
  public ResultVO<KnowledgeOpsFeedBackRatioDTO> queryFeedbackRatio(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryFeedbackRatio(params));
  }

  @Operation(summary = "查询点踩原因分布统计")
  @PostMapping("queryDislikeReasonStats")
  public ResultVO<List<BtDcQaRecordItemDto>> queryDislikeReasonStats(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryDislikeReasonStats(params));
  }

  @Operation(summary = "分页查询文档贡献者排名")
  @PostMapping("queryDocContributorRank")
  public ResultVO<PageInfo<KnowledgeOpsContributorDTO>> queryDocContributorRank(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryDocContributorRank(params));
  }

  @Operation(summary = "查询文档贡献分时统计")
  @PostMapping("queryDocContributeDailyStats")
  public ResultVO<List<BtDcQaRecordItemDto>> queryDocContributeDailyStats(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryDocContributeDailyStats(params));
  }

  @Operation(summary = "分页查询用户提问排名")
  @PostMapping("queryUserQuestionRank")
  public ResultVO<PageInfo<KnowledgeOpsContributorDTO>> queryUserQuestionRank(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryUserQuestionRank(params));
  }

  @Operation(summary = "查询用户提问分时统计")
  @PostMapping("queryUserQuestionDailyStats")
  public ResultVO<List<BtDcQaRecordItemDto>> queryUserQuestionDailyStats(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryUserQuestionDailyStats(params));
  }

  @Operation(summary = "分页查询综合排名")
  @PostMapping("queryComprehensiveRank")
  public ResultVO<PageInfo<KnowledgeOpsComprehensiveDTO>> queryComprehensiveRank(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryComprehensiveRank(params));
  }

  @Operation(summary = "分页查询点踩原因")
  @PostMapping("queryQaRecordPage")
  public ResultVO<PageInfo<BtDcQaRecordDTO>> queryQaRecordPage(@RequestBody KnowledgeOpsQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户ID不能为空");
    return ResultVO.success(knowledgeOpsService.queryQaRecordPage(params));
  }

  @Operation(summary = "更新反馈操作状态")
  @GetMapping("updateFeedbackOperateState")
  public ResultVO<Void> updateFeedbackOperateState(@RequestParam("qaId") Long qaId, @RequestParam("operateState") String operateState) {
    Assert.notNull(qaId, "qaId不能为空");
    Assert.hasText(operateState, "operateState不能为空");
    knowledgeOpsService.updateFeedbackOperateState(qaId, operateState);
    return ResultVO.success();
  }
}
