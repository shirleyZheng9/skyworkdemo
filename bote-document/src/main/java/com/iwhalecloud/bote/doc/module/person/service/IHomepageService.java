package com.iwhalecloud.bote.doc.module.person.service;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.FrequentLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.RecentFileDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.ReorderPinsRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.SearchDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UnpinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.RecentFileQueryParams;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.SearchDocumentQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 首页模块 Service 接口
 *
 * @author yangran
 * @since 2025-01-06
 */
public interface IHomepageService {

  /**
   * 获取置顶文档列表
   *
   * @param spaceId 空间ID
   * @return 置顶文档列表
   */
  List<PinnedDocumentDTO> getPinnedDocuments(Long spaceId);

  /**
   * 获取置顶文档库列表
   *
   * @param spaceId 空间ID
   * @return 置顶文档库列表
   */
  List<PinnedLibraryDTO> getPinnedLibraries(Long spaceId);

  /**
   * 获取置顶知识库列表
   *
   * @param spaceId 空间ID
   * @return 置顶知识库列表
   */
  List<PinnedKnowledgeDTO> getPinnedKnowledge(Long spaceId);

  /**
   * 设置首页置顶
   *
   * @param dto 置顶资源请求参数
   * @param userId 用户ID
   * @return 置顶记录ID
   */
  ResultVO<Long> pinResource(PinResourceRequestDTO dto, Long userId);

  /**
   * 取消首页置顶
   *
   * @param request 目标类型
   * @param userId 用户ID
   * @return 操作结果
   */
  ResultVO<Void> unpinResource(UnpinResourceRequestDTO request, Long userId);

  /**
   * 租户维度删除置顶数据
   *
   * @param tenantId 租户ID
   * @param targetId 目标资源ID
   * @param targetType 目标类型
   * @return 操作结果
   */
  ResultVO<Void> unpinResourceByTenant(Long tenantId, String targetId, String targetType, Long spaceId);

  /**
   * 调整首页置顶排序
   *
   * @param request 调整置顶排序请求参数
   * @param userId 用户ID
   * @return 操作结果，成功时返回targetType
   */
  ResultVO<String> reorderPins(ReorderPinsRequestDTO request, Long userId);

  /**
   * 获取常用文档库列表
   *
   * @return 常用文档库列表
   */
  List<FrequentLibraryDTO> getFrequentLibraries(Long tenantId, Long spaceId, String platform);

  /**
   * 获取最近访问文件列表
   *
   * @param queryParams 查询参数
   * @return 最近访问文件分页数据
   */
  PageInfo<RecentFileDTO> getRecentFiles(RecentFileQueryParams queryParams);

  /**
   * 从最近访问列表中移除文件
   *
   * @param documentId 文档ID
   * @param deleteSourceAfter 是否删除源文件
   * @return 操作结果
   */
  ResultVO<Void> removeFromRecentFiles(String documentId, Boolean deleteSourceAfter);

  /**
   * 搜索文档
   *
   * @param queryParams 搜索参数
   * @return 搜索结果分页数据
   */
  PageInfo<SearchDocumentDTO> searchDocuments(SearchDocumentQueryParams queryParams);
}
