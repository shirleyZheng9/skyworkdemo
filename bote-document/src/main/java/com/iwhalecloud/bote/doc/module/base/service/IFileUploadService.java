package com.iwhalecloud.bote.doc.module.base.service;

import com.iwhalecloud.bote.doc.module.base.dto.BaseChunkRequest;
import com.iwhalecloud.bote.doc.module.base.dto.FolderStructureCreateRequest;
import com.iwhalecloud.bote.doc.module.base.dto.FolderUploadCacheDTO;
import com.iwhalecloud.bote.doc.module.base.dto.MergeChunkRequest;
import com.iwhalecloud.bote.doc.module.base.dto.UnifiedUploadRequest;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DcDocumentTreeDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 *
 * @author yangran
 * @since 2025-08-25
 */
public interface IFileUploadService {

  /**
   * 上传单个文件
   *
   * @param file 上传的文件
   * @param request 上传请求参数
   * @return 文档信息
   */
  ResultVO<DcDocumentDTO> uploadFile(MultipartFile file, UnifiedUploadRequest request);

  /**
   * 上传分片文件
   *
   * @param chunkFile 分片文件
   * @param request 分片上传请求
   * @return 上传结果
   */
  ResultVO<Boolean> uploadChunk(MultipartFile chunkFile, UnifiedUploadRequest request);

  /**
   * 检查文件状态（用于断点续传）
   *
   * @param request 文件哈希值
   * @return 文件状态信息
   */
  ResultVO<Map<String, Object>> checkFileStatus(BaseChunkRequest request);

  /**
   * 合并分片文件
   *
   * @param request 合并请求
   * @return 合并结果
   */
  ResultVO<DcDocumentDTO> mergeChunks(MergeChunkRequest request);

  /**
   * 清理失败的分片上传
   *
   * @param chunkBatchId 分片批次号
   * @return 清理结果
   */
  ResultVO<Boolean> cleanupFailedUpload(String chunkBatchId);


  /**
   * 获取文件夹上传进度
   *
   * @param taskId 任务ID
   * @return 上传进度信息
   */
  ResultVO<FolderUploadCacheDTO> getFolderUploadProgress(String taskId);

  /**
   * 暂停文件夹上传
   *
   * @param taskId 任务ID
   * @return 操作结果
   */
  ResultVO<Boolean> pauseFolderUpload(String taskId);

  /**
   * 恢复文件夹上传
   *
   * @param taskId 任务ID
   * @return 操作结果
   */
  ResultVO<Boolean> resumeFolderUpload(String taskId);

  /**
   * 取消文件夹上传
   *
   * @param taskId 任务ID
   * @return 操作结果
   */
  ResultVO<Boolean> cancelFolderUpload(String taskId);

  /**
   * 创建文件夹结构
   *
   * @param request 文件夹创建请求
   * @return 创建结果，包含完整的文件夹树结构
   */
  ResultVO<DcDocumentTreeDTO> createFolderStructure(FolderStructureCreateRequest request);

  /**
   * 创建文件夹结构
   *
   * @param rootId 跟节点
   * @return 创建结果，包含完整的文件夹树结构
   */
  List<DcDocumentDTO> queryDcDocumentDTOByRootId(String rootId);

  /**
   * 按文档上传文件日志主键回退：下载该日志关联的历史文件（bt_file.file_name 作为文件名），再以覆盖上传方式更新当前文档对应文件。
   *
   * @param fileLogId {@code bt_dc_document_file_log.id}
   * @return 回退后的文档信息
   */
  ResultVO<Void> rollbackDocumentFromFileLog(Long fileLogId, Long tenantId);

}
