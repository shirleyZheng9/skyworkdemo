package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeAccessDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeCatalogDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeDocumentDTO;
import com.iwhalecloud.bote.dto.knowledge.access.KnowledgeFileDTO;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeDocumentParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeFileQueryParams;
import com.iwhalecloud.bote.dto.knowledge.access.query.KnowledgeQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.strategy.IKnowledgeAccessStrategy;
import com.iwhalecloud.bote.doc.module.knowledge.strategy.KnowledgeAccessStrategyEntry;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * 知识库接入管理 controller
 *
 * @author lxs
 * @since 2025/07/14
 */
@RestController
@RequestMapping(path = CommonConsts.API_PREFIX + "knowledge/access", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "知识库接入管理")
public class KnowledgeAccessManageController {

  private final KnowledgeAccessStrategyEntry strategyEntry;

  @Operation(summary = "查询知识库目录")
  @GetMapping("queryKnowledgeCatalog")
  @IgnoreTenant
  public ResultVO<List<KnowledgeCatalogDTO>> queryKnowledgeCatalog(@RequestParam(name = "knowledgeType") String knowledgeType) {
    Assert.hasLength(knowledgeType, "知识库类型不能为空");
    IKnowledgeAccessStrategy accessService = strategyEntry.getAccessStrategy(knowledgeType);
    return accessService.queryKnowledgeCatalog();
  }

  @Operation(summary = "分页查询知识库列表")
  @PostMapping("queryKnowledgeInfoPage")
  @IgnoreTenant
  public ResultVO<PageInfo<KnowledgeAccessDTO>> queryKnowledgeInfoPage(@RequestBody KnowledgeQueryParams queryParams) {
    Assert.hasLength(queryParams.getKnowledgeType(), "知识库类型不能为空");
    IKnowledgeAccessStrategy accessService = strategyEntry.getAccessStrategy(queryParams.getKnowledgeType());
    return accessService.queryKnowledgeInfoPage(queryParams);
  }

  @Operation(summary = "分页查询知识库文档列表")
  @PostMapping("queryKnowledgeFilePage")
  @IgnoreTenant
  public ResultVO<PageInfo<KnowledgeFileDTO>> queryKnowledgeFilePage(@RequestBody KnowledgeFileQueryParams queryParams) {
    Assert.hasLength(queryParams.getKnowledgeType(), "知识库类型不能为空");
    IKnowledgeAccessStrategy accessService = strategyEntry.getAccessStrategy(queryParams.getKnowledgeType());
    return accessService.queryKnowledgeFilePage(queryParams);
  }

  @PostMapping("getKnowledgeDoc")
  @Operation(summary = "获取知识库文档")
  @IgnoreTenant
  public ResultVO<KnowledgeDocumentDTO> getKnowledgeDocument(@RequestBody KnowledgeDocumentParams documentParams) {
    Assert.notNull(documentParams.getDocId(), "文档 ID 不能为空");
    Assert.hasLength(documentParams.getKnowledgeType(), "知识库类型不能为空");
    IKnowledgeAccessStrategy accessService = strategyEntry.getAccessStrategy(documentParams.getKnowledgeType());
    return accessService.getKnowledgeDocument(documentParams);
  }

  @GetMapping("downloadKnowledgeDoc")
  @Operation(summary = "下载知识库文档")
  @SuppressFBWarnings({"CRLF_INJECTION_LOGS", "XSS_SERVLET"})
  @IgnoreTenant
  public void downloadKnowledgeDoc(@RequestParam("knowledgeType") String knowledgeType,
    @RequestParam("docId") Long docId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    Assert.notNull(docId, "文档 ID 不能为空");
    Assert.hasLength(knowledgeType, "知识库类型不能为空");
    IKnowledgeAccessStrategy accessService = strategyEntry.getAccessStrategy(knowledgeType);
    accessService.downloadKnowledgeDoc(docId, httpServletRequest, httpServletResponse);
  }

}
