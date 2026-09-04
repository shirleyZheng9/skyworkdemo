package com.iwhalecloud.bote.mapper.base;

import com.iwhalecloud.bote.dto.base.CacheConfigDTO;
import java.util.List;

/**
 * 缓存配置管理 Mapper
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
public interface CacheConfigManageMapper {

  /**
   * 获取缓存配置列表
   *
   * @return 缓存配置列表
   */
  List<CacheConfigDTO> queryAllCacheConfig();
}
