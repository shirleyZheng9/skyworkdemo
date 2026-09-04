package com.iwhalecloud.bote.doc.module.collaboration.doc.service;

import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.doc.cache.DcDocumentNodeCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.config.properties.NodeJsServerProperties;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.collaboration.doc.ro.DocumentImportRO;
import com.iwhalecloud.bote.doc.module.collaboration.doc.ro.DocumentMarkdownExportRO;
import com.iwhalecloud.bote.doc.module.collaboration.doc.ro.OnlineDocumentExportRO;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.DocumentImportResult;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.DocumentMarkdownExportResult;
import com.iwhalecloud.bote.doc.module.collaboration.doc.vo.DocumentRefreshResult;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

/**
 * 调用node的服务
 *
 * @author Aiqing
 * @since 2025/9/24
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class NodeJsService {

  private static final Logger logger = LoggerFactory.getLogger(NodeJsService.class);

  private static final String EXPORT_CONTENT_PATH = "/api/document/exportMarkdownContent";
  private static final String EXPORT_DOCX_PATH = "/api/document/exportDocx";
  private static final String IMPORT_CONTENT_PATH = "/api/document/import";
  private static final String REFRESH_CONTENT_PATH = "/api/collab/refresh/:documentId";

  private final NodeJsServerProperties serverProperties;
  private final DcDocumentNodeCache documentNodeCache;

  /**
   * 导出文档的markdown内容
   *
   * @param documentId 文档ID
   * @return markdown内容
   */
  public String exportDocumentContent(String documentId) {
    String endpoint = serverProperties.getEndpoint();
    String apiKey = serverProperties.getApiKey();
    RestTemplate restTemplate = HttpUtil.getRestTemplate();

    DocumentMarkdownExportRO exportRO = new DocumentMarkdownExportRO();
    exportRO.setDocumentId(documentId);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(apiKey);
    HttpEntity<DocumentMarkdownExportRO> httpEntity = new HttpEntity<>(exportRO, httpHeaders);
    DocumentMarkdownExportResult exportResult =
      restTemplate.postForObject(endpoint + EXPORT_CONTENT_PATH, httpEntity, DocumentMarkdownExportResult.class);
    return exportResult == null ? null : exportResult.getContent();
  }

  /**
   * 导出docx格式文档到临时文件
   *
   * @param documentId 文档ID
   * @return 临时文件路径
   * @throws IOException 文件操作异常
   */
  public Path exportDocumentWithDocxFormatToTempFile(String documentId, Path tempDir) throws IOException {
    // 验证文档类型
    NodeBaseInfoDTO nodeInfo = documentNodeCache.getDocumentNodeInfo(documentId);
    Assert.notNull(nodeInfo, "文档不存在");
    Assert.isTrue(Objects.equals(nodeInfo.getNodeType(), DocumentTypeEnum.WORD_ONLINE.getCode()), "非在线文档不能导出");

    String endpoint = serverProperties.getEndpoint();
    String apiKey = serverProperties.getApiKey();
    RestTemplate restTemplate = HttpUtil.getRestTemplate();

    // 构建导出请求参数
    OnlineDocumentExportRO exportRO = new OnlineDocumentExportRO();
    exportRO.setDocumentId(documentId);

    // 设置请求头
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(apiKey);

    HttpEntity<OnlineDocumentExportRO> httpEntity = new HttpEntity<>(exportRO, httpHeaders);
    logger.trace("开始导出文档到临时文件: documentId={}, format=docx", documentId);

    // 创建临时文件，包含documentId
//    String tempFilePrefix = String.format("nodejs_export_%s_", documentId);
//    Path tempFile = Files.createTempFile(tempFilePrefix, ".docx");
    Path tempFile = tempDir.resolve("excel_export_" + System.currentTimeMillis() + "." + DocBaseConsts.DOCUMENT_DOCX_EXTENSION);

    // 创建文件并设置访问权限（仅所有者可读写）
    if (!Files.exists(tempFile)) {
      try {
        Files.createFile(tempFile);
        // 设置文件权限为 rw-------（600）：仅所有者可读写
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
        Files.setPosixFilePermissions(tempFile, permissions);
      }
      catch (UnsupportedOperationException e) {
        // Windows系统不支持POSIX权限，忽略此异常
        logger.debug("当前系统不支持POSIX文件权限设置");
      }
    }

    try {
      ResponseEntity<byte[]> response = restTemplate.exchange(
        endpoint + EXPORT_DOCX_PATH,
        HttpMethod.POST,
        httpEntity,
        byte[].class
      );

      byte[] documentBytes = response.getBody();
      if (documentBytes == null || documentBytes.length == 0) {
        logger.warn("导出的文档内容为空: documentId={}", documentId);
        throw new BssException("导出的文档内容为空");
      }

      // 写入临时文件，使用缓冲流提高性能
      try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());
           BufferedOutputStream fileOutputStream = new BufferedOutputStream(fos)) {
        fileOutputStream.write(documentBytes);
        fileOutputStream.flush();
      }
      // 立即清空字节数组引用，帮助GC
      documentBytes = null;

      long fileSize = Files.size(tempFile);
      if (fileSize == 0) {
        logger.warn("导出的文档内容为空: documentId={}", documentId);
        throw new BssException("导出的文档内容为空");
      }

      if (logger.isDebugEnabled()) {
        logger.debug("文档导出到临时文件成功: documentId={}, tempFile={}, size={}",
          documentId, tempFile, FileUtils.byteCountToDisplaySize(fileSize));
      }

      return tempFile;
    }
    catch (Exception e) {
      // 如果出现异常，清理临时文件
      try {
        Files.deleteIfExists(tempFile);
      }
      catch (IOException cleanupException) {
        logger.warn("清理临时文件失败: {}, {}", tempFile, cleanupException.getMessage());
      }
      throw e;
    }
  }

  /**
   * 导入在线文档内容
   *
   * @param documentId 文档ID
   * @param format 导入内容格式(html/markdown)
   * @param content 导入内容
   * @param userId 操作用户ID
   * @return 导入结果
   */
  public DocumentImportResult importDocumentContent(String documentId, String format, String content, String userId) {
    String endpoint = serverProperties.getEndpoint();
    String apiKey = serverProperties.getApiKey();
    RestTemplate restTemplate = HttpUtil.getRestTemplate();

    DocumentImportRO importRO = new DocumentImportRO();
    importRO.setDocumentId(documentId);
    importRO.setFormat(format);
    importRO.setContent(content);
    importRO.setUserId(userId);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(apiKey);
    HttpEntity<DocumentImportRO> httpEntity = new HttpEntity<>(importRO, httpHeaders);

    logger.info("开始导入文档内容: documentId={}, format={}, userId={}", documentId, format, userId);

    DocumentImportResult importResult =
      restTemplate.postForObject(endpoint + IMPORT_CONTENT_PATH, httpEntity, DocumentImportResult.class);

    if (logger.isDebugEnabled()) {
      logger.debug("文档内容导入完成: documentId={}, resultCode={}, resultMsg={}",
        documentId, importResult.getResultCode(), importResult.getResultMsg());
    }

    return importResult;
  }

  /**
   * 刷新在线文档内容
   *
   * @param documentId 文档ID
   */
  public void refreshDocumentContent(String documentId) {
    String endpoint = serverProperties.getEndpoint();
    String apiKey = serverProperties.getApiKey();
    RestTemplate restTemplate = HttpUtil.getRestTemplate();

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(apiKey);
    HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);

    String url = endpoint + REFRESH_CONTENT_PATH.replace(":documentId", documentId);
    logger.info("开始刷新文档内容: documentId={}, url={}", documentId, url);

    DocumentRefreshResult importResult =
      restTemplate.postForObject(url, httpEntity, DocumentRefreshResult.class);

    if (logger.isDebugEnabled() && importResult != null) {
      logger.debug("文档内容刷新完成: documentId={}, success={}, message={}", documentId, importResult.getSuccess(),
        importResult.getMessage());
    }
  }
}
