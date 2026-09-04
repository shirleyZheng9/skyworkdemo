package com.iwhalecloud.bote.doc.module.document.service;

import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookContentHistoryInfoDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockRequestDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookLockStatusDTO;
import com.iwhalecloud.bote.doc.module.document.dto.WorkbookSaveRequestDTO;
import java.util.List;

/**
 * 工作簿服务接口
 *
 * @author Aiqing
 * @since 2025-09-26
 */
public interface IWorkbookContentService {

  /**
   * 查询工作簿锁定状态
   *
   * @param documentId 文档ID
   * @param userId 当前用户ID
   * @param sessionId 当前会话ID
   * @return 锁定状态信息
   */
  WorkbookLockStatusDTO getLockStatus(String documentId, Long userId, String sessionId);

  /**
   * 锁定/解锁工作簿
   *
   * @param request 锁定请求参数
   * @param userId 当前用户ID
   * @param sessionId 当前会话ID
   * @return 锁定状态信息
   */
  WorkbookLockStatusDTO lockWorkbook(WorkbookLockRequestDTO request, Long userId, String sessionId);

  /**
   * 保存工作簿内容
   *
   * @param request 保存请求参数
   * @param userId 当前用户ID
   * @return 保存后的工作簿内容信息
   */
  WorkbookContentDTO saveWorkbook(WorkbookSaveRequestDTO request, Long userId);

  /**
   * 获取工作簿内容
   *
   * @param documentId 文档ID
   * @param userId 当前用户ID
   * @return 工作簿内容
   */
  WorkbookContentDTO getWorkbookContent(String documentId, Long userId);

  /**
   * 保存在线表格的历史版本
   *
   * @param documentId 文档ID
   * @param updatorId 更新人ID
   */
  void saveContentHistory(String documentId, Long updatorId);

  /**
   * 通过文档id查询在线列表历史版本
   * @param documentId 文档id
   * @return 在线文档列表数据
   */
  List<WorkbookContentHistoryInfoDTO> getWorkbookVersions(String documentId);

  /**
   * 通过文档id和历史列表id回退指定版本
   *
   * @param documentId 文档id
   * @param id 历史文件id
   * @return 恢复后的工作簿内容（与保存接口一致，含 libraryId）
   */
  WorkbookContentDTO restoreContentVersion(String documentId, Long id);

  /**
   * 需要将上传文件上的json文件下载转换为json字符串返回
   * @param id
   * @return
   */
  String getWorkbookHisContent(Long id);
}
