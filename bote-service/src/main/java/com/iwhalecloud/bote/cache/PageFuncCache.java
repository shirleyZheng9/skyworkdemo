package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimplePageFuncDTO;
import com.iwhalecloud.bote.dto.skill.SkillPageFuncDTO;
import com.iwhalecloud.bote.entity.skill.SkillPageFuncEntity;
import com.iwhalecloud.bote.mapper.skill.SkillPageFuncManageMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 页面函数缓存
 *
 * <p>缓存 key 为 tenantId:pageFuncId</p>
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Component
public final class PageFuncCache extends AbstractSkillCache<SimplePageFuncDTO> {
  private final SkillPageFuncManageMapper pageFuncMapper;

  public PageFuncCache(SkillPageFuncManageMapper pageFuncMapper) {
    super(CacheConsts.KEY_PREFIX_PAGE_FUNC);
    this.pageFuncMapper = pageFuncMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_PAGE_FUNC;
  }

  @Nullable
  @Override
  protected SimplePageFuncDTO loadById(Long tenantId, Long id) {
    SkillPageFuncDTO pageFunc = pageFuncMapper.selectSimplePageFunc(tenantId, id);
    return pageFunc == null ? null : SimplePageFuncDTO.from(pageFunc);
  }

  @Override
  protected Map<Long, SimplePageFuncDTO> loadByIds(Long tenantId, List<Long> ids) {
    return pageFuncMapper.selectSimplePageFuncList(tenantId, ids).stream()
      .collect(Collectors.toMap(SkillPageFuncEntity::getPageFuncId, SimplePageFuncDTO::from));
  }
}
