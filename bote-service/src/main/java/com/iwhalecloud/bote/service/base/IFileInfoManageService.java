package com.iwhalecloud.bote.service.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.base.SyncFileInfoRequest;
import com.iwhalecloud.bote.dto.base.query.FileInfoQueryParams;
import com.iwhalecloud.bote.dto.knowledge.query.UpdateFileParams;
import com.iwhalecloud.bote.dto.knowledge.query.UploadFileParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件信息管理服务
 *
 * @author auto
 * @since 2024-09-24
 */
public interface IFileInfoManageService {

  /**
   * 查询单个文件信息
   *
   * @param tenantId 租户 ID
   * @param fileInfoId 文件信息主键
   * @return 文件信息
   */
  @Nullable
  FileInfoDTO findFileInfo(Long tenantId, Long fileInfoId);

  /**
   * 删除文件信息
   *
   * @param tenantId 租户 ID
   * @param fileInfoId 文件信息主键
   * @return 结果
   */
  ResultVO<Void> deleteFileInfo(Long tenantId, Long fileInfoId);

  /**
   * 查询文件信息列表
   *
   * @param queryParams 查询条件
   * @return 文件信息列表
   */
  List<FileInfoDTO> queryFileInfoList(FileInfoQueryParams queryParams);

  /**
   * 查询文件信息列表（分页）
   *
   * @param queryParams 查询条件
   * @return 文件信息分页列表
   */
  PageInfo<FileInfoDTO> queryFileInfoPage(FileInfoQueryParams queryParams);

  /**
   * 重新上传文件
   *
   * @param file 文件
   * @param fileInfoId 文件信息ID
   * @param tenantId 租户 ID
   * @return 文件信息
   */
  ResultVO<FileInfoDTO> reUploadFile(MultipartFile file, Long fileInfoId, Long tenantId);

  /**
   * 上传文件
   *
   * @param file 文件
   * @param query 参数
   * @return 文件信息
   */
  ResultVO<FileInfoDTO> uploadFile(MultipartFile file, UploadFileParams query);

  /**
   * 编辑文件信息
   *
   * @param file 文件（可选，如果不为空则会重新上传文件）
   * @param query 编辑参数
   * @return 文件信息
   */
  ResultVO<FileInfoDTO> updateFile(MultipartFile file, UpdateFileParams query);

  /**
   * 文件 OCR 识别
   *
   * @param file 文件
   * @return 结果
   */
  ResultVO<Object> ocrFile(MultipartFile file);

  /**
   * 批量删除文件信息
   *
   * @param tenantId 租户 ID
   * @param fileInfoIds 文件信息ID列表
   * @return 结果
   */
  ResultVO<Void> batchDeleteFileInfo(Long tenantId, List<Long> fileInfoIds);

  /**
   * 同步文件信息
   *
   * @param request 同步文件信息请求
   * @return 文件信息
   */
  ResultVO<FileInfoDTO> syncFileInfo(SyncFileInfoRequest request);

  /**
   * 更新fileInfo中的关联的文件ID
   *
   * @param tenantId 租户ID
   * @param fileInfoId fileInfo主键
   * @param fileId 文件ID
   * @param fileName 文件名称
   */
  void syncFileInfoById(Long tenantId, Long fileInfoId, Long fileId, String fileName);

  /**
   * 在租户下新增一条 bt_file_info，复用已有 bt_file（同一 file_id），不复制对象存储中的文件。
   */
  FileInfoDTO createTenantRefFileInfo(Long tenantId, Long fileId, String fileName, String busiType);

  /**
   * 仅将 bt_file_info 置为无效，不删除对象存储中的物理文件（多记录引用同一 file_id 时使用）。
   */
  void softDeleteFileInfoRecordOnly(Long tenantId, Long fileInfoId);
}
