package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimpleFunctionSkillDTO;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.entity.skill.SkillFunctionEntity;
import com.iwhalecloud.bote.mapper.skill.SkillFunctionManageMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * 工具箱缓存
 *
 * <p>缓存 key 为 tenantId:funcId</p>
 *
 * @author bianjp
 * @since 2024-12-18
 */
@Component
public final class ToolboxCache extends AbstractSkillCache<SimpleFunctionSkillDTO> {
  private final SkillFunctionManageMapper functionMapper;

  public ToolboxCache(SkillFunctionManageMapper functionMapper) {
    super(CacheConsts.KEY_PREFIX_TOOLBOX);
    this.functionMapper = functionMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_TOOLBOX;
  }

  /**
   * 获取工具箱，支持获取平台级数据
   */
  @Nullable
  public SimpleFunctionSkillDTO getFunction(Long tenantId, Long id) {
    SimpleFunctionSkillDTO function = get(tenantId, id);
    if (function == null) {
      function = get(BaseConsts.PLATFORM_TENANT_ID, id);
    }
    return function;
  }

  @Nullable
  @Override
  protected SimpleFunctionSkillDTO loadById(Long tenantId, Long id) {
    SkillFunctionDTO function = functionMapper.selectSimpleFunction(tenantId, id);
    return function == null ? null : SimpleFunctionSkillDTO.from(function);
  }

  @Override
  protected Map<Long, SimpleFunctionSkillDTO> loadByIds(Long tenantId, List<Long> ids) {
    return functionMapper.selectSimpleFunctions(tenantId, ids).stream()
      .collect(Collectors.toMap(SkillFunctionEntity::getFuncId, SimpleFunctionSkillDTO::from));
  }
}
