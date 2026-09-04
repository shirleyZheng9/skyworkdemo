package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.cache.FileDownloadCache;
import com.iwhalecloud.bote.common.consts.SupportPreviewFileType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.FilePreviewConfig;
import com.iwhalecloud.bote.dto.base.DocumentPreviewDTO;
import com.iwhalecloud.bote.dto.base.FileDownloadToken;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.FilePreviewDTO;
import com.iwhalecloud.bote.dto.base.FilePreviewRequestParams;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.service.IFilePreviewService;
import com.iwhalecloud.bote.util.FileUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 文件预览服务实现类
 *
 * @author qian.sisheng
 * @since 2025-11-26
 */
@Service
@RequiredArgsConstructor
public class FilePreviewServiceImpl implements IFilePreviewService {
  private final Logger logger = LoggerFactory.getLogger(FilePreviewServiceImpl.class);

  private static final List<String> NEED_CONVERT_FILE_EXT = Arrays.asList("doc", "docx", "ppt", "pptx");

  private final FileInfoManageMapper fileInfoManageMapper;

  private final FileDownloadCache fileDownloadCache;

  private final FilePreviewConfig filePreviewConfig;

  private final IFileStoreService fileStoreService;

  @NonNull
  private static ResultVO<FilePreviewDTO> notSupportRes(String fileName, @Nullable Long fileSize) {
    FilePreviewDTO filePreviewDTO = new FilePreviewDTO();
    filePreviewDTO.setSupportPreview(false);
    filePreviewDTO.setFileName(fileName);
    filePreviewDTO.setFileSize(fileSize);
    return ResultVO.success(filePreviewDTO);
  }

  /**
   * 获取文件预览信息
   *
   * @param params 文件预览参数
   * @return 文件预览信息
   */
  @Override
  public ResultVO<FilePreviewDTO> getFilePreviewInfo(FilePreviewRequestParams params) {
    String fileUrl = params.getFileUrl();
    Long fileInfoId = params.getFileInfoId();
    // 优先使用文件URL
    if (StringUtils.isNotEmpty(fileUrl)) {
      try {
        return getFilePreviewInfoByFileUrl(fileUrl);
      }
      catch (Exception e) {
        logger.error("获取文件预览信息失败", e);
        if (fileInfoId == null) {
          return ResultVO.fail("获取文件预览信息失败, message" + e.getMessage());
        }
      }
    }
    return getFilePreviewByFileInfoId(fileInfoId, params.getTenantId());
  }

  /**
   * 通过文件信息ID获取文件预览信息
   *
   * @param fileInfoId 文件信息ID
   * @param tenantId 租户ID
   * @return 文件预览信息
   */
  private ResultVO<FilePreviewDTO> getFilePreviewByFileInfoId(@Nullable Long fileInfoId, Long tenantId) {
    FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
    if (fileInfo == null) {
      return ResultVO.fail("文档文件信息不存在");
    }
    Long fileSize = fileInfo.getFileSize();
    String fileName = fileInfo.getOriginalFileName();
    // 获取文件扩展名
    String extension = FilenameUtils.getExtension(fileName);
    // 检查预览功能是否启用
    ResultVO<FilePreviewDTO> result = checkFilePreviewResult(extension, fileName, fileSize);
    if (result != null) {
      return result;
    }
    DocumentPreviewDTO documentPreview = new DocumentPreviewDTO();
    documentPreview.setDocumentName(fileName);
    documentPreview.setDocumentId(String.valueOf(fileInfoId));
    return ResultVO.success(buildPreviewInfo(fileInfo.getFileId(), fileInfo.getFileSize(), extension, documentPreview));
  }

  /**
   * 通过URL获取文件预览信息
   *
   * @param fileUrl 文件访问Url
   * @return 文件预览信息
   */
  private ResultVO<FilePreviewDTO> getFilePreviewInfoByFileUrl(String fileUrl) {
    URI url = URI.create(fileUrl);
    AtomicReference<String> fileNameRef = new AtomicReference<>();
    AtomicReference<String> fileTypeRef = new AtomicReference<>();
    AtomicInteger fileSizeRef = new AtomicInteger();
    // 获取文件名称和文件类型
    HttpUtil.getRestTemplate().execute(url, HttpMethod.GET, null, response -> {
      // 获取文件名称
      fileNameRef.set(FileUtil.getFileName(response, url));
      // 获取文件类型
      fileTypeRef.set(FileUtil.getFileExtension(response.getHeaders().getContentType(), fileNameRef.get()));
      try (InputStream inputStream = response.getBody()) {
        fileSizeRef.set(inputStream.readAllBytes().length);
      }
      return null;
    });
    String fileName = fileNameRef.get();
    String fileType = fileTypeRef.get();
    int fileSize = fileSizeRef.get();
    // 检查预览功能是否启用
    ResultVO<FilePreviewDTO> result = checkFilePreviewResult(fileType, fileName, (long) fileSize);
    if (result != null) {
      return result;
    }
    String finalDownloadUrl = UriComponentsBuilder.fromUriString(fileUrl)
      .queryParam("fullfilename", URLEncoder.encode(fileName + "." + fileType, StandardCharsets.UTF_8))
      .queryParam("fileKey", "")
      .queryParam("revision", "").build().toUriString();
    String fileUrlBase64 = Base64.getEncoder().encodeToString(finalDownloadUrl.getBytes(StandardCharsets.UTF_8));
    String previewUrl = UriComponentsBuilder.fromUriString(filePreviewConfig.getKkFileViewUrl())
      .queryParam("title", "预览")
      .queryParam("url", fileUrlBase64)
      .queryParam("pdfDownloadDisable", true)
      .build()
      .toUriString();
    return ResultVO.success(createPreview(previewUrl, fileUrlBase64, fileType, fileName, (long) fileSize));
  }


  /**
   * 获取文档关联文件的预览地址
   *
   * @return 预览地址
   */
  @Override
  public ResultVO<FilePreviewDTO> getFilePreviewInfo(DocumentPreviewDTO documentPreview) {
    Long tenantId = documentPreview.getTenantId();
    Long fileInfoId = documentPreview.getFileInfoId();
    String documentName = documentPreview.getDocumentName();
    Long fileSize;
    Long fileId = documentPreview.getFileId();
    if (fileId != null) {
      FileInfoVO fileInfo = fileStoreService.getFileInfoById(fileId);
      if (fileInfo == null) {
        throw new BssException("历史版本文件不存在或已删除");
      }
      fileSize = fileInfo.getFileSize();
    }
    else {
      FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
      if (fileInfo == null) {
        return ResultVO.fail("文档文件信息不存在");
      }
      fileSize = fileInfo.getFileSize();
      fileId = fileInfo.getFileId();
    }
    // 检查文件大小
    String extension = FilenameUtils.getExtension(documentPreview.getDocumentName());
    // 检查预览功能是否启用
    ResultVO<FilePreviewDTO> result = checkFilePreviewResult(extension, documentName, fileSize);
    if (result != null) {
      return result;
    }
    FilePreviewDTO documentFilePreviewDTO = buildPreviewInfo(fileId, fileSize, extension, documentPreview);
    return ResultVO.success(documentFilePreviewDTO);
  }

  /**
   * 构建文档预览信息
   */
  private FilePreviewDTO buildPreviewInfo(Long fileId, Long fileSize, String extension, DocumentPreviewDTO documentPreview) {
    String fileName = documentPreview.getDocumentName();
    // 生成临时访问token
    FileDownloadToken downloadToken = fileDownloadCache.generateFileDownloadToken(SessionUtil.getLoginInfo().getUserId(),
      documentPreview.getDocumentId(), fileId, fileName, fileSize, (str) -> {
        return filePreviewConfig.getFileDownloadUrl().replace("{token}", str);
      });
    // 构建预览URL
    String fileDownloadUrl = downloadToken.getDownloadUrl();
    String finalDownloadUrl = UriComponentsBuilder.fromUriString(fileDownloadUrl)
      .queryParam("fullfilename", URLEncoder.encode(fileName, StandardCharsets.UTF_8))
      .queryParam("fileKey", documentPreview.getDocumentId())
      .queryParam("revision", documentPreview.getRevision())
      .build()
      .toUriString();

    String fileUrlBase64 = Base64.getEncoder().encodeToString(finalDownloadUrl.getBytes(StandardCharsets.UTF_8));
    String userName = documentPreview.getUserName();
    boolean enableWatermark = Boolean.TRUE.equals(filePreviewConfig.getEnableWatermark());
    String previewUrl = UriComponentsBuilder.fromUriString(filePreviewConfig.getKkFileViewUrl())
      .queryParam("title", "预览")
      .queryParam("url", fileUrlBase64)
      .queryParam("watermarkTxt", enableWatermark && StringUtils.isNotEmpty(userName) ? URLEncoder.encode(userName, StandardCharsets.UTF_8) : null)
      .queryParam("pdfDownloadDisable", true).build().toUriString();
    return createPreview(previewUrl, fileUrlBase64, extension, fileName, fileSize);
  }

  /**
   * 检查预览功能是否启用
   */
  @Nullable
  private ResultVO<FilePreviewDTO> checkFilePreviewResult(String extension, String fileName, @Nullable Long fileSize) {
    boolean sizeAvailable = fileSize != null && filePreviewConfig.isPreviewFileSizeValid(fileSize);
    boolean previewConfigEnabled = Boolean.TRUE.equals(filePreviewConfig.getEnabled());
    boolean supportPreview = SupportPreviewFileType.supportPreview(extension);
    if (!previewConfigEnabled || !supportPreview || !sizeAvailable) {
      return notSupportRes(fileName, fileSize);
    }
    return null;
  }

  /**
   * 构建预览DTO对象
   *
   * @param previewUrl 预览URL
   * @param fileUrl 文件URL
   * @param fileType 文件类型
   * @param fileName 文件名
   * @param fileSize 文件大小
   * @return 预览DTO对象
   */
  private FilePreviewDTO createPreview(String previewUrl, String fileUrl, String fileType, String fileName, Long fileSize) {
    FilePreviewDTO documentFilePreviewDTO = new FilePreviewDTO();
    documentFilePreviewDTO.setPreviewUrl(previewUrl);
    documentFilePreviewDTO.setFileUrl(fileUrl);
    documentFilePreviewDTO.setNeedConvert(NEED_CONVERT_FILE_EXT.contains(fileType.toLowerCase()));
    documentFilePreviewDTO.setSupportPreview(true);
    documentFilePreviewDTO.setFileName(fileName);
    documentFilePreviewDTO.setFileSize(fileSize);
    return documentFilePreviewDTO;
  }
}
