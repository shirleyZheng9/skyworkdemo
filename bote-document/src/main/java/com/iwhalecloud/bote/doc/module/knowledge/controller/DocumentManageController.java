package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.TenantIdUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.consts.KnowledgeConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocDeleteDocumentsDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentAddParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.IKnowledgeBaseManageService;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainConfigHelper;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainDocumentDetailDTO;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainDocumentHelper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档管理 controller
 *
 * @author auto
 * @since 2024-09-20
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "manager/document", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "知识库：文档管理")
public class DocumentManageController {

  private final IKnowledgeBaseManageService knowledgeBaseManageService;
  private final IDocumentManageService documentManageService;
  private final IRefreshCacheService refreshCacheService;
  private final DocChainDocumentHelper docChainDocumentHelper;
  private final DocChainConfigHelper docChainConfigHelper;

  @Operation(summary = "查询单个文档")
  @GetMapping("findDocument")
  public ResultVO<DocumentDTO> findDocument(@RequestParam("documentId") Long documentId,
    @RequestParam(name = "withContent", required = false) Boolean withContent, @RequestParam(name = "isExist", required = false) String isExist,
    @RequestParam(name = "knowledgeId", required = false) Long knowledgeId, @RequestParam("tenantId") Long tenantId,
    @RequestParam(name = "spaceId", required = false) Long spaceId) {
    Assert.notNull(documentId, "文档 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    if (!DocBaseConsts.TRUE.equals(isExist)) {
      return ResultVO.success(handlePlatformDocument(tenantId, documentId, withContent));
    }
    else if (Boolean.TRUE.equals(withContent)) {
      return ResultVO.success(handleDocChainDocument(tenantId, documentId, knowledgeId));
    }
    else {
      // 其他类型或不需要内容时，返回空对象
      return ResultVO.success(new DocumentDTO());
    }
  }

  private DocumentDTO handlePlatformDocument(Long tenantId, Long documentId, Boolean withContent) {
    DocumentDTO document = documentManageService.findDocument(tenantId, documentId);
    Assert.notNull(document, "文档不存在");
    if (Boolean.TRUE.equals(withContent) && document.getExtSystemId() != null) {
      if (!docChainConfigHelper.existsTopicDocId(document.getTenantId(), document.getTopicId(), document.getExtSystemId())) {
        throw new BssException("请更新学习知识！");
      }
      String readFormat = getReadFormat(tenantId, document.getKnowledgeId());
      document.setContent(
        docChainDocumentHelper.readDocument(document.getTenantId(), document.getExtSystemId(),
          readFormat));
    }
    return document;
  }

  private DocumentDTO handleDocChainDocument(Long tenantId, Long documentId, Long knowledgeId) {
    DocChainDocumentDetailDTO documentDetail = docChainDocumentHelper.getDocumentDetail(tenantId, documentId);
    DocumentDTO document = new DocumentDTO();
    document.setDocumentId(documentId);
    document.setKnowledgeId(knowledgeId);
    document.setDocName(documentDetail.getPath());
    String readFormat = getReadFormat(tenantId, knowledgeId);
    document.setContent(docChainDocumentHelper.readDocument(tenantId, documentId, readFormat));
    return document;
  }

  private String getReadFormat(Long tenantId, Long knowledgeId) {
    if (knowledgeId == null) {
      return null;
    }
    Boolean isCommonTopic = knowledgeBaseManageService.findKnowledgeBase(tenantId, knowledgeId).getIsCommonTopic();
    return BooleanUtils.isNotTrue(isCommonTopic) ? KnowledgeConsts.READ_FORMAT_HTML : null;
  }

  @Operation(summary = "新增文档")
  @PostMapping("addDocument")
  public ResultVO<Object> addDocument(@RequestBody DocumentAddParams params) {
    if (params.getSpaceId() == null) {
      params.setSpaceId(TenantIdUtil.getSpaceId(params.getTenantId()));
    }
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.notNull(params.getKnowledgeId(), "知识库 ID 不能为空");
    Assert.isTrue(
      !(CollectionUtils.isEmpty(params.getFileInfoIds()) && CollectionUtils.isEmpty(params.getStructFileInfoIds()) && CollectionUtils.isEmpty(
        params.getDcDocumentIds())), "文件关联 ID 集合不能为空");
    if (CollectionUtils.isEmpty(params.getDcDocumentIds())) {
      ResultVO<Object> result = documentManageService.addDocument(params);
      if (result.isSuccess()) {
        refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, params.getTenantId() + ":" + params.getKnowledgeId());
      }
      return result;
    }
    else {
      ResultVO<Object> result = documentManageService.addDocumentNew(params);
      if (result.isSuccess()) {
        refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, params.getTenantId() + ":" + params.getKnowledgeId());
      }
      return result;
    }
  }

  @Operation(summary = "删除多个文档")
  @PostMapping("deleteDocuments")
  public ResultVO<Long> deleteDocuments(@RequestBody DocDeleteDocumentsDTO deleteDocumentsDTO) {
    ResultVO<Long> result = documentManageService.deleteDocuments(deleteDocumentsDTO);
    if (result.isSuccess()) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, deleteDocumentsDTO.getTenantId() + ":" + result.getResultObject());
    }
    return result;
  }

  @Operation(summary = "删除文档")
  @GetMapping("deleteDocument")
  public ResultVO<Long> deleteDocument(@RequestParam(name = "tenantId") Long tenantId, @RequestParam(name = "documentId") Long documentId) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(documentId, "主键 ID 不能为空");
    ResultVO<Long> result = documentManageService.deleteDocument(tenantId, documentId);
    if (result.isSuccess()) {
      refreshCacheService.refresh(DocCacheConsts.CACHE_NAME_KNOWLEDGE, tenantId + ":" + result.getResultObject());
    }
    return result;
  }

  @Operation(summary = "查询文档列表")
  @PostMapping("queryDocumentList")
  public ResultVO<List<DocumentDTO>> queryDocumentList(@RequestBody DocumentQueryParams queryParams) {
    return ResultVO.success(documentManageService.queryDocumentList(queryParams));
  }

  @Operation(summary = "分页查询文档")
  @PostMapping("queryDocumentPage")
  public ResultVO<PageInfo<DocumentDTO>> queryDocumentPage(@RequestBody DocumentQueryParams queryParams) {
    return documentManageService.queryDocumentPage(queryParams);
  }

  @Operation(summary = "文档构建")
  @GetMapping("buildDocument")
  public ResultVO<Void> buildDocument(@RequestParam(name = "tenantId") Long tenantId, @RequestParam(name = "documentId") Long documentId,
    @RequestParam(name = "newDoc", required = false) Boolean newDoc, @RequestParam(name = "isExist", required = false) String isExist) {
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Assert.notNull(documentId, "主键 ID 不能为空");
    return documentManageService.buildDocument(tenantId, documentId, newDoc, isExist);
  }

  @IgnoreTenant
  @Operation(summary = "同步或者发布文档同步知识库文档")
  @GetMapping("documentReleased")
  public ResultVO<Void> documentReleased(@RequestParam String documentId) {
    documentManageService.documentReleased(documentId);
    return ResultVO.success();
  }

  @IgnoreTenant
  @Operation(summary = "从fileInfo创建文档节点")
  @GetMapping("createDocumentNodesFromFileInfo")
  public ResultVO<Void> createDocumentNodesFromFileInfo() {
    return documentManageService.createDocumentNodesFromFileInfo();
  }

  @Operation(summary = "查询文档信息", description = "用于工作流知识节点展示文档信息")
  @PostMapping("queryDocumentInfo")
  public ResultVO<Map<String, Object>> queryDocumentInfo(@RequestBody DocumentQueryParams params) {
    Assert.notNull(params.getTenantId(), "租户 ID 不能为空");
    Assert.hasText(params.getKnowledgeIdExpr(), "知识库不能为空");
    return documentManageService.queryDocumentInfo(params.getTenantId(), params.getKnowledgeIdExpr());
  }
}
