package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.control.model.NodeBaseInfoDTO;
import com.iwhalecloud.bote.doc.module.control.service.DocumentNodeService;
import com.iwhalecloud.bote.doc.module.document.dto.NodePathDTO;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 文档库文档的路径缓存
 *
 * @author Aiqing
 * @since 2025/8/20
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DocumentPathCache extends BaseSecondaryCache<List<NodePathDTO>> {

  private static final Logger logger = LoggerFactory.getLogger(DocumentPathCache.class);

  private static final String LIBRARY_PREFIX = "library";

  private final DocumentNodeService documentNodeService;
  private final DcDocumentNodeCache dcDocumentNodeCache;

  public DocumentPathCache(DocumentNodeService documentNodeService,
    DcDocumentNodeCache dcDocumentNodeCache) {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_PREFIX_DOCUMENT_PATH);
    this.documentNodeService = documentNodeService;
    this.dcDocumentNodeCache = dcDocumentNodeCache;
    // 本地缓存1分钟
    this.localCacheExpireInMinutes = 1;
  }

  public String buildPathKey(String libraryId, String documentId) {
    return LIBRARY_PREFIX + DocCacheConsts.CACHE_KEY_SPLIT + libraryId + DocCacheConsts.CACHE_KEY_SPLIT + documentId;
  }

  /**
   * 获取文档路径
   *
   * @param documentId 文档ID
   * @return 文档路径
   */
  @Nullable
  public List<NodePathDTO> getPath(String documentId) {
    if (StringUtils.isBlank(documentId)) {
      return Collections.emptyList();
    }
    NodeBaseInfoDTO nodeInfo = dcDocumentNodeCache.getDocumentNodeInfo(documentId);
    if (nodeInfo == null) {
      return Collections.emptyList();
    }
    return super.get(buildPathKey(nodeInfo.getLibraryId(), documentId));
  }


  @Override
  protected List<NodePathDTO> load(String key) {
    if (StringUtils.isBlank(key)) {
      return Collections.emptyList();
    }
    String documentKey = extractOriginalKey(key);

    String libraryId = dcDocumentNodeCache.getLibraryIdByDocument(documentKey);
    if (StringUtils.isBlank(libraryId)) {
      return Collections.emptyList();
    }
    List<NodePathDTO> parentPathByNodeId = documentNodeService.getParentPathByNodeId(libraryId, documentKey);
    // 此处需要反转
    List<NodePathDTO> filteredList = parentPathByNodeId.stream()
      .filter(item -> !Objects.equals(documentKey, item.getNodeId()))
      .collect(Collectors.toList());
    Collections.reverse(filteredList);
    return filteredList;
  }

  /**
   * 按文档ID列表清除缓存
   *
   * @param documentIds 文档ID列表
   */
  public void clearCacheByKeys(String libraryId, List<String> documentIds) {
    if (documentIds.isEmpty()) {
      return;
    }
    List<String> cacheKeys = documentIds.stream()
      .map(item -> buildPathKey(libraryId, item)).collect(Collectors.toList());
    logger.info("清除文档路径缓存: documentCount={}", documentIds.size());
    clearCacheWithFullKeys(cacheKeys);
  }

  /**
   * 批量获取租户隔离的缓存数据
   *
   * @param keys 原始key列表
   * @return 缓存数据映射，key为原始key
   */
  @Override
  public Map<String, List<NodePathDTO>> mget(List<String> keys) {

    if (CollectionUtils.isEmpty(keys)) {
      return Collections.emptyMap();
    }
    List<String> cacheKeys = new ArrayList<>();
    for (String key : keys) {
      String libraryId = dcDocumentNodeCache.getLibraryIdByDocument(key);
      if (StringUtils.isNotBlank(libraryId)) {
        cacheKeys.add(buildPathKey(libraryId, key));
      }
    }
    if (CollectionUtils.isEmpty(cacheKeys)) {
      return Collections.emptyMap();
    }
    logger.info("查询文档路径缓存: documentCount={}", cacheKeys.size());
    Map<String, List<NodePathDTO>> tenantResult = super.mget(cacheKeys);

    // 将结果key转换回原始key
    return tenantResult.entrySet().stream()
      .collect(Collectors.toMap(entry -> extractOriginalKey(entry.getKey()), Map.Entry::getValue));
  }

  public void clearCacheWithFullKeys(List<String> cacheKeys) {
    // 清除分布式缓存
    if (useDistributionCache) {
      deleteDistributedCache(cacheKeys);
    }

    // 清除本地缓存
    if (useLocalCache) {
      localCache.invalidateAll(cacheKeys);
      IRefreshCacheService refreshCacheService = SpringUtil.getBeanOptional(IRefreshCacheService.class);
      if (refreshCacheService != null) {
        refreshCacheService.refresh(this.getCacheName(), cacheKeys);
      }
    }
  }

  /**
   * 按文档库ID清除缓存
   * 清除指定文档库下所有文档的路径缓存
   *
   * @param libraryId 文档库ID
   */
  public void clearCacheByLibraryId(String libraryId) {
    if (StringUtils.isBlank(libraryId)) {
      return;
    }
    logger.info("清除文档库路径缓存: libraryId={}", libraryId);
    // 获取该文档库下所有文档的缓存键
    // todo 需要优化
    Set<String> allKeys = getKeys();
    String libraryPrefix = LIBRARY_PREFIX + DocCacheConsts.CACHE_KEY_SPLIT + libraryId;
    List<String> libraryKeys = allKeys.stream()
      .filter(key -> key.startsWith(libraryPrefix))
      .collect(Collectors.toList());
    if (!libraryKeys.isEmpty()) {
      clearCacheWithFullKeys(libraryKeys);
      logger.info("清除文档库路径缓存完成: libraryId={}, clearedCount={}", libraryId, libraryKeys.size());
    }
  }

  /**
   * 从文档库隔离的缓存key中提取原始key
   *
   * @param cacheKey 带文档库的缓存key
   * @return 原始key
   */
  protected String extractOriginalKey(String cacheKey) {
    if (StringUtils.isBlank(cacheKey)) {
      return cacheKey;
    }

    // 格式: library:libraryId:originalKey
    String[] parts = cacheKey.split(DocCacheConsts.CACHE_KEY_SPLIT, 3);
    if (parts.length >= 3) {
      return parts[2];
    }
    return cacheKey;
  }
}
