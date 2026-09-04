package com.iwhalecloud.bote.doc.module.base.service.impl;

import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.FilePreviewConfig;
import com.iwhalecloud.bote.doc.cache.FileChunkUploadCache;
import com.iwhalecloud.bote.doc.cache.FolderUploadCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.support.tree.DefaultTreeBuildFactory;
import com.iwhalecloud.bote.doc.common.support.tree.NodeSortHelper;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.common.utils.DcIdUtils;
import com.iwhalecloud.bote.doc.consts.ContentSourceEnum;
import com.iwhalecloud.bote.doc.consts.DocumentTypeEnum;
import com.iwhalecloud.bote.doc.module.base.dto.BaseChunkRequest;
import com.iwhalecloud.bote.doc.module.base.dto.FileChunkInfo;
import com.iwhalecloud.bote.doc.module.base.dto.FolderStructureCreateRequest;
import com.iwhalecloud.bote.doc.module.base.dto.FolderUploadCacheDTO;
import com.iwhalecloud.bote.doc.module.base.dto.MergeChunkRequest;
import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.base.service.IFileUploadService;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentTreeDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentFileLogDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentCreateRequestDTO;
import com.iwhalecloud.bote.doc.module.document.service.DocumentChangEventPublisher;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentFileLogService;
import com.iwhalecloud.bote.doc.module.document.service.IDocumentService;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.util.FileStoreUtils;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务实现
 *
 * @author yangran
 * @since 2025-08-25
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class FileUploadServiceImpl implements IFileUploadService {

  private final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

  //@formatter:off
  private final FileUploadHelper fileUploadHelper;
  private final IDocumentService documentService;
  private final IFileStoreService fileStoreService;
  private final FileChunkUploadCache fileChunkUploadCache;
  private final FolderUploadCache folderUploadCache;
  private final FilePreviewConfig documentFilePreviewConfig;
  private final DocumentNodeService documentNodeService;
  private final DocumentChangEventPublisher documentChangEventPublisher;
  private final IDocumentFileLogService documentFileLogService;

  //@formatter:on
  @Override
  @Transactional
  public ResultVO<DcDocumentDTO> uploadFile(MultipartFile file, UnifiedUploadRequest request) {

    if (!documentFilePreviewConfig.isFileSizeValid(file.getSize())) {
      return ResultVO.fail("文件大小超出上传限制，文件限制大小" + FileUtils.byteCountToDisplaySize(documentFilePreviewConfig.getMaxFileSize()));
    }
    if (StringUtils.isNotBlank(request.getDocumentId())) {
      DcDocumentDTO dcDocumentDTO = this.overrideDocumentFile(file, request.getDocumentId(), request.getOriginalFileName());
      return ResultVO.success(dcDocumentDTO);
    }
    // 上传到文件服务器中
    FileInfoDTO fileInfoDTO = saveFile(file, request.getOriginalFileName());
    DcDocumentDTO documentDTO = saveDocument(request, fileInfoDTO);
    return ResultVO.success(documentDTO);
  }

  private DcDocumentDTO overrideDocumentFile(MultipartFile file, String documentId, String fileName) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (!Objects.equals(documentDTO.getContentSource(), ContentSourceEnum.UPLOAD.getCode())) {
      throw new BssException("非上传的文件文档，不能覆盖上传");
    }
    Long fileInfoId = documentDTO.getFileInfoId();
    Assert.notNull(fileInfoId, "文件不存在");
    // 更新文档名
    String documentType = DcIdUtils.determineDocumentType(fileName);
    documentService.updateUploadDocumentNameWhenReUp(documentId, fileName, userId, documentType);
    // 上传文件
    FileInfoVO fileInfoVO = fileUploadHelper.uploadFile(file, fileName);

    Long tenantId = TenantContextHolder.getRequiredTenantId();
    // 更新fileInfo 中关联的fileId
    fileUploadHelper.updateFileIdByFileInfoId(fileInfoId, fileInfoVO.getFileId(), fileName, userId, tenantId);
    DcDocumentDTO latest = documentService.findByDocumentId(documentId);
    if (latest != null && latest.getRevision() != null && fileInfoVO.getFileId() != null) {
      documentFileLogService.append(documentId, fileInfoVO.getFileId(), latest.getRevision(), userId, tenantId);
    }
    String oldDocumentName = null;
    if (!fileName.equals(documentDTO.getDocumentName())) {
      oldDocumentName = documentDTO.getDocumentName();
    }
    // 触发知识构建
    documentChangEventPublisher.publishReUploadEvent(documentDTO.getLibraryId(), documentId, oldDocumentName, userId);
    return documentDTO;
  }

  @Override
  @Transactional
  public ResultVO<Boolean> uploadChunk(MultipartFile chunkFile, UnifiedUploadRequest request) {

    if (!documentFilePreviewConfig.isFileSizeValid(request.getOriginalFileSize())) {
      return ResultVO.fail("文件大小超出上传限制");
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = TenantContextHolder.getTenantId();
    // 检查分片是否已存在
    if (fileChunkUploadCache.hasChunk(tenantId, userId, request.getFileHash(), request.getChunkIndex())) {
      return ResultVO.success(true);
    }
    // 构建切片存储路径
    String chunkFilePath = String.format("/chunks/%s/%s_%d", userId, request.getFileHash(), request.getChunkIndex());
    // 验证分片大小
    if (!documentFilePreviewConfig.isChunkFileSizeValid(chunkFile.getSize())) { // 5MB限制
      return ResultVO.fail("分片文件过大，单个分片不能超过5MB");
    }
    // 上传切片到文件服务器
    UploadConfigVO config = createChunkUploadConfig(request, chunkFilePath);
    FileInfoVO fileInfoVO;
    try (InputStream inputStream = chunkFile.getInputStream()) {
      fileInfoVO = fileStoreService.uploadFile(inputStream, config);
    }
    catch (IOException e) {
      throw new BssException(String.format("上传 %s 失败: %s", chunkFile.getName(), e.getMessage()), e);
    }
    // 获取文件服务器返回的fileId和文件路径
    Long fileId = fileInfoVO.getFileId();
    String serverFilePath = fileInfoVO.getFilePathInServer();
    // 保存分片信息到缓存
    FileChunkInfo fileChunkInfo = new FileChunkInfo();
    fileChunkInfo.setFileId(fileId);
    fileChunkInfo.setChunkPath(chunkFilePath);
    fileChunkInfo.setFilePath(serverFilePath);
    fileChunkInfo.setChunkIndex(request.getChunkIndex());
    fileChunkInfo.setChunkSize(chunkFile.getSize());
    fileChunkInfo.setFileHash(request.getFileHash());
    // 保存到缓存，设置1小时过期
    fileChunkUploadCache.saveChunkInfoWithDetails(tenantId, userId, fileChunkInfo, 1);
    return ResultVO.success(true);
  }

  @Override
  public ResultVO<Map<String, Object>> checkFileStatus(BaseChunkRequest request) {
    String fileHash = request.getFileHash();
    try {
      // 获取原始文件名并进行文件类型校验
      String originalFileName = request.getFileName();

      // 从原始文件名提取文件类型并校验
      String fileType = StringUtils.substringAfterLast(originalFileName, ".");
      if (StringUtils.isBlank(fileType)) {
        return ResultVO.fail("无法识别文件类型，请确保文件名包含扩展名");
      }

      ResultVO<Void> validateResult = checkFileType(fileType.toLowerCase());
      if (!validateResult.isSuccess()) {
        return new ResultVO<>(validateResult);
      }

      Long userId = SessionUtil.getLoginInfo().getUserId();
      if (userId == null) {
        return ResultVO.fail("用户未登录");
      }

      // 获取租户ID，确保租户级别隔离
      Long tenantId = TenantContextHolder.getTenantId();
      if (tenantId == null) {
        return ResultVO.fail("租户信息获取失败");
      }

      Map<String, Object> result = new HashMap<>();

      // 检查文件是否上传完成
      if (fileChunkUploadCache.isChunkBatchCompleted(tenantId, userId, fileHash)) {
        result.put("status", "completed");
        result.put("url", fileChunkUploadCache.getFileUrl(tenantId, userId, fileHash));
        result.put("fileHash", fileHash);
        return ResultVO.success(result);
      }

      // 获取已上传的分片索引
      List<Integer> uploadedChunkIndexes = fileChunkUploadCache.getUploadedChunkIndexes(tenantId, userId, fileHash);

      // 统一返回List<Integer>类型
      if (uploadedChunkIndexes.isEmpty()) {
        result.put("status", "pending");
        result.put("uploadedChunks", uploadedChunkIndexes);
        result.put("fileHash", fileHash);
        return ResultVO.success(result);
      }

      // 获取分片总数（如果缓存中有的话）
      long chunkCount = fileChunkUploadCache.getChunkCount(tenantId, userId, fileHash);

      result.put("status", "uploading");
      result.put("uploadedChunks", uploadedChunkIndexes);
      result.put("uploadedCount", uploadedChunkIndexes.size());
      result.put("totalCount", chunkCount);
      result.put("fileHash", fileHash);

      return ResultVO.success(result);
    }
    catch (Exception e) {
      logger.error("检查文件状态失败: fileHash={}, error={}", fileHash, e.getMessage(), e);
      return ResultVO.fail("检查文件状态失败: " + e.getMessage());
    }
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<DcDocumentDTO> mergeChunks(MergeChunkRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    // 获取租户ID，确保租户级别隔离
    Long tenantId = TenantContextHolder.getTenantId();
    // 验证分片完整性
    ResultVO<Void> validationResult = validateChunkIntegrity(request, tenantId, userId);
    if (!validationResult.isSuccess()) {
      return new ResultVO<>(validationResult);
    }
    // 合并分片文件
    File mergeFile = mergeChunkFiles(request, tenantId, userId);
    // 处理合并后的文件
    ResultVO<DcDocumentDTO> processResult = processMergedFile(request, mergeFile, userId);
    if (processResult.isSuccess()) {
      // 清理临时文件
      cleanupTempFile(mergeFile);
      fileChunkUploadCache.deleteChunkBatchStatus(tenantId, userId, request.getFileHash());
      logger.debug("清理文件上传缓存成功: tenantId={}, userId={}, fileHash={}", tenantId, userId, request.getFileHash());
    }
    return processResult;
  }

  @Override
  public ResultVO<Boolean> cleanupFailedUpload(String fileHash) {
    try {
      // 参数验证
      if (StringUtils.isBlank(fileHash)) {
        return ResultVO.fail("文件hash不能为空");
      }

      Long userId = SessionUtil.getLoginInfo().getUserId();
      if (userId == null) {
        return ResultVO.fail("用户未登录");
      }

      // 获取租户ID，确保租户级别隔离
      Long tenantId = TenantContextHolder.getTenantId();
      if (tenantId == null) {
        return ResultVO.fail("租户信息获取失败");
      }

      // 清理失败的上传
      cleanupFailedUploadInternal(tenantId, userId, fileHash);

      return ResultVO.success(true);
    }
    catch (Exception e) {
      logger.error("清理失败的上传失败: chunkBatchId={}, error={}", fileHash, e.getMessage(), e);
      return ResultVO.fail("清理失败的上传失败: " + e.getMessage());
    }
  }

  @Override
  public ResultVO<FolderUploadCacheDTO> getFolderUploadProgress(String taskId) {
    FolderUploadCacheDTO status = folderUploadCache.getUploadStatus(taskId);
    if (status == null) {
      return ResultVO.fail("上传任务不存在");
    }
    return ResultVO.success(status);
  }

  @Override
  public ResultVO<Boolean> pauseFolderUpload(String taskId) {
    folderUploadCache.pauseUploadTask(taskId, 24);
    logger.trace("暂停文件夹上传任务: taskId={}", taskId);
    return ResultVO.success(true);

  }

  @Override
  public ResultVO<Boolean> resumeFolderUpload(String taskId) {
    folderUploadCache.resumeUploadTask(taskId, 24);
    logger.trace("恢复文件夹上传任务: taskId={}", taskId);
    return ResultVO.success(true);

  }

  @Override
  public ResultVO<Boolean> cancelFolderUpload(String taskId) {
    folderUploadCache.cancelUploadTask(taskId, 24);
    logger.trace("取消文件夹上传任务: taskId={}", taskId);
    return ResultVO.success(true);
  }

  @Override
  @Transactional
  @SuppressWarnings("PMD.GuardLogStatement")
  public ResultVO<DcDocumentTreeDTO> createFolderStructure(FolderStructureCreateRequest request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = TenantContextHolder.getTenantId();
    // 获取文件信息：支持两种方式
    List<FolderStructureCreateRequest.UploadedFileInfo> uploadedFiles = getUploadedFilesFromCache(
      request.getFolderUploadTaskId());
    logger.debug("从缓存中获取文件夹文件信息: taskId={}, fileCount={}", request.getFolderUploadTaskId(), uploadedFiles.size());
    if (uploadedFiles.isEmpty()) {
      return ResultVO.fail("获取文件列表为空，创建文件夹文档失败，请重试");
    }
    // 文件夹批量上传期间跳过单次创建节点触发的库元素重算，流程结束时统一重算一次，避免 bt_resource_element 死锁
    documentService.setSkipLibraryComputeForFolderUpload(true);
    try {
      // 1. 创建根文件夹节点
      DcDocumentDTO rootFolder = createRootFolder(request, userId, tenantId);
      // 2. 按父目录分组文件，确保同一目录下的文件按原始顺序排列
      Map<String, List<FolderStructureCreateRequest.UploadedFileInfo>> filesByParent = groupFilesByParent(uploadedFiles,
        rootFolder.getDocumentId(), request);
      // 3. 批量创建文档节点
      batchCreateFileDocuments(filesByParent, request, userId, tenantId);
      // 4. 构建完整的树结构并返回
      DcDocumentTreeDTO treeStructure = buildFolderTreeStructure(rootFolder.getDocumentId());
      logger.trace("文件夹结构创建完成: folderName={}, fileCount={}", request.getFolderName(), uploadedFiles.size());
      return ResultVO.success(treeStructure);
    }
    finally {
      documentService.setSkipLibraryComputeForFolderUpload(false);
      documentService.recomputeLibraryElements(request.getLibraryId());
    }
  }

  @Override
  public List<DcDocumentDTO> queryDcDocumentDTOByRootId(String rootId) {
    // 1. 获取根节点下的所有子节点ID
    List<String> allNodeIds = getAllNodeIdsInTree(rootId);
    // 2. 批量查询所有节点信息
    return documentService.findBatchByDocumentId(allNodeIds);
  }

  /**
   * 校验文件类型是否允许上传
   */
  private ResultVO<Void> checkFileType(String fileType) {
    String fileTypes = BaseSystemParameter.ALLOW_UPLOAD_FILE_TYPE.getValueFromDb();
    String[] allowFileTypes = fileTypes.split(",");
    if (!ArrayUtils.contains(allowFileTypes, fileType)) {
      return BaseErrorConstant.NOT_ALLOWED_UPLOAD_FILE_TYPE.toResult(fileType);
    }
    return ResultVO.success();
  }

  /**
   * 缓存文件夹文件信息
   * 使用HSET存储文件信息，避免并发覆盖导致数据丢失
   *
   * @param taskId 任务ID
   * @param fileHash 文件哈希
   * @param fileInfoId 文件信息ID
   * @param request 上传请求
   */
  private void cacheFolderFileInfo(String taskId, String fileHash, Long fileInfoId, UnifiedUploadRequest request) {
    if (StringUtils.isBlank(taskId) || StringUtils.isBlank(fileHash) || fileInfoId == null) {
      logger.warn("缓存文件夹文件信息参数不完整: taskId={}, fileHash={}, fileInfoId={}", taskId, fileHash, fileInfoId);
      return;
    }

    // 检查文件信息是否已存在
    if (folderUploadCache.hasFileInfo(taskId, fileHash)) {
      logger.debug("文件信息已存在，跳过: taskId={}, fileHash={}", taskId, fileHash);
      return;
    }

    // 初始化任务缓存（如果不存在）
    initializeTaskCacheIfNeeded(taskId, request);

    // 创建文件信息对象
    FolderUploadCacheDTO.UploadedFileInfo fileInfoObj = createFileInfo(fileHash, fileInfoId, request);

    // 使用HSET直接保存文件信息，避免并发覆盖
    // HSET操作是原子的，即使没有锁也不会丢失数据
    try {
      folderUploadCache.saveFileInfoWithHash(taskId, fileHash, fileInfoObj, 2);
      logger.debug("缓存文件夹文件信息成功：taskId={}, fileHash={}, fileInfoId={}, relativePath={}", taskId, fileHash,
        fileInfoId, request.getRelativePath());
    }
    catch (Exception e) {
      logger.error("缓存文件夹文件信息失败：taskId={}, fileHash={}, fileInfoId={}, error={}", taskId, fileHash, fileInfoId,
        e.getMessage(), e);
      throw new BssException("缓存文件夹文件信息失败: " + e.getMessage(), e);
    }
  }

  /**
   * 初始化任务缓存（如果不存在）
   *
   * @param taskId 任务ID
   * @param request 上传请求
   */
  private void initializeTaskCacheIfNeeded(String taskId, UnifiedUploadRequest request) {
    FolderUploadCacheDTO cache = folderUploadCache.getFolderUploadCache(taskId);
    if (cache == null) {
      cache = new FolderUploadCacheDTO();
      cache.setTaskId(taskId);
      cache.setLibraryId(request.getLibraryId());
      cache.setParentId(request.getParentId());
      cache.setBusiType(request.getBusiType());
      cache.setConvertToOnline(request.getConvertToOnline());
      cache.setStatus("UPLOADING");
      cache.setTotalFiles(0);
      cache.setUploadedFiles(0);
      cache.setUploadedFileInfos(new ArrayList<>());
      folderUploadCache.saveFolderUploadCache(cache);
      logger.debug("初始化任务缓存: taskId={}", taskId);
    }
  }

  /**
   * 创建文件信息对象
   *
   * @param fileHash 文件哈希
   * @param fileInfoId 文件信息ID
   * @param request 上传请求
   * @return 文件信息对象
   */
  private FolderUploadCacheDTO.UploadedFileInfo createFileInfo(String fileHash, Long fileInfoId,
    UnifiedUploadRequest request) {
    FolderUploadCacheDTO.UploadedFileInfo fileInfoObj = new FolderUploadCacheDTO.UploadedFileInfo();
    fileInfoObj.setFileHash(fileHash);
    fileInfoObj.setFileInfoId(fileInfoId);
    fileInfoObj.setRelativePath(request.getRelativePath());
    fileInfoObj.setOriginalFileName(request.getOriginalFileName());
    fileInfoObj.setFileSize(request.getOriginalFileSize());
    fileInfoObj.setBusiType(request.getBusiType());
    fileInfoObj.setConvertToOnline(request.getConvertToOnline());
    fileInfoObj.setUploadTime(new Date());
    return fileInfoObj;
  }

  /**
   * 创建文档记录
   */
  private DcDocumentDTO createDocument(FileInfoDTO fileInfo, UnifiedUploadRequest request, Long userId, Long tenantId) {
    // 使用通用方法创建文档请求
    DocumentCreateRequestDTO createRequest = buildDocumentRequest(request.getLibraryId(), request.getParentId(),
      DcIdUtils.determineDocumentType(fileInfo.getFileName()), fileInfo.getFileName(), fileInfo.getFileInfoId(),
      request.getConvertToOnline(), tenantId, null);

    // 创建并返回文档
    return createDocumentAndReturn(createRequest, userId, "创建文档失败，未能获取文档信息");
  }

  /**
   * 确定父文件夹ID
   */
  private String determineParentId(String relativePath, String rootParentId, String libraryId, String rootFolderName) {
    if (StringUtils.isBlank(relativePath)) {
      return rootParentId;
    }

    // 解析相对路径，创建必要的文件夹结构
    String[] pathParts = relativePath.split("/");
    if (pathParts.length <= 1) {
      // 根目录下的文件
      return rootParentId;
    }

    // 获取文件夹路径（去掉文件名）
    String folderPath = StringUtils.substringBeforeLast(relativePath, "/");

    // 检查第一级文件夹是否和根文件夹名称相同，如果相同则跳过，避免重复创建
    String[] folderParts = folderPath.split("/");
    if (folderParts.length > 0 && StringUtils.isNotBlank(rootFolderName) && rootFolderName.equals(folderParts[0])) {
      logger.debug("跳过与根文件夹同名的子文件夹创建: folderName={}, rootFolderName={}", folderParts[0], rootFolderName);
      // 如果第一级就是根文件夹名，则使用根文件夹作为父目录
      if (folderParts.length == 1) {
        return rootParentId;
      }
      // 如果有多级，去掉第一级后重新处理
      String remainingPath = StringUtils.substringAfter(folderPath, "/");
      return createParentFolder(remainingPath, rootParentId, libraryId);
    }

    // 创建父文件夹
    return createParentFolder(folderPath, rootParentId, libraryId);
  }

  /**
   * 查找或创建父文件夹
   */
  private String createParentFolder(String folderPath, String rootParentId, String libraryId) {
    if (StringUtils.isBlank(folderPath)) {
      return rootParentId;
    }

    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = TenantContextHolder.getTenantId();

    String[] folderParts = folderPath.split("/");
    String currentParentId = rootParentId;

    for (String folderName : folderParts) {
      if (StringUtils.isBlank(folderName)) {
        continue;
      }

      // 先检查文件夹是否已存在
      DcDocumentDTO existingFolder = documentService.findByNameAndParent(folderName, currentParentId, libraryId, tenantId);
      if (existingFolder != null) {
        currentParentId = existingFolder.getDocumentId();
        logger.trace("文件夹已存在，复用: name={}, documentId={}", folderName, currentParentId);
        continue;
      }

      // 文件夹不存在，创建新的文件夹
      try {
        // 使用通用方法创建文件夹请求
        DocumentCreateRequestDTO createRequest = buildDocumentRequest(libraryId, currentParentId, DocumentTypeEnum.FOLDER.getCode(), folderName, null,
          // 文件夹没有文件信息
          DocBaseConsts.FALSE, // 文件夹不转换
          tenantId, null // 文件夹没有前置文档
        );

        // 使用新的统一服务方法创建文件夹
        currentParentId = documentService.createUploadDocumentNode(userId, createRequest);
        logger.trace("创建文件夹: name={}, documentId={}, libraryId={}", folderName, currentParentId, libraryId);

      }
      catch (Exception e) {
        // 创建失败，直接抛出异常
        logger.error("创建文件夹失败: name={}, error={}", folderName, e.getMessage());
        throw new BssException("创建文件夹失败: " + folderName, e);
      }
    }

    return currentParentId;
  }

  private void deleteChunkFile(List<FileChunkInfo> chunkDetails) {
    // 删除文件服务器中的分片文件
    for (FileChunkInfo chunkDetail : chunkDetails) {
      try {
        fileStoreService.deleteFile(chunkDetail.getFileId());
      }
      catch (Exception e) {
        logger.warn("删除分片文件失败: fileId={}, error={}", chunkDetail.getFileId(), e.getMessage());
      }
    }
  }

  /**
   * 创建分片上传配置
   */
  private UploadConfigVO createChunkUploadConfig(UnifiedUploadRequest request, String chunkPath) {
    UploadConfigVO config = new UploadConfigVO();
    config.setStoreType(FileStoreUtils.getDefaultStoreType());
    config.setSubFolder(chunkPath.substring(0, chunkPath.lastIndexOf("/")));
    config.setOriginalFileName(request.getOriginalFileName() + ".chunk." + request.getChunkIndex());
    config.setFileSize(request.getTotalChunks() != null ? (request.getTotalChunks() * 5 * 1024 * 1024L) : 0L);
    config.setFileType("chunk");
    return config;
  }

  /**
   * 验证分片完整性
   */
  private ResultVO<Void> validateChunkIntegrity(MergeChunkRequest request, Long tenantId, Long userId) {
    int totalChunks = request.getTotalChunks();

    // 检查分片数量是否正确
    long uploadedChunkCount = fileChunkUploadCache.getChunkCount(tenantId, userId, request.getFileHash());
    if (uploadedChunkCount != totalChunks) {
      return ResultVO.fail("文件缺失，请重新上传");
    }

    // 获取所有分片详细信息
    List<FileChunkInfo> chunkDetails = fileChunkUploadCache.getAllChunkDetails(tenantId, userId, request.getFileHash());
    if (chunkDetails.size() != totalChunks) {
      return ResultVO.fail("分片信息不完整，请重新上传");
    }

    return ResultVO.success();
  }

  /**
   * 合并分片文件 - 并发优化版本
   */
  private File mergeChunkFiles(MergeChunkRequest request, Long tenantId, Long userId) {
    try {
      String fileHash = request.getFileHash();
      List<FileChunkInfo> chunkDetails = fileChunkUploadCache.getAllChunkDetails(tenantId, userId, fileHash);

      // 对分片按索引排序，确保顺序正确
      chunkDetails.sort(Comparator.comparingInt(FileChunkInfo::getChunkIndex));

      File mergeFile = File.createTempFile("merge_", ".tmp");

      // 使用并发优化的合并方法
      long totalBytesWritten = mergeChunksConcurrently(chunkDetails, mergeFile);

      // 文件完整性校验
      if (!validateMergedFile(mergeFile, request, totalBytesWritten)) {
        throw new BssException("文件完整性校验失败");
      }

      return mergeFile;
    }
    catch (Exception e) {
      logger.error("合并分片文件失败: {}", e.getMessage(), e);
      throw new BssException("合并文件失败", e);
    }
  }

  /**
   * 并发合并分片文件 - 高性能优化版本 使用动态批处理、内存池管理和异步I/O来提高性能
   */
  private long mergeChunksConcurrently(List<FileChunkInfo> chunkDetails, File mergeFile) throws Exception {

    final int bufferSize = 128 * 1024; // 128KB缓冲区，提高I/O效率

    long totalBytesWritten = 0;
    long estimatedTotalSize = estimateTotalFileSize(chunkDetails);

    // 根据文件大小动态调整批处理大小
    int batchSize = calculateOptimalBatchSize(estimatedTotalSize);
    try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(mergeFile.toPath()), bufferSize)) {
      // 分批处理，避免内存溢出
      for (int i = 0; i < chunkDetails.size(); i += batchSize) {
        int endIndex = Math.min(i + batchSize, chunkDetails.size());
        List<FileChunkInfo> batch = chunkDetails.subList(i, endIndex);

        logger.debug("处理分片批次: {}-{}, 批次大小: {}", i, endIndex - 1, batch.size());

        // 并发下载当前批次的分片，使用限制的线程池
        Map<Integer, byte[]> chunkDataMap = downloadChunksWithMemoryLimit(batch);

        // 按顺序写入文件，确保数据完整性
        for (FileChunkInfo chunkDetail : batch) {
          byte[] chunkData = chunkDataMap.get(chunkDetail.getChunkIndex());
          if (chunkData != null && chunkData.length > 0) {
            outputStream.write(chunkData);
            totalBytesWritten += chunkData.length;

            // 定期刷新缓冲区，避免内存累积
            if (totalBytesWritten % (10 * 1024 * 1024) < chunkData.length) { // 每10MB刷新一次
              outputStream.flush();
            }
          }
          else {
            throw new BssException("分片数据为空或下载失败: chunkIndex=" + chunkDetail.getChunkIndex());
          }
        }

        // 批次处理完成后清理内存
        chunkDataMap.clear();

        // 强制刷新以确保数据持久化
        outputStream.flush();
      }
    }

    logger.trace("文件合并完成: 总写入字节={}, 目标文件={}", totalBytesWritten, mergeFile.getName());
    return totalBytesWritten;
  }

  /**
   * 估算总文件大小
   */
  private long estimateTotalFileSize(List<FileChunkInfo> chunkDetails) {
    if (chunkDetails.isEmpty()) {
      return 0;
    }

    // 基于最后一个分片的索引估算总大小
    int maxChunkIndex = chunkDetails.stream().mapToInt(FileChunkInfo::getChunkIndex).max().orElse(0);

    // 假设大部分分片大小相似，使用平均值估算
    long avgChunkSize = (long) chunkDetails.stream().mapToLong(chunk -> chunk.getChunkSize() != null ? chunk.getChunkSize() : 0)
      .filter(size -> size > 0).average().orElse(documentFilePreviewConfig.getMaxFileSize()); // 默认5MB

    return (maxChunkIndex + 1) * avgChunkSize;
  }

  /**
   * 计算最优批处理大小
   */
  private int calculateOptimalBatchSize(long estimatedTotalSize) {
    long maxMemoryUsage = documentFilePreviewConfig.getMaxFileSize();
    // 根据文件大小和内存限制动态调整批处理大小
    // 确保批处理大小不会超过内存限制（假设每个分片约5MB）
    final long estimatedChunkSize = documentFilePreviewConfig.getMaxChunkFileSize();
    int maxBatchByMemory = (int) (maxMemoryUsage / estimatedChunkSize);

    if (estimatedTotalSize < 50 * 1024 * 1024) { // 小文件 (< 50MB)
      return Math.min(10, Math.max(3, Math.min(maxBatchByMemory, (int) (estimatedTotalSize / estimatedChunkSize))));
    }
    else { // 大文件 (> 50MB)
      return Math.min(3, maxBatchByMemory);
    }
  }

  /**
   * 带内存限制的并发下载
   */
  private Map<Integer, byte[]> downloadChunksWithMemoryLimit(List<FileChunkInfo> chunks) {
    Map<Integer, byte[]> resultMap = new ConcurrentHashMap<>();

    // 创建下载任务列表
    List<Runnable> downloadTasks = new ArrayList<>();

    for (FileChunkInfo chunk : chunks) {
      downloadTasks.add(() -> {
        try {
          long startTime = System.currentTimeMillis();
          byte[] chunkData = downloadChunkData(chunk);
          long downloadTime = System.currentTimeMillis() - startTime;

          resultMap.put(chunk.getChunkIndex(), chunkData);

          logger.debug("分片下载完成: chunkIndex={}, size={}KB, time={}ms", chunk.getChunkIndex(), chunkData.length / 1024, downloadTime);

        }
        catch (Exception e) {
          logger.error("下载分片失败: chunkIndex={}, fileId={}, error={}", chunk.getChunkIndex(), chunk.getFileId(), e.getMessage());
          throw new BssException("下载分片失败: " + chunk.getChunkIndex(), e);
        }
      });
    }

    // 使用项目标准的文档处理线程池批量执行任务
    try {
      ThreadPools.invokeTasks(ThreadPools.getDocument(), downloadTasks);
    }
    catch (Exception e) {
      logger.error("批量下载分片失败: {}", e.getMessage());
      throw e;
    }

    return resultMap;
  }

  /**
   * 下载单个分片数据
   */
  private byte[] downloadChunkData(FileChunkInfo chunkDetail) {
    Long fileId = chunkDetail.getFileId();
    if (fileId == null) {
      throw new BssException("分片文件ID为空: chunkIndex=" + chunkDetail.getChunkIndex());
    }

    logger.debug("下载分片数据: fileId={}, chunkIndex={}", fileId, chunkDetail.getChunkIndex());

    // 从文件服务器下载分片数据
    byte[] fileBytes = fileStoreService.downloadFile(fileId);
    if (fileBytes == null || fileBytes.length == 0) {
      throw new BssException("下载分片数据为空: fileId=" + fileId);
    }

    return fileBytes;
  }

  /**
   * 处理合并后的文件
   */
  public ResultVO<DcDocumentDTO> processMergedFile(MergeChunkRequest request, File mergeFile, Long userId) {
    try {
      String fileName = request.getFileName();
      MultipartFile mergedMultipartFile = createMultipartFileFromFile(mergeFile, fileName);
      String documentId = request.getDocumentId();
      boolean isOverride = StringUtils.isNotBlank(documentId);
      DcDocumentDTO documentDTO;
      if (isOverride) {
        documentDTO = this.overrideDocumentFile(mergedMultipartFile, documentId, fileName);
      }
      else {
        // 新上传
        documentDTO = uploadMergedFile(request, mergedMultipartFile);
      }
      // 获取租户ID，确保租户级别隔离
      Long tenantId = TenantContextHolder.getTenantId();
      // 标记文件上传完成
      String finalPath = String.format("/%s/%s/%s/%s", tenantId, request.getBusiType(), userId, fileName);
      fileChunkUploadCache.markChunkBatchCompleted(tenantId, userId, request.getFileHash(), finalPath, 1);
      // 删除分片文件
      deleteChunkFile(fileChunkUploadCache.getAllChunkDetails(tenantId, userId, request.getFileHash()));
      return ResultVO.success(documentDTO);
    }
    catch (Exception e) {
      logger.error("处理合并文件失败: {}", e.getMessage(), e);
      return ResultVO.fail("处理合并文件失败: " + e.getMessage());
    }
  }

  private DcDocumentDTO uploadMergedFile(MergeChunkRequest request, MultipartFile multipartFile) {
    // 将合并后的文件转换为MultipartFile，然后调用uploadFile方法
    UnifiedUploadRequest fileRequest = new UnifiedUploadRequest();
    fileRequest.setLibraryId(request.getLibraryId());
    fileRequest.setParentId(request.getParentId());
    fileRequest.setConvertToOnline(request.getConvertToOnline());
    fileRequest.setTenantId(TenantContextHolder.getTenantId());
    fileRequest.setFileHash(request.getFileHash());
    fileRequest.setOriginalFileName(request.getFileName());
    fileRequest.setOriginalFileSize(request.getFileSize());
    // 是否为文件夹上传
    if (DocBaseConsts.TRUE.equals(request.getIsFolderUpload())) {
      fileRequest.setIsFolderUpload(request.getIsFolderUpload());
      fileRequest.setFolderUploadTaskId(request.getFolderUploadTaskId());
      fileRequest.setRelativePath(request.getRelativePath());
    }
    else {
      fileRequest.setIsFolderUpload(DocBaseConsts.FALSE);
    }
    FileInfoDTO fileInfoDTO = saveFile(multipartFile, request.getFileName());
    // 调用uploadFile方法处理文件上传和文档创建
    return saveDocument(fileRequest, fileInfoDTO);
  }

  /**
   * 清理临时文件
   */
  private void cleanupTempFile(File file) {
    if (file.exists()) {
      try {
        boolean deleted = file.delete();
        if (!deleted) {
          logger.warn("临时文件删除失败: {}", file.getAbsolutePath());
        }
      }
      catch (Exception e) {
        logger.warn("删除临时文件异常: {}", e.getMessage());
      }
    }
  }

  /**
   * 验证合并文件的完整性
   */
  private boolean validateMergedFile(File mergeFile, MergeChunkRequest request, long totalBytesWritten) {
    try {
      // 1. 检查文件是否存在且可读
      if (!mergeFile.exists() || !mergeFile.canRead()) {
        logger.error("合并文件不存在或不可读: {}", mergeFile.getAbsolutePath());
        return false;
      }

      // 2. 检查文件大小是否与预期一致
      long actualFileSize = mergeFile.length();
      if (actualFileSize != totalBytesWritten) {
        logger.error("文件大小不匹配: 预期={}, 实际={}", totalBytesWritten, actualFileSize);
        return false;
      }

      // 3. 检查文件大小是否合理（可以根据分片数量和分片大小估算）
      // 这里可以根据实际业务需求添加更严格的大小检查
      if (actualFileSize <= 0) {
        logger.error("文件大小异常: {}", actualFileSize);
        return false;
      }

      return totalBytesWritten == request.getFileSize();
    }
    catch (Exception e) {
      logger.error("文件完整性校验异常: {}", e.getMessage(), e);
      return false;
    }
  }

  /**
   * 将File对象转换为MultipartFile
   */
  private MultipartFile createMultipartFileFromFile(File file, String fileName) {
    return new MultipartFile() {
      @Override
      public String getName() {
        return fileName;
      }

      @Override
      public String getOriginalFilename() {
        return fileName;
      }

      @Override
      public String getContentType() {
        return "";
      }

      @Override
      public boolean isEmpty() {
        return file.length() == 0;
      }

      @Override
      public long getSize() {
        return file.length();
      }

      @Override
      public byte[] getBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return Files.newInputStream(file.toPath());
      }

      @Override
      @SuppressWarnings("PMD.GuardLogStatement")
      public void transferTo(File dest) throws IOException, IllegalStateException {
        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    };
  }

  private void cleanupFailedUploadInternal(Long tenantId, Long userId, String chunkBatchId) {
    try {
      // 获取所有分片信息
      List<FileChunkInfo> chunkDetails = fileChunkUploadCache.getAllChunkDetails(tenantId, userId, chunkBatchId);

      // 删除文件服务器中的分片文件
      for (FileChunkInfo chunkInfo : chunkDetails) {
        if (chunkInfo.getFileId() != null) {
          try {
            fileStoreService.deleteFile(chunkInfo.getFileId());
            logger.debug("删除失败的分片文件: fileId={}", chunkInfo.getFileId());
          }
          catch (Exception e) {
            logger.warn("删除分片文件失败: fileId={}, error={}", chunkInfo.getFileId(), e.getMessage());
          }
        }
      }

      // 删除缓存中的分片信息
      fileChunkUploadCache.deleteChunkBatchStatus(tenantId, userId, chunkBatchId);
      logger.trace("清理失败的分片上传: tenantId={}, userId={}, chunkBatchId={}", tenantId, userId, chunkBatchId);
    }
    catch (Exception e) {
      logger.error("清理失败的分片上传异常: tenantId={}, userId={}, chunkBatchId={}, error={}", tenantId, userId, chunkBatchId, e.getMessage(), e);
    }
  }

  /**
   * 构建文件夹树结构
   */
  @Nullable
  private DcDocumentTreeDTO buildFolderTreeStructure(String rootDocumentId) {
    try {
      // 1. 获取根节点下的所有子节点ID
      List<String> allNodeIds = getAllNodeIdsInTree(rootDocumentId);
      Long currentLoginUserId = SessionUtil.getLoginInfo().getUserId();
      // 2. 批量查询所有节点信息
      List<DcDocumentDTO> allDocuments = documentService.findBatchByDocumentId(allNodeIds);
      // 根文件夹父节点必须设置为0 不然无法拼装树结构
      allDocuments.forEach(doc -> {
        if (rootDocumentId.equals(doc.getDocumentId())) {
          doc.setParentId("0");
        }
        if (!DocumentTypeEnum.FOLDER.getCode().equals(doc.getDocumentType())) {
          documentChangEventPublisher.publishUploadEvent(doc.getLibraryId(), doc.getDocumentId(), doc.getDocumentName(), currentLoginUserId);
        }
      });
      // 3. 转换为树结构DTO
      List<DcDocumentTreeDTO> treeNodes = allDocuments.stream()
        .map(this::convertToTreeDTO)
        .collect(Collectors.toList());
      // 4. 构建树结构
      List<DcDocumentTreeDTO> treeList = new DefaultTreeBuildFactory<DcDocumentTreeDTO>().doTreeBuild(treeNodes);
      // 5. 同级排序
      NodeSortHelper.sortNodeAtSameLevel(treeList);
      // 6. 返回根节点
      return treeList.isEmpty() ? null : treeList.get(0);
    }
    catch (Exception e) {
      logger.error("构建文件夹树结构失败: rootDocumentId={}, error={}", rootDocumentId, e.getMessage(), e);
      // 如果构建失败，至少返回根节点信息
      DcDocumentDTO rootDocument = documentService.findByDocumentId(rootDocumentId);
      return rootDocument != null ? convertToTreeDTO(rootDocument) : null;
    }
  }

  /**
   * 获取指定节点树中的所有节点ID
   */
  private List<String> getAllNodeIdsInTree(String rootNodeId) {
    // 使用 DocumentNodeService 的方法获取所有子节点ID，深度设为-1表示获取所有层级
    return documentNodeService.getNodeIdsInNodeTree(rootNodeId, -1);

  }

  /**
   * 将 DcDocumentDTO 转换为 DcDocumentTreeDTO
   */
  private DcDocumentTreeDTO convertToTreeDTO(DcDocumentDTO document) {
    DcDocumentTreeDTO treeDTO = new DcDocumentTreeDTO();
    // 复制基本属性
    treeDTO.setId(document.getId());
    treeDTO.setDocumentId(document.getDocumentId());
    treeDTO.setDocumentName(document.getDocumentName());
    treeDTO.setLibraryId(document.getLibraryId());
    treeDTO.setParentId(document.getParentId());
    treeDTO.setDocumentType(document.getDocumentType());
    treeDTO.setContentSource(document.getContentSource());
    treeDTO.setFileInfoId(document.getFileInfoId());
    treeDTO.setRevision(document.getRevision());
    treeDTO.setWordCount(document.getWordCount());
    treeDTO.setViewCount(document.getViewCount());
    treeDTO.setIsConvert(document.getIsConvert());
    treeDTO.setIsBuiltin(document.getIsBuiltin());
    treeDTO.setBuiltinType(document.getBuiltinType());
    treeDTO.setPrevDocumentId(document.getPrevDocumentId());
    treeDTO.setTenantId(document.getTenantId());
    treeDTO.setCreatedTime(document.getCreatedTime());
    treeDTO.setUpdatedTime(document.getUpdatedTime());
    treeDTO.setStatusCd(document.getStatusCd());
    // 复制扩展属性
    treeDTO.setLibraryName(document.getLibraryName());
    treeDTO.setFileId(document.getFileId());
    return treeDTO;
  }

  /**
   * 创建根文件夹节点
   */
  private DcDocumentDTO createRootFolder(FolderStructureCreateRequest request, Long userId, Long tenantId) {
    // 上传前校验一把文件夹是否存在
    if (StringUtils.isEmpty(request.getParentId())) {
      String rootNodeIdByLibraryId = documentNodeService.getRootNodeIdByLibraryId(request.getLibraryId(), request.getTenantId(), request.getSpaceId());
      request.setParentId(rootNodeIdByLibraryId);
    }
    if (documentService.existsDocumentByName(request.getLibraryId(), request.getParentId(), null, request.getFolderName(), DocumentTypeEnum.FOLDER.getCode(), ContentSourceEnum.ONLINE.getCode())) {
      String newFolderName = request.getFolderName() + "(" + System.currentTimeMillis() + ")";
      if (newFolderName.length() > 150) {
        newFolderName = newFolderName.substring(0, 150);
      }
      logger.trace("创建根文件夹重置名字: oldname={},newName={}", request.getFolderName(), newFolderName);
      request.setFolderName(newFolderName);
    }
    // 使用通用方法创建根文件夹请求
    DocumentCreateRequestDTO createRequest = buildDocumentRequest(request.getLibraryId(), request.getParentId(), DocumentTypeEnum.FOLDER.getCode(),
      request.getFolderName(), null, // 文件夹没有文件信息
      DocBaseConsts.FALSE, // 文件夹不转换
      tenantId, null // 没有前置文档
    );

    // 创建并返回文件夹
    DcDocumentDTO folderDTO = createDocumentAndReturn(createRequest, userId, "创建根文件夹失败，未能获取文件夹信息");

    logger.trace("创建根文件夹: name={}, documentId={}", request.getFolderName(), folderDTO.getDocumentId());
    return folderDTO;
  }

  /**
   * 批量创建文件文档节点
   */
  private void batchCreateFileDocuments(Map<String, List<FolderStructureCreateRequest.UploadedFileInfo>> filesByParent,
                                        FolderStructureCreateRequest request, Long userId, Long tenantId) {

    List<DocumentCreateRequestDTO> documentCreateRequests = new ArrayList<>();
    // 收集所有需要创建文档的文件信息
    for (Map.Entry<String, List<FolderStructureCreateRequest.UploadedFileInfo>> entry : filesByParent.entrySet()) {
      String parentId = entry.getKey();
      List<FolderStructureCreateRequest.UploadedFileInfo> files = entry.getValue();
      for (FolderStructureCreateRequest.UploadedFileInfo fileInfo : files) {
        // 1. 获取文件信息ID（优先从缓存中获取，如果没有则从哈希获取）
        Long fileInfoId = getFileInfoIdFromCache(request.getFolderUploadTaskId(), fileInfo.getFileHash());
        if (fileInfoId == null) {
          // 降级处理：根据文件哈希获取 fileId，然后创建文件信息记录
          String fileId = getFileIdByHash(fileInfo.getFileHash());
          if (fileId == null) {
            continue;
          }
          FileInfoDTO fileInfoDTO = fileUploadHelper.saveFileInfo(Long.valueOf(fileId), fileInfo.getOriginalFileName(), tenantId, userId);
          fileInfoId = fileInfoDTO.getFileInfoId();
        }
        // 2. 构建文档创建请求
        DocumentCreateRequestDTO createRequest = buildDocumentRequest(request.getLibraryId(), parentId,
          DcIdUtils.determineDocumentType(fileInfo.getOriginalFileName()), fileInfo.getOriginalFileName(), fileInfoId,
          fileInfo.getConvertToOnline(), tenantId, null);
        documentCreateRequests.add(createRequest);
      }
    }

    if (documentCreateRequests.isEmpty()) {
      logger.warn("没有需要创建文档的文件");
      return;
    }
    // 批量创建文档节点
    documentService.batchCreateUploadDocumentNodes(userId, documentCreateRequests);
  }

  /**
   * 根据文件哈希获取 fileId
   */
  @Nullable
  @SuppressWarnings("PMD.GuardLogStatement")
  private String getFileIdByHash(String fileHash) {
    Long tenantId = TenantContextHolder.getTenantId();
    Long userId = SessionUtil.getLoginInfo().getUserId();

    // 从文件分片缓存中获取文件的最终 fileId
    String fileUrl = fileChunkUploadCache.getFileUrl(tenantId, userId, fileHash);
    if (fileUrl != null) {
      // 从 URL 中提取 fileId，这里需要根据实际的 URL 格式来解析
      // 假设 URL 格式为: /tenantId/files/{fileId}/filename
      String[] parts = fileUrl.split("/");
      if (parts.length >= 4) {
        return parts[parts.length - 2]; // 获取倒数第二部分作为 fileId
      }
    }

    logger.warn("未找到文件的URL信息: tenantId={}, userId={}, fileHash={}", tenantId, userId, fileHash);
    return null;
  }

  /**
   * 从缓存中获取上传成功的文件信息
   * 从HSET中获取文件信息，避免数据丢失
   */
  private List<FolderStructureCreateRequest.UploadedFileInfo> getUploadedFilesFromCache(String taskId) {
    try {
      // 从HSET中获取所有文件信息
      List<FolderUploadCacheDTO.UploadedFileInfo> cacheFiles = folderUploadCache.getAllFileInfos(taskId);
      if (cacheFiles == null || cacheFiles.isEmpty()) {
        logger.warn("未找到文件夹上传缓存: taskId={}", taskId);
        return new ArrayList<>();
      }

      List<FolderStructureCreateRequest.UploadedFileInfo> result = new ArrayList<>();
      for (FolderUploadCacheDTO.UploadedFileInfo cacheFile : cacheFiles) {
        FolderStructureCreateRequest.UploadedFileInfo fileInfo = new FolderStructureCreateRequest.UploadedFileInfo();
        fileInfo.setFileHash(cacheFile.getFileHash());
        fileInfo.setRelativePath(cacheFile.getRelativePath());
        fileInfo.setOriginalFileName(cacheFile.getOriginalFileName());
        fileInfo.setFileSize(cacheFile.getFileSize());
        fileInfo.setBusiType(cacheFile.getBusiType());
        fileInfo.setConvertToOnline(cacheFile.getConvertToOnline());
        result.add(fileInfo);
      }

      logger.debug("从缓存中获取文件信息成功: taskId={}, fileCount={}", taskId, result.size());
      return result;
    }
    catch (Exception e) {
      logger.error("从缓存中获取文件信息失败: taskId={}, error={}", taskId, e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * 根据任务ID和文件哈希从缓存中获取文件信息ID
   * 从HSET中获取文件信息，避免数据丢失
   */
  @Nullable
  private Long getFileInfoIdFromCache(String taskId, String fileHash) {
    if (StringUtils.isBlank(taskId) || StringUtils.isBlank(fileHash)) {
      return null;
    }

    try {
      // 从HSET中获取所有文件信息
      List<FolderUploadCacheDTO.UploadedFileInfo> cacheFiles = folderUploadCache.getAllFileInfos(taskId);
      if (cacheFiles == null || cacheFiles.isEmpty()) {
        return null;
      }

      // 查找匹配的文件哈希
      for (FolderUploadCacheDTO.UploadedFileInfo cacheFile : cacheFiles) {
        if (fileHash.equals(cacheFile.getFileHash())) {
          return cacheFile.getFileInfoId();
        }
      }

      logger.debug("缓存中未找到文件信息ID: taskId={}, fileHash={}", taskId, fileHash);
      return null;
    }
    catch (Exception e) {
      logger.error("从缓存中获取文件信息ID失败: taskId={}, fileHash={}, error={}", taskId, fileHash, e.getMessage(), e);
      return null;
    }
  }

  /**
   * 按父目录分组文件，确保同一目录下的文件按原始顺序排列
   */
  private Map<String, List<FolderStructureCreateRequest.UploadedFileInfo>> groupFilesByParent(
    List<FolderStructureCreateRequest.UploadedFileInfo> files, String rootFolderId, FolderStructureCreateRequest request) {

    Map<String, List<FolderStructureCreateRequest.UploadedFileInfo>> filesByParent = new LinkedHashMap<>();

    for (FolderStructureCreateRequest.UploadedFileInfo file : files) {
      // 确定文件的父文件夹ID，传入根文件夹名称避免重复创建
      String parentId = determineParentId(file.getRelativePath(), rootFolderId, request.getLibraryId(), request.getFolderName());
      // 将文件添加到对应的父目录分组中
      filesByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(file);
    }

    return filesByParent;
  }

  private DocumentCreateRequestDTO buildDocumentRequest(String libraryId,
                                                        String parentId,
                                                        String documentType,
                                                        String documentName,
                                                        @Nullable Long fileInfoId,
                                                        String isConvert,
                                                        Long tenantId,
                                                        @Nullable String prevDocumentId) {
    DocumentCreateRequestDTO createRequest = new DocumentCreateRequestDTO();
    createRequest.setLibraryId(libraryId);
    createRequest.setParentId(parentId);
    createRequest.setDocumentType(documentType);
    createRequest.setDocumentName(documentName);
    createRequest.setFileInfoId(fileInfoId);
    createRequest.setIsConvert(isConvert);
    createRequest.setTenantId(tenantId);
    createRequest.setPrevDocumentId(prevDocumentId);
    return createRequest;
  }

  private DcDocumentDTO createDocumentAndReturn(DocumentCreateRequestDTO createRequest, Long userId, String errorMessage) {
    // 使用新的统一服务方法创建文档
    String documentId = documentService.createUploadDocumentNode(userId, createRequest);

    // 查询创建的文档实体返回
    DcDocumentDTO documentDTO = documentService.findByDocumentId(documentId);
    if (documentDTO == null) {
      throw new BssException(errorMessage);
    }
    return documentDTO;
  }

  private DcDocumentDTO saveDocument(UnifiedUploadRequest request, FileInfoDTO fileInfoDTO) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Long tenantId = TenantContextHolder.getTenantId();
    // 根据是否文件夹上传决定是否创建文档节点
    if (DocBaseConsts.TRUE.equals(request.getIsFolderUpload())) {
      // 文件夹文件上传：缓存文件信息，不创建文档节点
      cacheFolderFileInfo(request.getFolderUploadTaskId(), request.getFileHash(), fileInfoDTO.getFileInfoId(), request);
      // 返回空的文档对象，标识文件上传成功但未创建文档
      DcDocumentDTO document = new DcDocumentDTO();
      document.setFileInfoId(fileInfoDTO.getFileInfoId());
      return document;
    }
    else {
      // 单文件上传：创建文档记录
      return createDocument(fileInfoDTO, request, userId, tenantId);
    }
  }

  private FileInfoDTO saveFile(MultipartFile file, String fileName) {
    if (StringUtils.isEmpty(fileName)) {
      fileName = StringUtils.trimToNull(file.getOriginalFilename());
    }
    String fileType = FilenameUtils.getExtension(fileName);
    Assert.hasText(fileType, "获取文件类型失败");
    ResultVO<Void> validateResult = checkFileType(fileType.toLowerCase());
    if (!validateResult.isSuccess()) {
      // 文件流获取类型不支持的话 再从文件名的后缀中获取校验
      throw new BssException(validateResult.getResultMsg());
    }
    return fileUploadHelper.uploadFileWithInfoSave(file, fileName);
  }

  @Override
  @Transactional
  public ResultVO<Void> rollbackDocumentFromFileLog(Long fileLogId, Long tenantId) {
    ResultVO<RollbackFromLogContext> prepared = buildRollbackFromFileLogContext(fileLogId, tenantId);
    if (!prepared.isSuccess()) {
      return ResultVO.fail(prepared.getResultMsg());
    }
    return downloadHistoricalFileAndOverrideDocument(prepared.getResultObject());
  }

  /**
   * 校验日志与历史文件元数据，组装回退所需上下文
   */
  private ResultVO<RollbackFromLogContext> buildRollbackFromFileLogContext(Long fileLogId, Long tenantId) {
    DocumentFileLogDTO logDto = documentFileLogService.getDetailById(fileLogId, tenantId);
    if (logDto == null) {
      return ResultVO.fail("上传日志不存在");
    }
    if (logDto.getFileId() == null) {
      return ResultVO.fail("历史文件不存在");
    }
    String fileName = StringUtils.trimToNull(logDto.getFileName());
    if (StringUtils.isBlank(fileName)) {
      return ResultVO.fail("历史文件名不存在");
    }
    FileInfoVO historicalFile = fileStoreService.getFileInfoById(logDto.getFileId());
    if (historicalFile == null) {
      return ResultVO.fail("无法在文件存储中定位该历史版本文件");
    }
    long fileSize = historicalFile.getFileSize() != null ? historicalFile.getFileSize() : 0L;
    if (!documentFilePreviewConfig.isFileSizeValid(fileSize)) {
      return ResultVO.fail("文件大小超出上传限制，文件限制大小" + FileUtils.byteCountToDisplaySize(documentFilePreviewConfig.getMaxFileSize()));
    }
    return ResultVO.success(new RollbackFromLogContext(logDto, fileName, historicalFile));
  }

  /**
   * 将历史文件下载到临时文件后以覆盖上传方式写回当前文档
   */
  private ResultVO<Void> downloadHistoricalFileAndOverrideDocument(RollbackFromLogContext ctx) {
    String ext = FilenameUtils.getExtension(ctx.fileName());
    String suffix = StringUtils.isNotBlank(ext) ? "." + ext : null;
    Path tempPath = null;
    try {
      tempPath = Files.createTempFile("doc-log-rollback-", suffix);
      try (InputStream in = fileStoreService.downloadFileStream(ctx.historicalFile())) {
        Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
      }
      MultipartFile multipartFile = createMultipartFileFromFile(tempPath.toFile(), ctx.fileName());
      overrideDocumentFile(multipartFile, ctx.logDto().getDocumentId(), ctx.fileName());
      return ResultVO.success();
    }
    catch (IOException e) {
      throw new BssException("下载或处理历史文件失败: " + e.getMessage(), e);
    }
    finally {
      if (tempPath != null) {
        try {
          Files.deleteIfExists(tempPath);
        }
        catch (IOException e) {
          logger.warn("Failed to delete temp file after rollback: {}", tempPath, e);
        }
      }
    }
  }

  /**
   * 按上传日志回退时，校验通过后携带的上下文
   */
  private record RollbackFromLogContext(DocumentFileLogDTO logDto, String fileName, FileInfoVO historicalFile) {
  }
}
