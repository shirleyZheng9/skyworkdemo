package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.SceneDslUtil;
import com.iwhalecloud.bote.dto.orchestration.SceneDslDTO;
import com.iwhalecloud.bote.dto.scene.SimpleDslInfoDTO;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 复杂场景 DSL 缓存
 *
 * @author bianjp
 * @since 2024-12-10
 */
@Component
public class SceneDslCache extends AbstractSkillCache<SceneDslDTO> {
  private final SceneQueryMapper sceneQueryMapper;

  public SceneDslCache(SceneQueryMapper sceneQueryMapper) {
    super(CacheConsts.KEY_PREFIX_SCENE_DSL);
    this.sceneQueryMapper = sceneQueryMapper;
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SCENE_DSL;
  }

  /**
   * 获取 DSL
   */
  public SceneDslDTO getDsl(Long tenantId, Long sceneId, boolean useCache) {
    Assert.notNull(sceneId, "sceneId 不能为空");
    SceneDslDTO dsl = useCache ? get(tenantId, sceneId) : loadById(tenantId, sceneId);
    Assert.notNull(dsl, () -> "智能体不存在: sceneId=" + sceneId);
    return dsl;
  }

  @Override
  @Nullable
  protected SceneDslDTO loadById(Long tenantId, Long id) {
    SimpleDslInfoDTO info = sceneQueryMapper.selectDslBySceneId(tenantId, id);
    if (info == null) {
      return null;
    }
    Assert.hasLength(info.getDslJson(), () -> "智能体尚未编排: sceneId=" + id);
    Assert.isTrue(Boolean.TRUE.equals(info.getChatflow()), () -> "智能体类型不是流程模式: sceneId=" + id);
    SceneDslDTO dsl = SceneDslUtil.parse(info.getDslJson());
    dsl.setId(id);
    dsl.setCode(info.getCode());
    dsl.setName(info.getName());
    dsl.setChatflow(info.getChatflow());
    return dsl;
  }
}
