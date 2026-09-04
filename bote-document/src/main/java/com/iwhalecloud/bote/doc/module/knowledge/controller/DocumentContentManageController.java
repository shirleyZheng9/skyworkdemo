package com.iwhalecloud.bote.doc.module.knowledge.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentContentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.DocumentParameterDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocumentContentQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentContentManageService;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeBaseOperateDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * @author qian.sisheng
 * @since 2025-3-12
 */
@RestController
@RequestMapping(path = CommonConsts.API_PREFIX + "manager/documentContent", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档内容管理")
public class DocumentContentManageController {
  private final IDocumentContentManageService contentManageService;
  @Operation(summary = "保存语料参数列表")
  @PostMapping("saveCorpusParameter")
  public ResultVO<DocumentDTO> saveCorpusParameter(@RequestBody DocumentDTO document) {
    return contentManageService.saveDocumentParameter(document);
  }

  @Operation(summary = "分页查询语料参数列表")
  @PostMapping("queryDocumentParameterPage")
  public ResultVO<PageInfo<DocumentParameterDTO>> queryDocumentParameterPage(@RequestBody DocumentContentQueryParams queryParams) {
    return ResultVO.success(contentManageService.queryDocumentParameterPage(queryParams));
  }

  @Operation(summary = "分页查询语料参数列表")
  @PostMapping("queryDocumentParameterList")
  public ResultVO<List<DocumentParameterDTO>> queryDocumentParameterList(@RequestBody DocumentContentQueryParams queryParams) {
    return ResultVO.success(contentManageService.queryDocumentParameterList(queryParams));
  }

  @Operation(summary = "保存文档内容")
  @PostMapping("saveDocumentContent")
  public ResultVO<Void> saveCorpusContent(@RequestBody DocumentDTO document) {
    return contentManageService.saveDocumentContent(document);
  }

  @Operation(summary = "更新文档列内容")
  @PostMapping("updateDocumentContentCellValue")
  public ResultVO<Void> updateDocumentContentCellValue(@RequestBody DocumentDTO document) {
    return contentManageService.updateDocumentContentCellValue(document);
  }

  @Operation(summary = "删除文档内容")
  @PostMapping("deleteDocumentContent")
  public ResultVO<Void> deleteDocumentContent(@RequestBody DocumentDTO document) {
    return contentManageService.deleteDocumentContent(document);
  }

  @Operation(summary = "分页查询文档内容列表")
  @PostMapping("queryDocumentContentPage")
  public ResultVO<PageInfo<DocumentContentDTO>> queryDocumentContentPage(@RequestBody DocumentContentQueryParams queryParams) {
    return ResultVO.success(contentManageService.queryDocumentContentPage(queryParams));
  }

  @Operation(summary = "查询文档内容列表")
  @PostMapping("queryDocumentContentList")
  public ResultVO<List<DocumentContentDTO>> queryDocumentContentList(@RequestBody DocumentContentQueryParams queryParams) {
    return ResultVO.success(contentManageService.queryDocumentContentList(queryParams));
  }

  @Operation(summary = "导入文档内容")
  @PostMapping("importDocumentContent")
  public ResultVO<Void> importDocumentContent(@RequestParam(name = "documentId") Long documentId, @RequestParam(name = "importType") String importType,
    @RequestParam(name = "file") MultipartFile file, @RequestParam(name = "tenantId") Long tenantId) {
    Assert.notNull(documentId, "文档 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    return contentManageService.importDocumentContent(tenantId, documentId, file, null, importType);
  }

  @Operation(summary = "导出文档内容，用于语料管理")
  @GetMapping("exportDocumentContent")
  public void exportDocumentContent(@RequestParam(name = "documentId") Long documentId, @RequestParam(name = "tenantId") Long tenantId,
    HttpServletResponse response) {
    Assert.notNull(documentId, "文档 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    contentManageService.exportDocumentContent(tenantId, documentId, response);
  }

  @Operation(summary = "发布文档，用于通知更新关联的知识库文档")
  @GetMapping("publishDocument")
  public ResultVO<Void> publishDocument(@RequestParam(name = "documentId") Long documentId, @RequestParam(name = "tenantId") Long tenantId) {
    Assert.notNull(documentId, "主键 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    contentManageService.publishDocument(tenantId, documentId);
    return ResultVO.success();
  }

  @Operation(summary = "导出模板")
  @GetMapping("exportTemplate")
  public void exportTemplate(@RequestParam(name = "documentId") Long documentId, @RequestParam(name = "tenantId") Long tenantId,
    HttpServletResponse response) {
    Assert.notNull(documentId, "文档 ID 不能为空");
    Assert.notNull(tenantId, "租户 ID 不能为空");
    contentManageService.exportTemplate(tenantId, documentId, response);
  }

  /**
   * 根据条件查询知识文档列表
   * write by zyt 2025.5.20 11465479
   *
   * @param knowledgeBaseOperateData 包含查询条件的参数
   * @return 返回一个包含查询结果的列表，每个结果为一个键值对映射
   */
  @Operation(summary = "根据条件查询知识")
  @PostMapping("queryDocumentListByCondition")
  public List<Map<String, Object>> queryDocumentListByCondition(@RequestBody KnowledgeBaseOperateDTO knowledgeBaseOperateData) {
    // 查询文档列
    Long documentId = knowledgeBaseOperateData.getDocumentId();
    Assert.notNull(documentId, "文档 ID 不能为空");
    Long tenantId = knowledgeBaseOperateData.getTenantId();
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Map<String, Object> params = knowledgeBaseOperateData.getContent();
    return contentManageService.queryDocumentListByCondition(documentId, tenantId, params);
  }

  /**
   * 更新/插入知识内容
   * write by zyt 2025.5.20 11465479
   *
   * @param knowledgeBaseOperateData 包含查询条件
   * @return 返回一个包含查询结果的映射
   */
  @Operation(summary = "更新/插入知识内容")
  @PostMapping("saveDocument")
  public Map<String, Object> saveDocument(@RequestBody KnowledgeBaseOperateDTO knowledgeBaseOperateData) {
    Long documentId = knowledgeBaseOperateData.getDocumentId();
    Assert.notNull(documentId, "文档 ID 不能为空");
    Long tenantId = knowledgeBaseOperateData.getTenantId();
    Assert.notNull(tenantId, "租户 ID 不能为空");
    Map<String, Object> paramsMap = knowledgeBaseOperateData.getContent();
    return contentManageService.saveDocument(documentId, tenantId, paramsMap);
  }
}
