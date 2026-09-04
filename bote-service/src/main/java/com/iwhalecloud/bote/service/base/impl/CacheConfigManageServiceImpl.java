package com.iwhalecloud.bote.service.base.impl;

import com.iwhalecloud.bote.dto.base.CacheConfigDTO;
import com.iwhalecloud.bote.mapper.base.CacheConfigManageMapper;
import com.iwhalecloud.bote.service.base.ICacheConfigManageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 缓存配置服务
 *
 * @author chen.linfa
 * @since 2024-07-31
 */
@Service
@RequiredArgsConstructor
public class CacheConfigManageServiceImpl implements ICacheConfigManageService {
  private final CacheConfigManageMapper cacheConfigManagerMapper;

  @Override
  public List<CacheConfigDTO> queryAllCacheConfig() {
    return cacheConfigManagerMapper.queryAllCacheConfig();
  }
}
