package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bss.litchi.cache.helper.BaseLocalCache;
import com.iwhalecloud.bss.litchi.database.inspect.DatabaseInspector;
import com.iwhalecloud.bss.litchi.database.inspect.definition.Table;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 平台表结构定义缓存
 *
 * @author chen.linfa
 * @since 2024-10-22
 */
@Component
public class TableDefinitionCache extends BaseLocalCache<Table> {
  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_TABLE_DEFINITION;
  }

  @Nullable
  @Override
  protected Table load(String key) {
    if (StringUtils.isEmpty(key)) {
      return null;
    }
    return DatabaseInspector.inspectTable(key);
  }
}
