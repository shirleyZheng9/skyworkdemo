package com.iwhalecloud.bote.doc.module.document.service.impl;

import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.module.base.service.impl.FileUploadHelper;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentAttachmentDTO;
import com.iwhalecloud.bote.doc.module.document.entity.AttachmentsEntity;
import com.iwhalecloud.bote.doc.module.document.mapper.AttachmentsMapper;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentAttachmentService;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.util.BeanUtil;
import com.iwhalecloud.bss.litchi.util.data.Base58;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文档附件service
 *
 * @author Aiqing
 * @since 2025/9/9
 */
@Service
@RequiredArgsConstructor
public class DocumentAttachmentServiceImpl implements IDocumentAttachmentService {

  private static final Logger logger = LoggerFactory.getLogger(DocumentAttachmentServiceImpl.class);

  /**
   * 文档附件路径的正则表达式模式
   * 匹配格式：/bote/dc/document/attachment/files/{documentId}/{fileName}
   */
  private static final Pattern ATTACHMENT_PATH_PATTERN = Pattern.compile(
    DocBaseConsts.DOCUMENT_ATTACHMENT_GET_PATH
      .replace("{0}", "([^/]+)")
      .replace("{1}", "([^/]+)")
  );

  private final BoteFileInfoMapper fileInfoMapper;
  private final AttachmentsMapper attachmentsMapper;
  private final FileUploadHelper fileUploadHelper;

  private static Long parseAttachmentId(String fileNameWithoutExt) {
    if (StringUtils.isBlank(fileNameWithoutExt)) {
      return null;
    }
    try {
      return Long.parseLong(new String(Base58.decode(fileNameWithoutExt), StandardCharsets.UTF_8));
    }
    catch (IllegalArgumentException e) {
      throw new BssException("无效的文件编码", e);
    }
  }

  @Override
  @Transactional
  public DocumentAttachmentDTO upload(File file, String documentId, Long userId) {
    FileInfoVO fileInfoVO = fileUploadHelper.uploadFile(file);
    return insert(fileInfoVO, documentId, userId);
  }

  @Override
  @Transactional
  public List<DocumentAttachmentDTO> save(List<FileInfoVO> fileInfoVOs, String documentId, Long userId) {

    if (CollectionUtils.isEmpty(fileInfoVOs)) {
      return null;
    }
    List<DocumentAttachmentDTO> result = new ArrayList<>();
    fileInfoVOs.forEach(fileInfoVO -> {
      DocumentAttachmentDTO attachmentDTO = insert(fileInfoVO, documentId, userId);
      result.add(attachmentDTO);
    });
    return result;
  }

  private DocumentAttachmentDTO insert(FileInfoVO fileInfoVO, String documentId, Long userId) {

    String extension = FilenameUtils.getExtension(fileInfoVO.getFileName());

    // 保存文档的关联关系
    AttachmentsEntity attachmentsEntity = new AttachmentsEntity();
    attachmentsEntity.setId(IDUtils.nextId());
    attachmentsEntity.setDocumentId(documentId);
    attachmentsEntity.setFileId(fileInfoVO.getFileId());
    attachmentsEntity.setFileName(fileInfoVO.getFileName());
    attachmentsEntity.setFileSize(fileInfoVO.getFileSize());
    attachmentsEntity.setFileExtension(extension);
    attachmentsEntity.setCreatorId(userId);
    attachmentsEntity.setUpdatorId(userId);
    attachmentsEntity.setStatusCd(DocBaseConsts.STATUS_CD_VALID);
    this.attachmentsMapper.insert(attachmentsEntity);

    DocumentAttachmentDTO attachmentDTO = BeanUtil.copy(fileInfoVO, DocumentAttachmentDTO.class);
    attachmentDTO.setAttachmentId(attachmentsEntity.getId());
    attachmentDTO.setDocumentId(attachmentsEntity.getDocumentId());
    // 构造访问URL
    String url = MessageFormat.format(DocBaseConsts.DOCUMENT_ATTACHMENT_GET_PATH, documentId,
      buildVisitFileName(attachmentsEntity.getId(), extension));
    attachmentDTO.setUrl(url);
    return attachmentDTO;
  }

  @Override
  public DocumentAttachmentDTO getAttachmentInfo(Long attachmentId) {
    AttachmentsEntity attachmentsEntity = attachmentsMapper.selectByPrimaryKey(attachmentId);
    if (attachmentsEntity == null) {
      return null;
    }
    Long fileId = attachmentsEntity.getFileId();
    FileInfoVO fileInfoVO = fileInfoMapper.getFileInfoById(fileId);
    if (fileInfoVO == null) {
      return null;
    }
    DocumentAttachmentDTO attachmentDTO = BeanUtil.copy(fileInfoVO, DocumentAttachmentDTO.class);
    attachmentDTO.setAttachmentId(attachmentsEntity.getId());
    attachmentDTO.setDocumentId(attachmentsEntity.getDocumentId());

    boolean picture = FileTypeUtil.isPicture(fileInfoVO.getFileType());
    attachmentDTO.setIsPicture(picture);

    // 构造访问URL
    String documentId = attachmentsEntity.getDocumentId();
    String extension = FilenameUtils.getExtension(fileInfoVO.getFileName());
    String url = MessageFormat.format(DocBaseConsts.DOCUMENT_ATTACHMENT_GET_PATH, documentId,
      buildVisitFileName(attachmentsEntity.getId(), extension));
    attachmentDTO.setUrl(url);
    attachmentDTO.setTenantId(attachmentsEntity.getTenantId());
    return attachmentDTO;
  }

  @Override
  public DocumentAttachmentDTO parseDocumentImageFile(String filePath) {
    // 使用正则表达式匹配 DocBaseConsts.DOCUMENT_ATTACHMENT_GET_PATH 提取出documentId 和 attachmentId
    if (StringUtils.isBlank(filePath)) {
      return null;
    }
    try {
      Matcher matcher = ATTACHMENT_PATH_PATTERN.matcher(filePath);
      if (!matcher.matches()) {
        return null;
      }
      // 提取文档ID和文件名
      String documentId = matcher.group(1);
      String fileName = matcher.group(2);

      // 从文件名中解析附件ID
      Long attachmentId = resolveAttachmentId(fileName);
      if (attachmentId == null) {
        return null;
      }
      // 获取附件信息
      DocumentAttachmentDTO attachmentDTO = getAttachmentInfo(attachmentId);
      if (attachmentDTO == null) {
        return null;
      }
      // 验证文档ID是否匹配
      if (!Objects.equals(documentId, attachmentDTO.getDocumentId())) {
        return null;
      }
      return attachmentDTO;

    }
    catch (Exception e) {
      logger.error("解析文档附件信息失败，filePath:{}", filePath, e);
      // 解析失败时返回null, 不抛出错误
      return null;
    }
  }

  @Override
  public void fetchAttachmentFile(DocumentAttachmentDTO attachmentDTO, OutputStream outputStream) throws IOException {
    Long fileId = attachmentDTO.getFileId();
    if (fileId == null) {
      return;
    }
    // 从文件管理器获取文件输入流并复制到输出流
    try (InputStream inputStream = fileUploadHelper.downloadFileStream(attachmentDTO)) {
      IOUtils.copy(inputStream, outputStream);
      // 确保数据写入输出流
      outputStream.flush();
    }
  }

  private String buildVisitFileName(Long attachmentId, String extension) {
    String fileName = Base58.encode(String.valueOf(attachmentId).getBytes(StandardCharsets.UTF_8));
    if (StringUtils.isBlank(extension)) {
      return fileName;
    }
    return fileName + "." + extension;
  }

  @Override
  public Long resolveAttachmentId(String filePath) {
    String fileNameWithoutExt = FilenameUtils.removeExtension(filePath);
    return parseAttachmentId(fileNameWithoutExt);
  }
}
