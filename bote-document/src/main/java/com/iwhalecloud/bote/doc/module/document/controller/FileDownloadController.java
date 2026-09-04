package com.iwhalecloud.bote.doc.module.document.controller;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.FileDownloadCache;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.common.utils.ContentTypeUtil;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.control.base.role.ControlRole;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.OnlineDocumentInfoDTO;
import com.iwhalecloud.bote.doc.module.document.service.IDocContentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentExportService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.doc.module.document.service.IWorkbookContentService;
import com.iwhalecloud.bote.dto.base.FileDownloadToken;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件下载控制器 为kkfileview提供带token验证的文件下载服务
 *
 * @author Aiqing
 * @since 2025-09-06
 */
@RestController
@RequestMapping(DocBaseConsts.API_PREFIX + "dc/document/file")
@RequiredArgsConstructor
@Tag(name = "文档中心-文件下载")
@SuppressWarnings("PMD.GuardLogStatement")
public class FileDownloadController {
  private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

  //@formatter:off
  private final FileDownloadCache dcDocumentDownloadCache;
  private final IFileStoreService fileStoreService;
  private final IDocumentService documentService;
  private final ControlTemplate controlTemplate;
  private final IDocumentExportService documentExportService;
  private final IDocContentService documentContentService;
  private final IWorkbookContentService workbookContentService;
  //@formatter:on

  /**
   * 通过Token下载文件 供kkfileview调用的文件下载接口
   *
   * @param token 文件下载token
   * @param response HTTP响应
   */
  @Operation(summary = "通过Token下载文件", description = "供kkfileview调用的文件下载接口")
  @GetMapping("/download/{token}")
  @IgnoreSession
  @IgnoreTenant
  public void downloadFileByToken(@Parameter(description = "文件下载token") @PathVariable String token, HttpServletResponse response) {
    try {
      // 1. 校验token
      FileDownloadToken tokenInfo = dcDocumentDownloadCache.validateAndGetToken(token);
      if (tokenInfo == null) {
        logger.warn("无效的文件下载token: {}", token);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Invalid or expired token");
        return;
      }
      Long fileId = tokenInfo.getFileId();
      FileInfoVO fileInfoVO = fileStoreService.getFileInfoById(fileId);
      if (fileInfoVO == null) {
        logger.warn("文档文件不存在， fileId: {}, documentId:{}", fileId, tokenInfo.getDocumentId());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("文档文件不存在");
        return;
      }
      // 2. 设置响应头
      setupResponseHeaders(response, tokenInfo.getFileName(), fileInfoVO.getFileName());

      // 3. 下载文件
      downloadFileFromStore(fileInfoVO, response);

      logger.debug("文件下载完成: token={}, fileName={}, size:{}",
        token, tokenInfo.getFileName(), FileUtils.byteCountToDisplaySize(fileInfoVO.getFileSize()));
    }
    catch (Exception e) {
      logger.error("文件下载异常: token={}, error={}", token, e.getMessage(), e);
      try {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("File download failed: " + e.getMessage());
      }
      catch (IOException ioException) {
        logger.error("写入错误响应失败", ioException);
      }
    }
  }

  @Operation(summary = "根据文档 ID 下载文件")
  @GetMapping(path = "/downloadById/{documentId}", produces = MediaType.ALL_VALUE)
  public void downloadById(@Parameter(description = "文件 ID", required = true)
                           @PathVariable("documentId") String documentId,
                           HttpServletResponse response) {
    try {
      DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
      if (documentDTO == null) {
        response.sendError(HttpStatus.NOT_FOUND.value(), "文档不存在");
        return;
      }
      // 权限判断：检查用户是否有导出权限
      Long userId = SessionUtil.getLoginInfo().getUserId();
      ControlRole controlRole = controlTemplate.fetchNodeRole(documentDTO.getLibraryId(), userId, documentId);
      boolean hasExportPermission = controlRole.hasPermission(NodePermission.EXPORT_NODE);
      if (!hasExportPermission) {
        response.sendError(HttpStatus.FORBIDDEN.value(), "无权限下载文件");
        return;
      }

      // 判断文档类型，只有在线文档/在线表格需要保存历史版本
      DocumentTypeEnum documentType = DocumentTypeEnum.getByCode(documentDTO.getDocumentType());
      if (documentType != null && documentType.isOnlineDocument()) {
        if (DocumentTypeEnum.WORD_ONLINE == documentType) {
          // 在线文档：获取文档内容并保存历史版本
          OnlineDocumentInfoDTO documentInfoDTO = documentContentService.findContentByDocumentId(documentId);
          documentContentService.saveContentHistory(documentId, JsonUtil.toJsonString(documentInfoDTO.getContent()), documentInfoDTO.getUpdatorId());
        }
        else if (DocumentTypeEnum.EXCEL_ONLINE == documentType) {
          // 在线表格：获取表格内容并保存历史版本
          workbookContentService.saveContentHistory(documentId, userId);
        }
      }

      documentExportService.downloadDocument(documentDTO, response);
    }
    catch (Exception e) {
      logger.error("文件下载异常: documentId={}, error={}", documentId, e.getMessage(), e);
      try {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("File download failed: " + e.getMessage());
      }
      catch (IOException ioException) {
        logger.error("写入错误响应失败", ioException);
      }
    }
  }


  /**
   * 设置响应头
   */
  private void setupResponseHeaders(HttpServletResponse response, String downloadFileName, String fileOriginalName) {
    response.setContentType(ContentTypeUtil.getContentType(fileOriginalName));

    // 设置文件名
    if (StringUtils.isNotBlank(downloadFileName)) {
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(downloadFileName, StandardCharsets.UTF_8).build().toString());
    }

    // 设置缓存控制
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
    response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    response.setHeader(HttpHeaders.EXPIRES, "0");
  }

  /**
   * 从URL下载文件并写入响应
   */
  private void downloadFileFromStore(FileInfoVO fileInfoVO, HttpServletResponse response) throws IOException {
    try (InputStream inputStream = fileStoreService.downloadFileStream(fileInfoVO)) {
      IOUtils.copy(inputStream, response.getOutputStream());
      response.getOutputStream().flush();
    }
  }

}
