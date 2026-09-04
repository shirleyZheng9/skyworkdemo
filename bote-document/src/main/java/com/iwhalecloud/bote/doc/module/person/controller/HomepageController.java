package com.iwhalecloud.bote.doc.module.person.controller;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.cache.PinnedDocumentCache;
import com.iwhalecloud.bote.doc.cache.PinnedKnowledgeCache;
import com.iwhalecloud.bote.doc.cache.PinnedLibraryCache;
import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.FrequentLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedKnowledgeDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedLibraryDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.RecentFileDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.RemoveFromRecentFilesRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.ReorderPinsRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.SearchDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.UnpinResourceRequestDTO;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.RecentFileQueryParams;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.query.SearchDocumentQueryParams;
import com.iwhalecloud.bote.doc.module.person.service.IHomepageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页管理
 *
 * @author yangran
 * @since 2025-01-06
 */
@RestController
@RequestMapping(path = DocBaseConsts.API_PREFIX + "dc/homepage", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "文档中心：首页管理")
public class HomepageController {
  private final IHomepageService homepageService;

  private final PinnedDocumentCache pinnedDocumentCache;

  private final PinnedKnowledgeCache pinnedKnowledgeCache;

  private final PinnedLibraryCache pinnedLibraryCache;

  @GetMapping("pinnedDocuments")
  @Operation(summary = "获取置顶文档列表")
  public ResultVO<List<PinnedDocumentDTO>> getPinnedDocuments(@RequestParam Long spaceId) {
    return ResultVO.success(homepageService.getPinnedDocuments(spaceId));
  }

  @GetMapping("pinnedLibraries")
  @Operation(summary = "获取置顶文档库列表")
  public ResultVO<List<PinnedLibraryDTO>> getPinnedLibraries(@RequestParam Long spaceId) {
    return ResultVO.success(homepageService.getPinnedLibraries(spaceId));
  }

  @GetMapping("pinnedKnowledge")
  @Operation(summary = "获取置顶知识库列表")
  public ResultVO<List<PinnedKnowledgeDTO>> getPinnedKnowledge(@RequestParam Long spaceId) {
    return ResultVO.success(homepageService.getPinnedKnowledge(spaceId));
  }

  @PostMapping("pin")
  @Operation(summary = "设置首页置顶")
  public ResultVO<Long> pinResource(@RequestBody PinResourceRequestDTO request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    ResultVO<Long> result = homepageService.pinResource(request, userId);
    if (result.isSuccess()) {
      // 刷新缓存
      String cacheKey = userId + ":" + request.getSpaceId();
      if ("DOCUMENT".equals(request.getTargetType())) {
        pinnedDocumentCache.delete(cacheKey);
      }
      else if ("LIBRARY".equals(request.getTargetType())) {
        pinnedLibraryCache.delete(cacheKey);
      }
      else if ("KNOWLEDGE".equals(request.getTargetType())) {
        pinnedKnowledgeCache.delete(cacheKey);
      }
    }
    return result;
  }

  @PostMapping("unpin")
  @Operation(summary = "取消首页置顶")
  public ResultVO<Void> unpinResource(@RequestBody UnpinResourceRequestDTO request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    ResultVO<Void> result = homepageService.unpinResource(request, userId);
    if (result.isSuccess()) {
      // 刷新缓存
      String cacheKey = userId + ":" + request.getSpaceId();
      if ("DOCUMENT".equals(request.getTargetType())) {
        pinnedDocumentCache.delete(cacheKey);
      }
      else if ("LIBRARY".equals(request.getTargetType())) {
        pinnedLibraryCache.delete(cacheKey);
      }
      else if ("KNOWLEDGE".equals(request.getTargetType())) {
        pinnedKnowledgeCache.delete(cacheKey);
      }
    }
    return result;
  }

  @PostMapping("reorderPins")
  @Operation(summary = "调整首页置顶排序")
  public ResultVO<Void> reorderPins(@RequestBody ReorderPinsRequestDTO request) {
    Long userId = SessionUtil.getLoginInfo().getUserId();
    ResultVO<String> result = homepageService.reorderPins(request, userId);
    if (result.isSuccess()) {
      // 刷新缓存
      String cacheKey = userId + ":" + request.getSpaceId();
      String targetType = result.getResultObject();
      if ("DOCUMENT".equals(targetType)) {
        pinnedDocumentCache.delete(cacheKey);
      }
      else if ("LIBRARY".equals(targetType)) {
        pinnedLibraryCache.delete(cacheKey);
      }
      else if ("KNOWLEDGE".equals(targetType)) {
        pinnedKnowledgeCache.delete(cacheKey);
      }
    }
    return ResultVO.success();
  }

  @GetMapping("frequentLibraries")
  @Operation(summary = "获取常用文档库列表")
  public ResultVO<List<FrequentLibraryDTO>> getFrequentLibraries(@RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId,
    @RequestParam(name = "platform", required = false) String platform) {
    return ResultVO.success(homepageService.getFrequentLibraries(tenantId, spaceId, platform));
  }

  @GetMapping("recentFiles")
  @Operation(summary = "获取最近访问文件列表")
  public ResultVO<PageInfo<RecentFileDTO>> getRecentFiles(
    @Parameter(description = "页码，默认1，最大5") @RequestParam(value = "pageNum", defaultValue = "1") Integer page,
    @Parameter(description = "每页大小，固定10") @RequestParam(value = "pageSize", defaultValue = "10") Integer size,
    @Parameter(description = "排序方式：lastOpen-最近打开，lastModified-最近修改，默认lastOpen")
    @RequestParam(value = "sortBy", defaultValue = "lastOpen") String sortBy,
    @Parameter(description = "排序顺序：desc-倒序，asc-正序，默认desc")
    @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
    @Parameter(description = "文件类型过滤") @RequestParam(value = "fileType", required = false) String fileType,
    @Parameter(description = "创建者过滤：me-我创建的，others-他人创建的")
    @RequestParam(value = "creatorFilter", required = false) String creatorFilter,
    @RequestParam(value = "platform", required = false) String platform,
    @RequestParam(name = "tenantId") Long tenantId,
    @RequestParam(name = "spaceId") Long spaceId) {

    RecentFileQueryParams queryParams = new RecentFileQueryParams();
    queryParams.setPageNum(page);
    queryParams.setPageSize(size);
    queryParams.setSortBy(sortBy);
    queryParams.setSortOrder(sortOrder);
    queryParams.setFileType(fileType);
    queryParams.setCreatorFilter(creatorFilter);
    queryParams.setTenantId(tenantId);
    queryParams.setSpaceId(spaceId);
    queryParams.setPlatform(platform);

    return ResultVO.success(homepageService.getRecentFiles(queryParams));
  }

  @PostMapping("removeFromRecentFiles")
  @Operation(summary = "从最近访问列表中移除文件")
  public ResultVO<Void> removeFromRecentFiles(@RequestBody RemoveFromRecentFilesRequestDTO request) {
    Assert.notNull(request, "请求参数不能为空");
    Assert.notNull(request.getDocumentId(), "文档ID不能为空");
    Assert.notNull(request.getDeleteSourceAfter(), "删除源文件标志不能为空");
    return homepageService.removeFromRecentFiles(request.getDocumentId(), request.getDeleteSourceAfter());
  }

  @GetMapping("search/documents")
  @Operation(summary = "全局搜索文档")
  public ResultVO<PageInfo<SearchDocumentDTO>> searchDocuments(
    @Parameter(description = "搜索关键词") @RequestParam(value = "keyword", required = false) String keyword,
    @Parameter(description = "文档类型过滤") @RequestParam(value = "type", required = false) String type,
    @Parameter(description = "文档库ID过滤") @RequestParam(value = "libraryId", required = false) String libraryId,
    @Parameter(description = "空间ID过滤") @RequestParam(value = "spaceId", required = false) Long spaceId,
    @Parameter(description = "租户ID过滤") @RequestParam(value = "tenantId", required = false) Long tenantId,
    @Parameter(description = "是否为ai门户") @RequestParam(value = "platform", required = false) String platform,
    @Parameter(description = "页码，默认1") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
    @Parameter(description = "每页大小，默认20") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
    SearchDocumentQueryParams queryParams = new SearchDocumentQueryParams();
    queryParams.setKeyword(keyword);
    queryParams.setType(type);
    queryParams.setLibraryId(libraryId);
    queryParams.setPageNum(pageNum);
    queryParams.setPageSize(pageSize);
    queryParams.setSpaceId(spaceId);
    queryParams.setTenantId(tenantId);
    queryParams.setPlatform(platform);
    return ResultVO.success(homepageService.searchDocuments(queryParams));
  }

  @GetMapping("refreshCaches")
  @Operation(summary = "刷新首页置顶缓存")
  public ResultVO<String> refreshPinnedCaches(Long spaceId) {
    Long tenantId = TenantContextHolder.getRequiredTenantId();

    int documentCount = pinnedDocumentCache.clearTenantCache(tenantId, spaceId);
    int knowledgeCount = pinnedKnowledgeCache.clearTenantCache(tenantId, spaceId);
    int libraryCount = pinnedLibraryCache.clearTenantCache(tenantId, spaceId);

    String message = String.format("成功刷新缓存：置顶文档 %d 条，置顶知识库 %d 条，置顶文档库 %d 条", documentCount,
      knowledgeCount, libraryCount);

    return ResultVO.success(message);
  }
}
