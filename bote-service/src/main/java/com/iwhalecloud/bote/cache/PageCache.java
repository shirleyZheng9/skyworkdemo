package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimplePageDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.entity.skill.SkillPageEntity;
import com.iwhalecloud.bote.mapper.skill.SkillPageManageMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 页面缓存
 *
 * <p>缓存 key 为 tenantId:pageId</p>
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Component
public final class PageCache extends AbstractSkillCache<SimplePageDTO> {
  private final SkillPageManageMapper pageMapper;

  public PageCache(SkillPageManageMapper pageMapper) {
    super(CacheConsts.KEY_PREFIX_PAGE);
    this.pageMapper = pageMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_PAGE;
  }

  @Nullable
  @Override
  protected SimplePageDTO loadById(Long tenantId, Long id) {
    SkillPageDTO page = pageMapper.selectSimplePage(tenantId, id);
    return page == null ? null : SimplePageDTO.from(page);
  }

  @Override
  protected Map<Long, SimplePageDTO> loadByIds(Long tenantId, List<Long> ids) {
    return pageMapper.selectSimplePages(tenantId, ids).stream()
      .collect(Collectors.toMap(SkillPageEntity::getPageId, SimplePageDTO::from));
  }
}
