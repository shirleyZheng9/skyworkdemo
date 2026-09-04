package com.iwhalecloud.bote.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.dto.base.SimpleFlowStepDTO;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.scene.SimpleDslInfoDTO;
import com.iwhalecloud.bote.mapper.skill.FlowQueryMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 工作流 DSL 缓存
 *
 * @author bianjp
 * @since 2024-12-10
 */
@Component
public class FlowDslCache extends AbstractSkillCache<SceneDslDTO> {
  private final FlowQueryMapper flowQueryMapper;

  public FlowDslCache(FlowQueryMapper flowQueryMapper) {
    super(CacheConsts.KEY_PREFIX_FLOW_DSL);
    this.flowQueryMapper = flowQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_FLOW_DSL;
  }

  /**
   * 获取 DSL
   */
  public SceneDslDTO getDsl(Long tenantId, Long flowId, boolean useCache) {
    Assert.notNull(flowId, "flowId 不能为空");
    SceneDslDTO dsl = useCache ? get(tenantId, flowId) : loadById(tenantId, flowId);
    Assert.notNull(dsl, () -> "流程不存在: flowId=" + flowId);
    return dsl;
  }

  @Override
  @Nullable
  protected SceneDslDTO loadById(Long tenantId, Long flowId) {
    SimpleDslInfoDTO info = flowQueryMapper.selectDslByFlowId(tenantId, flowId);
    if (info == null) {
      return null;
    }
    return buildDsl(info);
  }

  @Override
  protected Map<Long, SceneDslDTO> loadByIds(Long tenantId, List<Long> ids) {
    return flowQueryMapper.selectDslByFlowIds(tenantId, ids).stream()
      .collect(Collectors.toMap(SimpleDslInfoDTO::getId, this::buildDsl));
  }

  /**
   * 构造 DSL 对象
   */
  private SceneDslDTO buildDsl(SimpleDslInfoDTO info) {
    Assert.hasLength(info.getDslJson(), () -> "流程尚未编排: flowId=" + info.getId());
    SceneDslDTO dsl = SceneDslUtil.parse(info.getDslJson());
    dsl.setId(info.getId());
    dsl.setCode(info.getCode());
    dsl.setName(info.getName());
    dsl.setChatflow(info.getChatflow());
    if (StringUtils.isNotEmpty(info.getFlowStepJson())) {
      dsl.setFlowSteps(JsonUtil.parseJson(info.getFlowStepJson(), new TypeReference<List<SimpleFlowStepDTO>>() {
      }));
    }
    return dsl;
  }
}
