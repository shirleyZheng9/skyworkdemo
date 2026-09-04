package com.iwhalecloud.bote.doc.module.document.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityDTO;
import com.iwhalecloud.bote.doc.module.document.dto.DocumentActivityQueryParams;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档动态记录服务接口
 *
 * @author Aiqing
 * @since 2025-08-21
 */
public interface IDocumentActivityService {

  /**
   * 记录文档操作动态
   *
   * @param documentId 文档ID
   * @param libraryId 文档库ID
   * @param userId 操作用户ID
   * @param actionType 操作类型
   * @param ipAddress IP地址
   * @param userAgent 用户代理
   * @param tenantId 租户ID
   * @return 动态记录ID
   */
  Long recordActivity(String documentId, String libraryId, Long userId, String actionType,
                      String ipAddress, String userAgent, Long tenantId, String documentName, String targetLibraryId);

  /**
   * 分页查询文档动态记录
   *
   * @param queryParams 查询参数
   * @return 分页查询结果
   */
  PageInfo<DocumentActivityDTO> findActivityPage(DocumentActivityQueryParams queryParams);

  /**
   * 统计文档访问次数
   *
   * @param documentId 文档ID
   * @param actionType 操作类型
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 访问次数
   */
  Long countDocumentAccess(String documentId, String actionType, LocalDateTime startTime,
                           LocalDateTime endTime);

  /**
   * 统计动态记录数量
   *
   * @param documentId 文档ID
   * @param libraryId 文档库ID
   * @param userId 用户ID
   * @param actionType 操作类型
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @return 记录数量
   */
  Long countByCondition(String documentId, String libraryId, Long userId, String actionType,
                        LocalDateTime startTime, LocalDateTime endTime);

  List<DocumentActivityDTO> findByDocumentId(String documentId, Integer limit);
}
