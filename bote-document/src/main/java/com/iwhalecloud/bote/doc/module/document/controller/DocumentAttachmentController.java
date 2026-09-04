package com.iwhalecloud.bote.doc.module.document.controller;

import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.DocumentAttachmentCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.annotation.IgnoreTenant;
import com.iwhalecloud.bote.doc.common.utils.ContentTypeUtil;
import com.iwhalecloud.bote.doc.consts.DocumentPermConsts;
import com.iwhalecloud.bote.doc.module.control.base.ControlTemplate;
import com.iwhalecloud.bote.doc.module.control.base.permission.NodePermission;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentUploadRO;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Aiqing
 * @since 2025/9/9
 */
@RestController
@RequestMapping(DocBaseConsts.API_PREFIX + "dc/document/attachment")
@RequiredArgsConstructor
@Tag(name = "文档中心：文档库文档")
public class DocumentAttachmentController {

  private static final Logger logger = LoggerFactory.getLogger(DocumentAttachmentController.class);

  /**
   * 图片文件缓存时间（秒）- 1小时
   */
  private static final int IMAGE_CACHE_MAX_AGE = 3600;

  private final IDocumentAttachmentService documentAttachmentService;
  private final ControlTemplate controlTemplate;
  private final IDocumentService documentService;
  private final DocumentAttachmentCache documentAttachmentCache;
  private final IFileStoreService fileStoreService;

  @Operation(summary = "查询附件信息")
  @GetMapping("/getAttachmentInfo")
  public ResultVO<DocumentAttachmentDTO> getAttachmentInfo(@RequestParam Long attachmentId) {
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    DocumentAttachmentDTO attachmentWithCheck = documentAttachmentCache.getAttachmentWithCheck(attachmentId, currentLoginUserId);
    if (attachmentWithCheck == null || attachmentWithCheck.getAttachmentId() == null) {
      return ResultVO.fail("附件不存在");
    }
    return ResultVO.success(attachmentWithCheck);
  }

  @Operation(
    summary = "上传文件",
    description = "上传图片或者附件到指定文档"
  )
  @PostMapping(value = "/upload", consumes = "multipart/form-data")
  public ResultVO<DocumentAttachmentDTO> upload(MultipartFile file, @Valid DocumentAttachmentUploadRO uploadRO) {
    String documentId = uploadRO.getDocumentId();
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      return ResultVO.fail("文档不存在");
    }
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    // 检查编辑权限
    controlTemplate.checkNodePermission(documentDTO.getLibraryId(), currentLoginUserId, documentId,
      NodePermission.EDIT_NODE, DocumentPermConsts.ACCESS_DENIED_CALLBACK);
    Path tempFile = null;
    DocumentAttachmentDTO attachmentDTO;
    try {
      tempFile = Files.createTempFile("bote-ocr-", file.getOriginalFilename());
      file.transferTo(tempFile.toFile());
       attachmentDTO = documentAttachmentService.upload(tempFile.toFile(), documentId, currentLoginUserId);
    }
    catch (IOException e) {
      throw new BssException("文件处理异常: " + e.getMessage(), e);
    }
    finally {
      if (tempFile != null) {
        try {
          Files.deleteIfExists(tempFile);
        }
        catch (IOException e) {
          logger.warn("删除临时文件失败: {}", tempFile, e);
        }
      }
    }

    return ResultVO.success(attachmentDTO);
  }

  @Operation(summary = "下载附件文件", description = "通过编码的文件名下载文档附件")
  @GetMapping("/files/{documentId}/{fileName}")
  @IgnoreTenant
  public void getAttachmentFile(
    @Parameter(description = "文档ID", required = true) @PathVariable String documentId,
    @Parameter(description = "编码后的文件名", required = true) @PathVariable String fileName,
    HttpServletRequest request,
    HttpServletResponse response) {

    Long attachmentId = documentAttachmentService.resolveAttachmentId(fileName);

    if (attachmentId == null) {
      throw new BssException("无效的文件名编码");
    }

    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    DocumentAttachmentDTO attachmentWithCheck = documentAttachmentCache.getAttachmentWithCheck(attachmentId, currentLoginUserId);

    if (attachmentWithCheck == null || attachmentWithCheck.getAttachmentId() == null
      || !Objects.equals(documentId, attachmentWithCheck.getDocumentId())) {
      throw new BssException("附件不存在");
    }
    // 是否是图片
    boolean isPicture = Boolean.TRUE.equals(attachmentWithCheck.getIsPicture());

    String fileRealName = attachmentWithCheck.getFileName();
    try (InputStream inputStream = fileStoreService.downloadFileStream(attachmentWithCheck)) {
      // 设置响应头
      response.setContentType(ContentTypeUtil.getContentType(fileRealName));
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileRealName, StandardCharsets.UTF_8).build().toString());
      // 根据文件类型设置不同的缓存策略
      if (isPicture) {
        // 图片文件设置短期缓存，提高加载性能
        response.setHeader("Cache-Control", "public, max-age=" + IMAGE_CACHE_MAX_AGE);
        response.setHeader("Expires", String.valueOf(System.currentTimeMillis() + IMAGE_CACHE_MAX_AGE * 1000L));

        // 设置ETag用于缓存验证（使用附件ID和文件ID组合）
        String etag = "\"" + attachmentWithCheck.getAttachmentId() + "-" + attachmentWithCheck.getFileId() + "\"";
        response.setHeader("ETag", etag);

        // 检查客户端缓存，如果文件未修改则返回304
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }
      }
      else {
        // 其他文件（如文档、压缩包等）不缓存，确保数据安全
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
      }

      ServletOutputStream outputStream = response.getOutputStream();  //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
      StreamUtils.copy(inputStream, outputStream);
    }
    catch (Exception e) {
      logger.error("文件下载失败，documentId:{}. fileName:{}", documentId, fileName, e);
      // 如果下载过程中出现异常，返回错误信息
      response.reset();
      throw new BssException("文件下载失败", e);
    }
  }

}
