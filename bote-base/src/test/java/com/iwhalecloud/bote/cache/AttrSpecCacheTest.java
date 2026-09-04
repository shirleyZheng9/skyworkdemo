package com.iwhalecloud.bote.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.dto.base.SimpleAttrDTO;
import com.iwhalecloud.bote.dto.base.SimpleAttrValueRelDTO;
import com.iwhalecloud.bote.mapper.base.AttrQueryMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link AttrSpecCache} 单元测试。
 *
 * <p>反射关闭 BaseSecondaryCache 后端，使 get()->load()->put() 仅经 mapper。覆盖 load 的空/格式非法/
 * 合法空列表/带关联递归分支、get 的租户级未命中回退平台级、loadAll 的空/分组。</p>
 */
class AttrSpecCacheTest {

  private AttrQueryMapper mapper;
  private AttrSpecCache cache;

  @BeforeEach
  void setUp() throws Exception {
    mapper = mock(AttrQueryMapper.class);
    cache = new AttrSpecCache(mapper);
    CacheTestSupport.disableCacheBackends(cache);
  }

  @Test
  void getCacheName_returnsConstant() {
    assertThat(cache.getCacheName()).isEqualTo(CacheConsts.CACHE_NAME_ATTR_SPEC);
  }

  // ==================== load ====================

  @Test
  void load_emptyKey_returnsNull() {
    assertThat(cache.load("")).isNull();
  }

  @Test
  void load_noColon_returnsNull() {
    assertThat(cache.load("abc")).isNull();
  }

  @Test
  void load_twoColons_returnsNull() {
    assertThat(cache.load("1:2:3")).isNull();
  }

  @Test
  void load_validButMapperEmpty_returnsEmptyList() {
    when(mapper.selectAttrValueByAttrCode(10L, "code")).thenReturn(Collections.emptyList());
    assertThat(cache.load("10:code")).isEmpty();
  }

  @Test
  void load_validWithNoRels_returnsList() {
    SimpleAttrDTO attr = new SimpleAttrDTO();
    attr.setAttrValueId(100L);
    when(mapper.selectAttrValueByAttrCode(10L, "code")).thenReturn(List.of(attr));
    // 未配置 rel 桩 -> selectAttrValueRelList 返回 null -> getAttrRelList 提前返回
    List<SimpleAttrDTO> result = cache.load("10:code");
    assertThat(result).containsExactly(attr);
    assertThat(attr.getAttrRelList()).isNull();
  }

  @Test
  void load_validWithMatchingRel_buildsAttrRelList() {
    SimpleAttrDTO attr = new SimpleAttrDTO();
    attr.setAttrValueId(100L);
    SimpleAttrValueRelDTO rel = new SimpleAttrValueRelDTO();
    rel.setAAttrValueId(100L);
    rel.setZAttrValueId(200L);
    rel.setZAttrValue("child-val");
    rel.setZAttrValueName("child-name");
    rel.setZAttrNbr("child-code");
    rel.setZAttrName("child-attr");
    rel.setZAttrId(9L);
    rel.setTenantId(10L);

    when(mapper.selectAttrValueByAttrCode(10L, "code")).thenReturn(List.of(attr));
    // 首次按 [100] 查询返回 rel；递归按 [200] 查询返回空，终止递归
    when(mapper.selectAttrValueRelList(eq(10L), argThat(l -> l != null && l.contains(100L))))
      .thenReturn(List.of(rel));
    when(mapper.selectAttrValueRelList(eq(10L), argThat(l -> l != null && l.contains(200L))))
      .thenReturn(Collections.emptyList());

    List<SimpleAttrDTO> result = cache.load("10:code");
    assertThat(result).containsExactly(attr);
    assertThat(attr.getAttrRelList()).hasSize(1);
    SimpleAttrDTO child = attr.getAttrRelList().get(0);
    assertThat(child.getAttrValueId()).isEqualTo(200L);
    assertThat(child.getAttrValue()).isEqualTo("child-val");
    assertThat(child.getAttrCode()).isEqualTo("child-code");
    // 递归终止：孙级 selectAttrValueRelList 返回空，命中 isEmpty 提前返回，attrRelList 保持 null
    assertThat(child.getAttrRelList()).isNull();
  }

  @Test
  void load_validWithNonMatchingRel_leavesAttrRelListEmpty() {
    SimpleAttrDTO attr = new SimpleAttrDTO();
    attr.setAttrValueId(100L);
    SimpleAttrValueRelDTO unrelated = new SimpleAttrValueRelDTO();
    unrelated.setAAttrValueId(999L); // 不匹配 attr 的 100

    when(mapper.selectAttrValueByAttrCode(10L, "code")).thenReturn(List.of(attr));
    when(mapper.selectAttrValueRelList(any(), anyList())).thenReturn(List.of(unrelated));

    List<SimpleAttrDTO> result = cache.load("10:code");
    assertThat(result).containsExactly(attr);
    assertThat(attr.getAttrRelList()).isEmpty();
  }

  // ==================== get(tenantId, attrCode) ====================

  @Test
  void get_tenantLevelHit_returnsListWithoutFallback() {
    SimpleAttrDTO attr = new SimpleAttrDTO();
    when(mapper.selectAttrValueByAttrCode(10L, "code")).thenReturn(List.of(attr));
    assertThat(cache.get(10L, "code")).containsExactly(attr);
  }

  @Test
  void get_tenantLevelMiss_fallsBackToPlatform() {
    SimpleAttrDTO platformAttr = new SimpleAttrDTO();
    when(mapper.selectAttrValueByAttrCode(10L, "code")).thenReturn(Collections.emptyList());
    when(mapper.selectAttrValueByAttrCode(CommonConsts.PLATFORM_TENANT_ID, "code"))
      .thenReturn(List.of(platformAttr));
    assertThat(cache.get(10L, "code")).containsExactly(platformAttr);
  }

  @Test
  void get_platformTenant_doesNotFallback() {
    // 租户即平台租户时，即便为空也不回退
    when(mapper.selectAttrValueByAttrCode(CommonConsts.PLATFORM_TENANT_ID, "code"))
      .thenReturn(Collections.emptyList());
    assertThat(cache.get(CommonConsts.PLATFORM_TENANT_ID, "code")).isEmpty();
  }

  // ==================== loadAll ====================

  @Test
  void loadAll_emptyList_acceptsEmptyMap() {
    when(mapper.selectAllAttrValue(CommonConsts.PLATFORM_TENANT_ID)).thenReturn(Collections.emptyList());
    Map<String, List<SimpleAttrDTO>> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, List<SimpleAttrDTO>>>) captured::putAll);
    assertThat(captured).isEmpty();
  }

  @Test
  void loadAll_nullList_acceptsEmptyMap() {
    when(mapper.selectAllAttrValue(CommonConsts.PLATFORM_TENANT_ID)).thenReturn(null);
    Map<String, List<SimpleAttrDTO>> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, List<SimpleAttrDTO>>>) captured::putAll);
    assertThat(captured).isEmpty();
  }

  @Test
  void loadAll_nonEmptyList_groupedByTenantAndAttrCode() {
    SimpleAttrDTO a = new SimpleAttrDTO();
    a.setTenantId(10L);
    a.setAttrCode("c1");
    SimpleAttrDTO b = new SimpleAttrDTO();
    b.setTenantId(CommonConsts.PLATFORM_TENANT_ID);
    b.setAttrCode("c2");
    when(mapper.selectAllAttrValue(CommonConsts.PLATFORM_TENANT_ID)).thenReturn(List.of(a, b));

    Map<String, List<SimpleAttrDTO>> captured = new HashMap<>();
    cache.loadAll((Consumer<Map<String, List<SimpleAttrDTO>>>) captured::putAll);

    assertThat(captured).hasSize(2)
      .containsEntry("10:c1", List.of(a))
      .containsEntry(CommonConsts.PLATFORM_TENANT_ID + ":c2", List.of(b));
  }
}
