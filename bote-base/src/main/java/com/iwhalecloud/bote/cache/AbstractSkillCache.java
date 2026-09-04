package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 技能缓存抽象类
 *
 * <p>修改技能配置时会自动按 key 刷新缓存，默认会加载数据到缓存中，如果频繁修改而不使用将会产生不必要的开销，这里改为刷新缓存时只删除不重新加载。</p>
 *
 * @author bianjp
 * @since 2024-12-17
 */
public abstract class AbstractSkillCache<T> extends AbstractTenantCache<T> {
  public AbstractSkillCache(String keyPrefix) {
    super(keyPrefix);
  }

  /**
   * 根据 ID 获取缓存对象
   */
  @Nullable
  public T get(Long tenantId, Long id) {
    String key = tenantId + CacheConsts.COLON + id;
    return get(key);
  }

  /**
   * 批量获取缓存对象
   *
   * <p>所有技能都必须存在，有不存在的会抛异常</p>
   */
  public List<T> batchGet(Long tenantId, List<Long> ids) {
    Assert.notEmpty(ids, "ids 不能为空");
    // 只有一个 id 时使用查询单个的方法
    if (ids.size() == 1) {
      T skill = get(tenantId, ids.get(0));
      Assert.notNull(skill, () -> "技能不存在: type=" + getCacheName() + ", id=" + ids.get(0));
      return Collections.singletonList(skill);
    }

    Map<Long, T> skillMap = new HashMap<>();
    // 本地缓存不存在的 id
    List<Long> missingIds = new ArrayList<>();
    String prefix = tenantId + ":";
    // 先从本地缓存获取
    for (Long id : ids) {
      T value = localCache.getIfPresent(prefix + id);
      if (value != null) {
        skillMap.put(id, value);
      }
      else {
        missingIds.add(id);
      }
    }
    // 本地缓存不存在的，从数据库批量加载
    if (!missingIds.isEmpty()) {
      Map<Long, T> map = loadByIds(tenantId, missingIds);
      skillMap.putAll(map);
      // 写入本地缓存
      for (Entry<Long, T> entry : map.entrySet()) {
        localCache.put(prefix + entry.getKey(), entry.getValue());
      }
    }

    // 按 id 顺序返回，并检查技能是否存在
    List<T> skills = new ArrayList<>(ids.size());
    for (Long id : ids) {
      T skill = skillMap.get(id);
      Assert.notNull(skill, () -> "技能不存在: type=" + getCacheName() + ", id=" + id);
      skills.add(skill);
    }
    return skills;
  }

  @Override
  @Nullable
  protected final T load(String key) {
    String[] pieces = StringUtils.split(key, ':');
    if (pieces == null || pieces.length != 2 || !NumberUtils.isCreatable(pieces[0]) || !NumberUtils.isCreatable(pieces[1])) {
      return null;
    }
    Long tenantId = Long.parseLong(pieces[0]);
    Long id = Long.parseLong(pieces[1]);
    return loadById(tenantId, id);
  }

  /**
   * 根据 ID 加载数据
   */
  @Nullable
  protected abstract T loadById(Long tenantId, Long id);

  /**
   * 根据 ID 列表批量加载
   */
  protected Map<Long, T> loadByIds(Long tenantId, List<Long> ids) {
    // 部分缓存用不到批量加载，可以不实现
    throw new UnsupportedOperationException("缓存 " + getCacheName() + " 不支持批量加载");
  }

}
