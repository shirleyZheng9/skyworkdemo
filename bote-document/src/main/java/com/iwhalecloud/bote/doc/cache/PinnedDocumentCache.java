package com.iwhalecloud.bote.doc.cache;

import com.iwhalecloud.bote.doc.common.constant.DocBaseConsts;
import com.iwhalecloud.bote.doc.common.tenant.TenantContextHolder;
import com.iwhalecloud.bote.doc.consts.DocCacheConsts;
import com.iwhalecloud.bote.doc.module.person.dto.homepage.PinnedDocumentDTO;
import com.iwhalecloud.bote.doc.module.person.mapper.HomepageMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 置顶文档缓存
 *
 * @author lizuyin
 * @since 2025/08/19
 */
@Component
public class PinnedDocumentCache extends AbstractTenantCache<List<PinnedDocumentDTO>> {
  private final HomepageMapper homepageMapper;

  public PinnedDocumentCache(HomepageMapper homepageMapper) {
    super(DocCacheConsts.GROUP_DOC, DocCacheConsts.CACHE_NAME_PINNED_DOCUMENT);
    this.homepageMapper = homepageMapper;
  }

  @Override
  public String getCacheName() {
    return DocCacheConsts.CACHE_NAME_PINNED_DOCUMENT;
  }

  @Override
  @Nullable
  protected List<PinnedDocumentDTO> loadByKey(String key) {
    if (StringUtils.isEmpty(key)) {
      return null;
    }
    String[] parts = key.split(":");
    if (parts.length != 2) {
      return null;
    }
    Long userId = Long.valueOf(parts[0]);
    Long spaceId = Long.valueOf(parts[1]);
    Long tenantId = TenantContextHolder.getRequiredTenantId();
    return homepageMapper.selectPinnedDocuments(userId, spaceId, DocBaseConsts.STATUS_CD_VALID, tenantId);
  }

  @Override
  protected void loadAll(Consumer<Map<String, List<PinnedDocumentDTO>>> mapConsumer) {
    Objects.requireNonNull(mapConsumer);
    // 由于置顶文档是按用户ID查询的，全量加载时返回空Map
    mapConsumer.accept(Collections.emptyMap());
  }

  /**
   * 根据用户ID和空间ID查询置顶文档列表
   *
   * @param userId 用户ID
   * @param spaceId 空间ID
   * @return 置顶文档列表
   */
  @Nullable
  public List<PinnedDocumentDTO> getPinnedDocuments(Long userId, Long spaceId) {
    return get(userId + ":" + spaceId);
  }
}
