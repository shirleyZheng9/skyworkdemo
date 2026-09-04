package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.orchestration.IfCondition;
import com.iwhalecloud.bote.dto.skill.ServiceMockDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceDTO;
import com.iwhalecloud.bote.dto.skill.SimpleServiceMockDTO;
import com.iwhalecloud.bote.dto.skill.SkillServiceDTO;
import com.iwhalecloud.bote.mapper.skill.ServiceMockManageMapper;
import com.iwhalecloud.bote.mapper.skill.SkillServiceManageMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * API 技能缓存
 *
 * <p>缓存 key 为 tenantId:serviceId</p>
 *
 * @author bianjp
 * @since 2024-11-05
 */
@Component
public final class ApiSkillCache extends AbstractSkillCache<SimpleServiceDTO> {
  private final SkillServiceManageMapper serviceMapper;
  private final ServiceMockManageMapper serviceMockManageMapper;

  public ApiSkillCache(SkillServiceManageMapper serviceMapper, ServiceMockManageMapper serviceMockManageMapper) {
    super(CacheConsts.KEY_PREFIX_API);
    this.serviceMapper = serviceMapper;
    this.serviceMockManageMapper = serviceMockManageMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_API;
  }

  @Override
  @Nullable
  protected SimpleServiceDTO loadById(Long tenantId, Long id) {
    SkillServiceDTO service = serviceMapper.selectSimpleServiceById(tenantId, id);
    if (service == null) {
      return null;
    }
    SimpleServiceDTO dto = SimpleServiceDTO.from(service);
    if (Boolean.TRUE.equals(dto.getMockEnabled())) {
      List<ServiceMockDTO> serviceMocks = serviceMockManageMapper.selectSimpleServiceMockList(tenantId, id);
      dto.setMocks(parseServiceMocks(serviceMocks));
    }
    return dto;
  }

  /**
   * 查询并解析服务的模拟报文
   */
  @Nullable
  private List<SimpleServiceMockDTO> parseServiceMocks(List<ServiceMockDTO> serviceMocks) {
    if (CollectionUtils.isEmpty(serviceMocks)) {
      return null;
    }
    List<SimpleServiceMockDTO> simpleMocks = new ArrayList<>(serviceMocks.size());
    for (ServiceMockDTO mock : serviceMocks) {
      SimpleServiceMockDTO dto = new SimpleServiceMockDTO();
      dto.setMockName(mock.getMockName());
      dto.setCondition(JsonUtil.parseJsonRequired(mock.getConditionJson(), IfCondition.class));
      if (StringUtils.isNotEmpty(mock.getRspJson())) {
        dto.setResponse(JsonUtil.parseJsonRequired(mock.getRspJson(), Object.class));
      }
      simpleMocks.add(dto);
    }
    return simpleMocks;
  }

  @Override
  protected Map<Long, SimpleServiceDTO> loadByIds(Long tenantId, List<Long> ids) {
    List<SimpleServiceDTO> services = serviceMapper.selectSimpleServiceByIds(tenantId, ids).stream()
      .map(SimpleServiceDTO::from)
      .collect(Collectors.toList());
    if (services.isEmpty()) {
      return Collections.emptyMap();
    }
    // 开启了接口模拟的服务 ID 列表
    List<Long> mockServiceIds = services.stream()
      .filter(s -> Boolean.TRUE.equals(s.getMockEnabled()))
      .map(SimpleServiceDTO::getServiceId)
      .collect(Collectors.toList());
    // 批量查询模拟响应
    if (!mockServiceIds.isEmpty()) {
      List<ServiceMockDTO> serviceMocks = serviceMockManageMapper.selectSimpleServiceMockListByIds(tenantId, mockServiceIds);
      for (SimpleServiceDTO service : services) {
        if (Boolean.TRUE.equals(service.getMockEnabled())) {
          service.setMocks(parseServiceMocks(ListUtils.select(serviceMocks, s -> service.getServiceId().equals(s.getServiceId()))));
        }
      }
    }
    return services.stream().collect(Collectors.toMap(SimpleServiceDTO::getServiceId, Function.identity()));
  }
}
