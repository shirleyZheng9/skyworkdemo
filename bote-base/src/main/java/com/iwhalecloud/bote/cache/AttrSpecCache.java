package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.base.SimpleAttrValueRelDTO;
import com.iwhalecloud.bote.mapper.base.AttrQueryMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 静态数据缓存
 *
 * @author chen.linfa
 * @since 2024-07-30
 */
@Component
public class AttrSpecCache extends AbstractTenantCache<List<SimpleAttrDTO>> {
  private final AttrQueryMapper attrQueryMapper;

  public AttrSpecCache(AttrQueryMapper attrQueryMapper) {
    super(CacheConsts.KEY_PREFIX_ATTR_SPEC);
    this.attrQueryMapper = attrQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_ATTR_SPEC;
  }

  @Nullable
  public List<SimpleAttrDTO> get(Long tenantId, String attrCode) {
    String key = tenantId + CacheConsts.COLON + attrCode;
    List<SimpleAttrDTO> list = get(key);
    if (CollectionUtils.isEmpty(list) && !CommonConsts.PLATFORM_TENANT_ID.equals(tenantId)) {
      // 查询租户级静态数据
      key = CommonConsts.PLATFORM_TENANT_ID + CacheConsts.COLON + attrCode;
      list = get(key);
    }
    return list;
  }

  @Nullable
  @Override
  protected List<SimpleAttrDTO> load(String key) {
    if (StringUtils.isEmpty(key) || StringUtils.countMatches(key, CacheConsts.COLON) != 1) {
      return null;
    }
    List<String> keys = Arrays.asList(key.split(CacheConsts.COLON));
    Long tenantId = Long.parseLong(keys.get(0));
    String attrCode = keys.get(1);
    List<SimpleAttrDTO> simpleAttrList = attrQueryMapper.selectAttrValueByAttrCode(tenantId, attrCode);
    if (CollectionUtils.isEmpty(simpleAttrList)) {
      return Collections.emptyList();
    }
    getAttrRelList(tenantId, simpleAttrList);
    return simpleAttrList;
  }

  private void getAttrRelList(@Nullable Long tenantId, List<SimpleAttrDTO> simpleAttrList) {
    List<SimpleAttrValueRelDTO> attrValueRelList;
    if (tenantId != null) {
      List<Long> valueIdList = simpleAttrList.stream().map(SimpleAttrDTO::getAttrValueId).collect(Collectors.toList());
      attrValueRelList = attrQueryMapper.selectAttrValueRelList(tenantId, valueIdList);
    }
    else {
      attrValueRelList = attrQueryMapper.selectAttrValueRelList(null, null);
    }
    if (CollectionUtils.isEmpty(attrValueRelList)) {
      return;
    }
    for (SimpleAttrDTO attr : simpleAttrList) {
      List<SimpleAttrValueRelDTO> collect = attrValueRelList.stream().filter(v -> v.getAAttrValueId().equals(attr.getAttrValueId())).toList();
      List<SimpleAttrDTO> attrRelList = new ArrayList<>();
      for (SimpleAttrValueRelDTO skillAttrValueRel : collect) {
        SimpleAttrDTO simpleAttr = getSimpleAttr(skillAttrValueRel);
        attrRelList.add(simpleAttr);
      }
      // 递归查询下一级
      if (CollectionUtils.isNotEmpty(attrRelList)) {
        getAttrRelList(tenantId, attrRelList);
      }
      attr.setAttrRelList(attrRelList);
    }
  }

  private SimpleAttrDTO getSimpleAttr(SimpleAttrValueRelDTO skillAttrValueRel) {
    SimpleAttrDTO simpleAttr = new SimpleAttrDTO();
    simpleAttr.setAttrValueId(skillAttrValueRel.getZAttrValueId());
    simpleAttr.setAttrValue(skillAttrValueRel.getZAttrValue());
    simpleAttr.setAttrValueName(skillAttrValueRel.getZAttrValueName());
    simpleAttr.setAttrCode(skillAttrValueRel.getZAttrNbr());
    simpleAttr.setAttrName(skillAttrValueRel.getZAttrName());
    simpleAttr.setAttrId(skillAttrValueRel.getZAttrId());
    simpleAttr.setTenantId(skillAttrValueRel.getTenantId());
    return simpleAttr;
  }

  @Override
  protected void loadAll(Consumer<Map<String, List<SimpleAttrDTO>>> mapConsumer) {
    // 全量刷新时，只加载平台级的静态数据。租户级的静态数据可能数据量过大，且不一定常用，不适合全量加载
    List<SimpleAttrDTO> list = attrQueryMapper.selectAllAttrValue(CommonConsts.PLATFORM_TENANT_ID);
    if (CollectionUtils.isEmpty(list)) {
      mapConsumer.accept(Collections.emptyMap());
    }
    else {
      getAttrRelList(CommonConsts.PLATFORM_TENANT_ID, list);
      Map<String, List<SimpleAttrDTO>> map = list.stream()
        .collect(Collectors.groupingBy(p -> p.getTenantId() + CacheConsts.COLON + p.getAttrCode()));
      mapConsumer.accept(map);
    }
  }

}
