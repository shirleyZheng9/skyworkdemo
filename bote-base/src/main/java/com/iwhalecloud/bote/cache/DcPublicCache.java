package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.entity.base.DcPublicEntity;
import com.iwhalecloud.bote.mapper.base.DcPublicQueryMapper;
import com.iwhalecloud.bss.litchi.cache.helper.BaseSecondaryCache;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * dc_public 配置缓存
 *
 * @author qian.sisheng
 * @since 2024/8/8
 */
@Component
public class DcPublicCache extends BaseSecondaryCache<List<DcPublicEntity>> {
  private final DcPublicQueryMapper dcPublicQueryMapper;

  public DcPublicCache(DcPublicQueryMapper dcPublicQueryMapper) {
    super(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_DC_PUBLIC);
    this.dcPublicQueryMapper = dcPublicQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_DC_PUBLIC;
  }

  @Nullable
  @Override
  protected List<DcPublicEntity> load(String stype) {
    if (StringUtils.isEmpty(stype) || !NumberUtils.isCreatable(stype)) {
      return null;
    }
    return dcPublicQueryMapper.selectDcPublicListByStype(Long.valueOf(stype));
  }

  @Override
  protected void loadAll(Consumer<Map<String, List<DcPublicEntity>>> mapConsumer) {
    Objects.requireNonNull(mapConsumer);
    List<DcPublicEntity> list = dcPublicQueryMapper.selectDcPublicList();
    if (CollectionUtils.isEmpty(list)) {
      mapConsumer.accept(Collections.emptyMap());
    }
    else {
      Map<String, List<DcPublicEntity>> map = list.stream().collect(Collectors.groupingBy(d -> d.getStype().toString()));
      mapConsumer.accept(map);
    }
  }

  /**
   * 根据配置字典编码, 查询配置字典列表
   *
   * @param stype 配置字典编码
   * @return 配置字典列表
   */
  @Nullable
  public List<DcPublicEntity> getDcPublicList(String stype) {
    if (StringUtils.isEmpty(stype)) {
      return null;
    }
    return get(stype);
  }

  /**
   * 查询配置字典
   *
   * @param stype 配置字典编码
   * @return 配置字典，有多条时返回第一条
   */
  @Nullable
  public DcPublicEntity getDcPublic(String stype) {
    List<DcPublicEntity> dcPublicList = getDcPublicList(stype);
    return CollectionUtils.isNotEmpty(dcPublicList) ? dcPublicList.get(0) : null;
  }

  /**
   * 查询配置字典的 codea
   *
   * @param stype 配置字典编码
   * @return codea 值
   */
  @Nullable
  public String getCodea(String stype) {
    DcPublicEntity dcPublic = getDcPublic(stype);
    return dcPublic != null ? dcPublic.getCodea() : null;
  }
}
