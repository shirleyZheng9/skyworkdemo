package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.dto.skill.SimpleServiceGatewayDTO;
import com.iwhalecloud.bote.mapper.skill.ServiceGatewayManageMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 网关缓存
 * <p>缓存 key 为 tenantId:platformId</p>
 *
 * @author bianjp
 * @since 2024-11-05
 */
@Component
public final class GatewayCache extends AbstractSkillCache<SimpleServiceGatewayDTO> {
  private final ServiceGatewayManageMapper gatewayMapper;

  public GatewayCache(ServiceGatewayManageMapper gatewayMapper) {
    super(CacheConsts.KEY_PREFIX_GATEWAY);
    this.gatewayMapper = gatewayMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_GATEWAY;
  }

  @Override
  @Nullable
  protected SimpleServiceGatewayDTO loadById(Long tenantId, Long id) {
    SimpleServiceGatewayDTO gateway = gatewayMapper.selectSimpleGatewayByPlatformIdAndEnvCode(tenantId, id, EnvUtil.getEnvCode());
    // 提前解析响应头，避免使用时重复解析
    if (gateway != null) {
      gateway.parse();
    }
    return gateway;
  }

}
