package com.iwhalecloud.bote.doc.module.base.service.impl;

import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.DateUtil;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传辅助类
 *
 * @author chen.linfa
 * @since 2025-11-08
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class FileUploadHelper {
  private static final Logger logger = LoggerFactory.getLogger(FileUploadHelper.class);

  private final BoteFileInfoMapper fileInfoMapper;
  private final FileInfoManageMapper fileInfoManageMapper;
  private final IFileStoreService fileStoreService;

  /**
   * 上传文件
   *
   * @param file 问啊见
   * @return 文件信息
   */
  public FileInfoVO uploadFile(File file) {
    String originalFilename = file.getName();
    try (FileInputStream fis = new FileInputStream(file)) {
      UploadConfigVO config = createUploadConfig(file);
      FileInfoVO fileInfoVO = fileStoreService.uploadFile(fis, config);
      fileInfoVO.setIsPicture(FileTypeUtil.isPicture(config.getFileType()));
      return fileInfoVO;
    }
    catch (IOException e) {
      logger.error("Failed to upload page file: name={}, originalFileName={}, size={}", file.getName(),
        originalFilename, file.length(), e);
      throw new BssException(String.format("上传 %s 失败: %s", originalFilename, e.getMessage()), e);
    }
  }

  /**
   * 上传文件并保存文件关联信息
   *
   * @param file 文件
   * @return 文件信息
   */
  public FileInfoDTO uploadFileWithInfoSave(MultipartFile file, String fileName) {
    Long tenantId = TenantContextHolder.getTenantId();
    FileInfoVO fileInfoVO = this.uploadFile(file, fileName);
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    return saveFileInfo(fileInfoVO, fileName, tenantId, currentLoginUserId);
  }

  /**
   * 上传文件并保存文件关联信息
   *
   * @param inputStream 文件流
   * @param fileName 文件名称
   * @param fileSize 文件大小
   * @param tenantId 租户ID
   */
  public FileInfoDTO uploadFileWithInfoSave(InputStream inputStream,
    String fileName,
    Long fileSize,
    Long tenantId) {
    FileInfoVO fileInfoVO = this.uploadFile(inputStream, fileName, fileSize);
    Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
    return saveFileInfo(fileInfoVO, fileName, tenantId, currentLoginUserId);
  }

  /**
   * 更新文件名称
   *
   * @param fileInfoId 文档ID
   * @param fileName 文件名称
   */
  public void updateFileInfoName(Long fileInfoId, String fileName, Long tenantId) {
    FileInfoDTO fileInfo = fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
    if (fileInfo == null) {
      return;
    }
    fileInfoManageMapper.updateFileName(tenantId, fileInfoId, fileName);
    Long fileId = fileInfo.getFileId();
    if (fileId != null) {
      fileInfoMapper.updateFileName(fileId, fileName);
    }
  }

  /**
   * 更新文件信息表中的文件ID
   *
   * @param fileInfoId fileInfoId
   * @param fileId 文件ID
   * @param fileName 文件名称
   * @param updatorId 更新人
   * @param tenantId 租户ID
   */
  public void updateFileIdByFileInfoId(Long fileInfoId, Long fileId, String fileName, Long updatorId, Long tenantId) {
    fileInfoManageMapper.updateFileIdByFileInfoId(tenantId, fileInfoId, fileId, fileName, updatorId);
  }

  /**
   * 下载文件
   *
   * @param fileInfoVO 文件信息
   * @return 输入流
   */
  public InputStream downloadFileStream(FileInfoVO fileInfoVO) {
    return fileStoreService.downloadFileStream(fileInfoVO);
  }

  public FileInfoVO uploadFile(InputStream inputStream, String fileName, Long fileSize) {
    UploadConfigVO config = createUploadConfig(fileName, fileSize);
    return fileStoreService.uploadFile(inputStream, config);
  }
  private FileInfoDTO saveFileInfo(FileInfoVO fileInfo, String fileName, Long tenantId, Long optUserId) {
    return saveFileInfo(fileInfo.getFileId(), fileName, tenantId, optUserId);
  }

  /**
   * 上传文件
   *
   * @param file 文件
   * @return 文件信息
   */
  public FileInfoVO uploadFile(MultipartFile file, String fileName) {
    UploadConfigVO config = createUploadConfig(fileName, file.getSize());
    config.setOriginalFileName(fileName);
    try (InputStream inputStream = file.getInputStream()) {
      FileInfoVO fileInfoVO = fileStoreService.uploadFile(inputStream, config);
      fileInfoVO.setIsPicture(FileTypeUtil.isPicture(config.getFileType()));
      return fileInfoVO;
    }
    catch (IOException e) {
      logger.error("Failed to upload page file: name={}, fileName={}, size={}",
        file.getName(), fileName, file.getSize(), e);
      throw new BssException(String.format("上传 %s 失败: %s", fileName, e.getMessage()), e);
    }
  }

  private UploadConfigVO createUploadConfig(File file) throws IOException {
    String fileName = Optional.of(file.getName()).orElse("");
    return createUploadConfig(fileName, file.length());
  }

  private UploadConfigVO createUploadConfig(String fileName,
    Long fileSize) {
    Long tenantId = TenantContextHolder.getTenantId();
    UploadConfigVO config = new UploadConfigVO();
    config.setStoreType(FileStoreUtils.getDefaultStoreType());
    String currentMonth = DateUtil.format(LocalDateTime.now(), "yyyyMM");
    config.setSubFolder(tenantId + "/" + DocBaseConsts.UPLOAD_FILE_BASE_PATH + "/" + currentMonth);
    config.setOriginalFileName(StringUtils.trimToNull(fileName));
    config.setFileSize(fileSize);

    String fileExtension = StringUtils.substringAfterLast(fileName, ".");
    String fileType = StringUtils.isNotBlank(fileExtension) ? fileExtension.toLowerCase() : "unknown";
    config.setFileType(fileType);
    return config;
  }

  /**
   * 保存文件的业务关联信息
   *
   * @param fileId 文件ID
   * @param fileName 文件名
   * @param tenantId 租户ID
   * @param optUserId 操作用户ID
   * @return 文件信息
   */
  public FileInfoDTO saveFileInfo(Long fileId, String fileName, Long tenantId, Long optUserId) {
    FileInfoDTO dto = new FileInfoDTO();
    dto.setFileInfoId(IDUtils.nextId());
    dto.setFileId(fileId);
    dto.setFileName(fileName);
    dto.setTenantId(tenantId);
    dto.setBusiType(DocBaseConsts.FILE_BUSI_TYPE_DOCUMENT);
    dto.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    dto.setCreatedTime(new Date());
    dto.setCreatorId(optUserId);
    dto.setUpdatorId(optUserId);
    fileInfoManageMapper.insertFileInfo(dto);
    return dto;
  }
}
