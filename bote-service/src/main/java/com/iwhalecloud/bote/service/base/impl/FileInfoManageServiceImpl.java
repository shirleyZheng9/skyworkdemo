package com.iwhalecloud.bote.service.base.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.OcrUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.IDocumentManageService;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.SyncFileInfoRequest;
import com.iwhalecloud.bote.dto.base.query.FileInfoQueryParams;
import com.iwhalecloud.bote.dto.knowledge.query.UpdateFileParams;
import com.iwhalecloud.bote.dto.knowledge.query.UploadFileParams;
import com.iwhalecloud.bote.mapper.base.BoteFileInfoMapper;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.service.base.IFileInfoManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.sequence.IDUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件信息管理服务实现
 *
 * @author auto
 * @since 2024-09-24
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class FileInfoManageServiceImpl implements IFileInfoManageService {

  private final Logger logger = LoggerFactory.getLogger(FileInfoManageServiceImpl.class);

  private final FileInfoManageMapper fileInfoManageMapper;

  private final BoteFileInfoMapper boteFileInfoMapper;

  private final IFileStoreService fileStoreService;

  private final IDocumentManageService documentManageService;

  @Override
  @Nullable
  public FileInfoDTO findFileInfo(Long tenantId, Long fileInfoId) {
    return fileInfoManageMapper.getFileInfo(tenantId, fileInfoId);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteFileInfo(Long tenantId, Long fileInfoId) {
    FileInfoDTO fileInfo = findFileInfo(tenantId, fileInfoId);
    Assert.notNull(fileInfo, "查询不到有效的文件");
    // 存在在用关系，不可删除
    String template = "\"fileInfoId\":\"" + fileInfoId + "\"";
    boolean exists = fileInfoManageMapper.existsRelatedData(fileInfo.getTenantId(), fileInfo.getFileInfoId(), fileInfo.getBusiType(), template);
    Assert.isTrue(!exists, "存在在用的关联配置，不可删除");

    fileInfoManageMapper.deleteFileInfo(fileInfo.getTenantId(), fileInfoId, SessionUtil.getLoginInfo().getUserId());
    fileStoreService.deleteFile(fileInfo.getFileId());
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> batchDeleteFileInfo(Long tenantId, List<Long> fileInfoIds) {
    if (CollectionUtils.isEmpty(fileInfoIds)) {
      return ResultVO.fail("文件信息ID列表不能为空");
    }
    // 查询所有文件信息
    List<FileInfoDTO> fileInfos = fileInfoIds.stream().map(p -> findFileInfo(tenantId, p)).filter(Objects::nonNull).collect(Collectors.toList());
    if (fileInfos.isEmpty()) {
      String invalidFileIds = fileInfoIds.stream().map(String::valueOf).collect(Collectors.joining("、"));
      return ResultVO.fail("未找到有效的文件信息，无效的文件ID: [" + invalidFileIds + "]");
    }

    // 按业务类型分组
    Map<String, List<FileInfoDTO>> fileInfoMap = fileInfos.stream().collect(Collectors.groupingBy(FileInfoDTO::getBusiType));

    // 检查每种业务类型下是否存在关联数据
    for (Map.Entry<String, List<FileInfoDTO>> entry : fileInfoMap.entrySet()) {
      String busiType = entry.getKey();
      List<Long> typeFileInfoIds = entry.getValue().stream().map(FileInfoDTO::getFileInfoId).collect(Collectors.toList());

      List<FileInfoDTO> relatedFiles = fileInfoManageMapper.batchExistsRelatedData(tenantId, typeFileInfoIds, busiType);

      if (!CollectionUtils.isEmpty(relatedFiles)) {
        String fileNames = relatedFiles.stream().map(FileInfoDTO::getFileName).collect(Collectors.joining("、"));
        return ResultVO.fail("文件 [" + fileNames + "] 存在在用的关联配置，不可删除");
      }
    }

    // 批量删除文件信息
    Long userId = SessionUtil.getLoginInfo().getUserId();
    for (FileInfoDTO fileInfo : fileInfos) {
      fileInfoManageMapper.deleteFileInfo(fileInfo.getTenantId(), fileInfo.getFileInfoId(), userId);
      fileStoreService.deleteFile(fileInfo.getFileId());
    }

    return ResultVO.success();
  }

  @Override
  public List<FileInfoDTO> queryFileInfoList(FileInfoQueryParams queryParams) {
    return fileInfoManageMapper.selectFileInfoList(queryParams);
  }

  @Override
  public PageInfo<FileInfoDTO> queryFileInfoPage(FileInfoQueryParams queryParams) {
    if (KnowledgeConsts.DOCUMENT_TYPE_STRUCT_DATA.equals(queryParams.getDocumentType())) {
      queryParams.setFileTypes(Arrays.asList("xlsx", "xls", "csv"));
    }
    RowBounds rowBounds = queryParams.buildRowBounds();
    //noinspection resource
    return fileInfoManageMapper.selectFileInfoPage(queryParams, rowBounds).toPageInfo();
  }

  @Override
  public ResultVO<FileInfoDTO> reUploadFile(MultipartFile file, Long fileInfoId, Long tenantId) {
    FileInfoDTO fileInfo = findFileInfo(tenantId, fileInfoId);
    Assert.notNull(fileInfo, "查询不到有效的文件信息");
    try {
      UploadConfigVO config = createUploadConfig(file, tenantId, fileInfo.getBusiType());
      ResultVO<Void> validateResult = validateDocument(config, fileInfo.getBusiType(), file);
      if (!validateResult.isSuccess()) {
        return new ResultVO<>(validateResult);
      }
      FileInfoVO newFileInfo = fileStoreService.uploadFile(file, config);
      // 如果是页面文件，则文件名保持不变
      if (BaseConsts.FILE_BUSI_TYPE_PAGE.equals(fileInfo.getBusiType())) {
        newFileInfo.setFileName(fileInfo.getFileName());
      }
      Long userId = SessionUtil.getLoginInfo().getUserId();
      fileInfoManageMapper.updateFileIdByFileInfoId(fileInfo.getTenantId(), fileInfo.getFileInfoId(), newFileInfo.getFileId(),
        newFileInfo.getFileName(), userId);
      if (BaseConsts.FILE_BUSI_TYPE_DOCUMENT.equals(fileInfo.getBusiType())) {
        // 重新上传文档，触发关联的知识库文档重新构建
        documentManageService.rebuildDocuments(fileInfo.getTenantId(), fileInfo.getFileInfoId(), newFileInfo.getFileId(), newFileInfo.getFileName());
      }
      return ResultVO.success(fileInfo);
    }
    catch (IOException | RuntimeException e) {
      logger.error("Failed to upload page file: name={}, originalFileName={}, size={}", file.getName(), file.getOriginalFilename(), file.getSize(),
        e);
      return ResultVO.fail("上传 " + file.getOriginalFilename() + " 失败: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResultVO<FileInfoDTO> uploadFile(MultipartFile file, UploadFileParams query) {
    try {
      UploadConfigVO config = createUploadConfig(file, query.getTenantId(), query.getBusiType());
      ResultVO<Void> validateResult = validateDocument(config, query.getBusiType(), file);
      if (!validateResult.isSuccess()) {
        return new ResultVO<>(validateResult);
      }
      FileInfoVO fileInfo = fileStoreService.uploadFile(file, config);
      fileInfo.setIsPicture(FileTypeUtil.isPicture(config.getFileType()));
      return ResultVO.success(saveFileInfo(fileInfo, query));
    }
    catch (IOException | RuntimeException e) {
      logger.error("Failed to upload file: name={}, originalFileName={}, size={}", file.getName(), file.getOriginalFilename(), file.getSize(), e);
      return ResultVO.fail("上传 " + file.getOriginalFilename() + " 失败: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public ResultVO<FileInfoDTO> updateFile(MultipartFile file, UpdateFileParams query) {
    Assert.notNull(query.getFileInfoId(), "文件信息ID不能为空");
    Assert.notNull(query.getTenantId(), "租户ID不能为空");

    // 查询现有文件信息
    FileInfoDTO existingFileInfo = findFileInfo(query.getTenantId(), query.getFileInfoId());
    Assert.notNull(existingFileInfo, "查询不到有效的文件信息");

    try {
      // 处理文件上传和文件名更新
      ResultVO<Void> fileProcessResult = processFileUpdate(file, query, existingFileInfo);
      if (!fileProcessResult.isSuccess()) {
        return new ResultVO<>(fileProcessResult);
      }

      // 更新其他可编辑字段
      updateFileInfoFields(query, existingFileInfo);

      // 保存更新到数据库
      saveFileInfoUpdate(existingFileInfo);

      // 处理文档重建
      if (ObjectUtils.isNotEmpty(file) && !file.isEmpty()) {
        handleDocumentRebuild(file, existingFileInfo);
      }

      return ResultVO.success(existingFileInfo);
    }
    catch (IOException | RuntimeException e) {
      logger.error("Failed to update file: fileInfoId={}, fileName={}", query.getFileInfoId(),
        file.getOriginalFilename(), e);
      return ResultVO.fail("更新文件失败: " + e.getMessage());
    }
  }

  /**
   * 处理文件上传和文件名更新
   *
   * @param file 上传的文件
   * @param query 更新参数
   * @param existingFileInfo 现有文件信息
   * @return 处理结果
   * @throws IOException IO异常
   */
  private ResultVO<Void> processFileUpdate(MultipartFile file, UpdateFileParams query, FileInfoDTO existingFileInfo) throws IOException {
    if (ObjectUtils.isNotEmpty(file) && !file.isEmpty()) {
      return processFileUpload(file, query, existingFileInfo);
    }
    else if (StringUtils.isNotEmpty(query.getFileName())) {
      // 如果没有上传新文件但有新文件名，则只更新文件名
      existingFileInfo.setFileName(query.getFileName());
    }
    return ResultVO.success();
  }

  /**
   * 处理文件上传
   *
   * @param file 上传的文件
   * @param query 更新参数
   * @param existingFileInfo 现有文件信息
   * @return 处理结果
   * @throws IOException IO异常
   */
  private ResultVO<Void> processFileUpload(MultipartFile file, UpdateFileParams query, FileInfoDTO existingFileInfo) throws IOException {
    String busiType = StringUtils.isNotEmpty(query.getBusiType()) ? query.getBusiType() : existingFileInfo.getBusiType();

    UploadConfigVO config = createUploadConfig(file, query.getTenantId(), busiType);
    ResultVO<Void> validateResult = validateDocument(config, busiType, file);
    if (!validateResult.isSuccess()) {
      return validateResult;
    }

    FileInfoVO newFileInfo = fileStoreService.uploadFile(file, config);
    newFileInfo.setIsPicture(FileTypeUtil.isPicture(config.getFileType()));

    // 更新文件ID和文件名
    existingFileInfo.setFileId(newFileInfo.getFileId());
    if (StringUtils.isNotEmpty(query.getFileName())) {
      existingFileInfo.setFileName(query.getFileName());
    }
    else {
      existingFileInfo.setFileName(newFileInfo.getFileName());
    }

    return ResultVO.success();
  }

  /**
   * 更新文件信息的其他可编辑字段
   *
   * @param query 更新参数
   * @param existingFileInfo 现有文件信息
   */
  private void updateFileInfoFields(UpdateFileParams query, FileInfoDTO existingFileInfo) {
    if (query.getCatalogItemId() != null) {
      existingFileInfo.setCatalogItemId(query.getCatalogItemId());
    }
    if (StringUtils.isNotEmpty(query.getBusiType())) {
      existingFileInfo.setBusiType(query.getBusiType());
    }
    if (StringUtils.isNotEmpty(query.getBusiSubType())) {
      existingFileInfo.setBusiSubType(query.getBusiSubType());
    }
    if (StringUtils.isNotEmpty(query.getFileDesc())) {
      existingFileInfo.setRemark(query.getFileDesc());
    }
    if (StringUtils.isNotEmpty(query.getReqJson())) {
      existingFileInfo.setReqJson(query.getReqJson());
    }
  }

  /**
   * 保存文件信息更新到数据库
   *
   * @param existingFileInfo 文件信息
   */
  private void saveFileInfoUpdate(FileInfoDTO existingFileInfo) {
    Long updatorId = SessionUtil.getLoginInfo().getUserId();
    existingFileInfo.setUpdatorId(updatorId);
    fileInfoManageMapper.updateFileInfo(existingFileInfo);
  }

  /**
   * 处理文档重建
   *
   * @param file 上传的文件
   * @param existingFileInfo 文件信息
   */
  private void handleDocumentRebuild(MultipartFile file, FileInfoDTO existingFileInfo) {
    if (!file.isEmpty() && BaseConsts.FILE_BUSI_TYPE_DOCUMENT.equals(existingFileInfo.getBusiType())) {
      documentManageService.rebuildDocuments(existingFileInfo.getTenantId(), existingFileInfo.getFileInfoId(),
        existingFileInfo.getFileId(), existingFileInfo.getFileName());
    }
  }

  @Override
  public ResultVO<Object> ocrFile(MultipartFile file) {
    Path tempFile = null;
    try {
      tempFile = Files.createTempFile("bote-ocr-", file.getOriginalFilename());
      file.transferTo(tempFile.toFile());
      return ResultVO.success(OcrUtil.identifyWords(tempFile.toFile()));
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
  }

  private UploadConfigVO createUploadConfig(MultipartFile file, Long tenantId, String busiType) throws IOException {
    UploadConfigVO config = new UploadConfigVO();
    config.setStoreType(FileStoreUtils.getDefaultStoreType());
    if (BaseConsts.FILE_BUSI_TYPE_EVAL.equals(busiType)) {
      // 评测数据，存储路径特殊处理
      config.setSubFolder(BaseConsts.FINETUNE_FILE_PATH + tenantId);
    }
    else {
      config.setSubFolder(tenantId + "/" + busiType);
    }
    config.setOriginalFileName(StringUtils.trimToNull(file.getOriginalFilename()));
    config.setFileSize(file.getSize());
    config.setFileType(FileTypeUtil.getType(file));
    return config;
  }

  private ResultVO<Void> validateDocument(UploadConfigVO config, String busiType, MultipartFile file) {
    if (file.isEmpty()) {
      return BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toResult(config.getOriginalFileName());
    }
    if (BaseConsts.FILE_BUSI_TYPE_DOCUMENT.equals(busiType)) {
      String fileTypes = SystemParameter.ALLOW_UPLOAD_FILE_TYPE.getValueFromDb();
      String[] allowFileTypes = fileTypes.split(",");
      if (!ArrayUtils.contains(allowFileTypes, config.getFileType())) {
        return BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toResult(config.getFileType());
      }
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<FileInfoDTO> syncFileInfo(SyncFileInfoRequest request) {
    try {
      // 参数验证
      Assert.notNull(request.getFileId(), "文件ID不能为空");
      Assert.notNull(request.getTenantId(), "租户ID不能为空");
      Assert.hasText(request.getFileName(), "文件名称不能为空");
      Assert.hasText(request.getStoreType(), "存储类型不能为空");
      Assert.hasText(request.getFilePathInServer(), "服务器文件路径不能为空");
      Assert.hasText(request.getFileSize(), "文件大小不能为空");

      // 从文件名提取文件类型
      String fileType = FilenameUtils.getExtension(request.getFileName());
      if (StringUtils.isNotEmpty(fileType)) {
        fileType = fileType.toLowerCase();
      }

      // 转换文件大小为 Long
      long fileSizeLong;
      try {
        fileSizeLong = Long.parseLong(request.getFileSize());
      }
      catch (NumberFormatException e) {
        return ResultVO.fail("文件大小格式错误: " + request.getFileSize());
      }

      // 1. 同步 bt_file 表
      syncBtFile(request, fileType, fileSizeLong);

      // 2. 同步 bt_file_info 表
      FileInfoDTO fileInfoDTO = syncBtFileInfo(request);

      return ResultVO.success(fileInfoDTO);
    }
    catch (Exception e) {
      logger.error("同步文件信息失败", e);
      return ResultVO.fail("同步文件信息失败: " + e.getMessage());
    }
  }

  @Override
  public void syncFileInfoById(Long tenantId, Long fileInfoId, Long fileId, String fileName) {
    Long userId = SessionUtil.getOptionalUserId();
    fileInfoManageMapper.updateFileIdByFileInfoId(tenantId, fileInfoId, fileId, fileName, userId);
  }

  @Override
  @Transactional
  public FileInfoDTO createTenantRefFileInfo(Long tenantId, Long fileId, String fileName, String busiType) {
    Assert.notNull(tenantId, "租户ID不能为空");
    Assert.notNull(fileId, "文件ID不能为空");
    Assert.hasText(fileName, "文件名称不能为空");
    Assert.hasText(busiType, "业务类型不能为空");
    Long fileInfoId = IDUtils.nextId();
    Long userId = SessionUtil.getLoginInfo().getUserId();
    FileInfoDTO dto = new FileInfoDTO();
    dto.setFileInfoId(fileInfoId);
    dto.setFileId(fileId);
    dto.setFileName(fileName);
    dto.setTenantId(tenantId);
    dto.setBusiType(busiType);
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatorId(userId);
    dto.setUpdatorId(userId);
    fileInfoManageMapper.insertFileInfo(dto);
    return dto;
  }

  @Override
  @Transactional
  public void softDeleteFileInfoRecordOnly(Long tenantId, Long fileInfoId) {
    FileInfoDTO fileInfo = findFileInfo(tenantId, fileInfoId);
    if (fileInfo == null) {
      return;
    }
    fileInfoManageMapper.deleteFileInfo(tenantId, fileInfoId, SessionUtil.getLoginInfo().getUserId());
  }

  /**
   * 同步 bt_file 表
   */
  private void syncBtFile(SyncFileInfoRequest request, String fileType, Long fileSize) {
    // 检查 bt_file 表中是否存在该 fileId
    FileInfoVO existingFile = boteFileInfoMapper.getFileInfoById(request.getFileId());

    FileInfoVO fileInfoVO = new FileInfoVO();
    fileInfoVO.setFileId(request.getFileId());
    fileInfoVO.setStoreType(request.getStoreType());
    fileInfoVO.setFilePathInServer(request.getFilePathInServer());
    fileInfoVO.setFileName(request.getFileName());
    fileInfoVO.setFileSize(fileSize);
    fileInfoVO.setFileType(fileType);
    fileInfoVO.setStatusCd(BaseConsts.STATUS_CD_VALID);

    if (existingFile == null) {
      // 不存在，插入新记录
      fileInfoVO.setAppId(null); // bot_id 可以为空
      boteFileInfoMapper.insertFileInfo(fileInfoVO);
    }
    else {
      // 存在，更新记录
      boteFileInfoMapper.updateFileInfo(fileInfoVO);
    }
  }

  /**
   * 同步 bt_file_info 表
   */
  private FileInfoDTO syncBtFileInfo(SyncFileInfoRequest request) {
    // 检查 bt_file_info 表中是否存在关联记录
    FileInfoDTO existingFileInfo = fileInfoManageMapper.getFileInfoByFileIdAndTenantId(
      request.getFileId(), request.getTenantId());

    FileInfoDTO fileInfoDTO = new FileInfoDTO();
    fileInfoDTO.setFileId(request.getFileId());
    fileInfoDTO.setFileName(request.getFileName());
    fileInfoDTO.setTenantId(request.getTenantId());
    fileInfoDTO.setStatusCd(BaseConsts.STATUS_CD_VALID);
    fileInfoDTO.setStatusTime(new Date());

    Long userId = SessionUtil.getLoginInfo().getUserId();

    if (existingFileInfo == null) {
      // 不存在，插入新记录
      fileInfoDTO.setFileInfoId(Sequences.FILE_INFO_ID.next());
      fileInfoDTO.setCreatedTime(new Date());
      fileInfoDTO.setCreatorId(userId);
      fileInfoDTO.setUpdatorId(userId);
      if (StringUtils.isNotEmpty(request.getBusiType())) {
        fileInfoDTO.setBusiType(request.getBusiType());
      }
      fileInfoManageMapper.insertFileInfo(fileInfoDTO);
    }
    else {
      // 存在，更新记录
      fileInfoDTO.setFileInfoId(existingFileInfo.getFileInfoId());
      fileInfoDTO.setUpdatorId(userId);
      // 只有文件名不同时才更新
      if (!Objects.equals(existingFileInfo.getFileName(), request.getFileName())) {
        fileInfoDTO.putFieldUpdateFlag("fileName", true);
        fileInfoManageMapper.updateFileInfo(fileInfoDTO);
      }
      // 返回更新后的完整信息
      fileInfoDTO = fileInfoManageMapper.getFileInfo(request.getTenantId(), fileInfoDTO.getFileInfoId());
    }

    return fileInfoDTO;
  }

  private FileInfoDTO saveFileInfo(FileInfoVO fileInfo, UploadFileParams query) {
    FileInfoDTO dto = new FileInfoDTO();
    dto.setFileInfoId(Sequences.FILE_INFO_ID.next());
    dto.setFileId(fileInfo.getFileId());
    dto.setFileName(StringUtils.isNotEmpty(query.getFileName()) ? query.getFileName() : fileInfo.getFileName());
    dto.setTenantId(query.getTenantId());
    dto.setBusiType(query.getBusiType());
    dto.setBusiSubType(query.getBusiSubType());
    dto.setRemark(query.getFileDesc());
    dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    dto.setCreatedTime(new Date());
    dto.setCatalogItemId(query.getCatalogItemId());
    dto.setCreatorId(SessionUtil.getLoginInfo().getUserId());
    dto.setUpdatorId(SessionUtil.getLoginInfo().getUserId());
    dto.setReqJson(query.getReqJson());
    fileInfoManageMapper.insertFileInfo(dto);
    return dto;
  }
}
