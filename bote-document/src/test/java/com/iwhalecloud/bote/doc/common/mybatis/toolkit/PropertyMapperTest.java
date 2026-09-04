package com.iwhalecloud.bote.doc.common.mybatis.toolkit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * {@link PropertyMapper} 单元测试。
 *
 * <p>覆盖 newInstance/keys、whenNotBlank 两重载（值存在/缺失/空白、链式返回 this、function 转换）、
 * group 的无匹配键空 Map 与有匹配键分组（含子键写入与无子键空 Properties）。</p>
 */
class PropertyMapperTest {

  @Test
  void keys_returnsStringPropertyNames() {
    Properties p = new Properties();
    p.setProperty("a", "1");
    p.setProperty("b", "2");
    PropertyMapper mapper = PropertyMapper.newInstance(p);
    assertThat(mapper.keys()).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void whenNotBlank_consumer_presentValueInvokedAndChains() {
    Properties p = new Properties();
    p.setProperty("k", "v");
    List<String> captured = new ArrayList<>();
    PropertyMapper mapper = PropertyMapper.newInstance(p);
    assertThat(mapper.whenNotBlank("k", captured::add)).isSameAs(mapper);
    assertThat(captured).containsExactly("v");
  }

  @Test
  void whenNotBlank_consumer_blankOrAbsent_notInvoked() {
    Properties p = new Properties();
    p.setProperty("k", "  ");
    List<String> captured = new ArrayList<>();
    PropertyMapper.newInstance(p).whenNotBlank("k", captured::add);
    PropertyMapper.newInstance(p).whenNotBlank("missing", captured::add);
    assertThat(captured).isEmpty();
  }

  @Test
  void whenNotBlank_function_appliedAndConsumed() {
    Properties p = new Properties();
    p.setProperty("n", "42");
    List<Integer> captured = new ArrayList<>();
    PropertyMapper.newInstance(p).whenNotBlank("n", Integer::parseInt, captured::add);
    assertThat(captured).containsExactly(42);
  }

  @Test
  void whenNotBlank_function_absent_notInvoked() {
    Properties p = new Properties();
    List<Integer> captured = new ArrayList<>();
    PropertyMapper.newInstance(p).whenNotBlank("missing", Integer::parseInt, captured::add);
    assertThat(captured).isEmpty();
  }

  @Test
  void group_noMatchingKeys_returnsEmptyMap() {
    Properties p = new Properties();
    p.setProperty("x", "1");
    Map<String, Properties> result = PropertyMapper.newInstance(p).group("grp.");
    assertThat(result).isEmpty();
  }

  @Test
  void group_emptyPrefix_buildsGroupedMapWithSubKeys() {
    Properties p = new Properties();
    p.setProperty("x", "GroupA");
    p.setProperty("x:foo", "bar");
    p.setProperty("x:baz", "qux");
    Map<String, Properties> result = PropertyMapper.newInstance(p).group("");
    assertThat(result).containsKey("GroupA");
    assertThat(result.get("GroupA"))
      .containsEntry("foo", "bar")
      .containsEntry("baz", "qux");
  }

  @Test
  void group_matchingPrefixWithoutSubKeys_buildsEmptyProperties() {
    Properties p = new Properties();
    p.setProperty("grp.x", "GroupA");
    Map<String, Properties> result = PropertyMapper.newInstance(p).group("grp");
    assertThat(result).containsKey("GroupA");
    assertThat(result.get("GroupA")).isEmpty();
  }
}
