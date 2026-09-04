package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.entity.bot.BotSceneEntity;
import com.iwhalecloud.bote.mapper.scene.SceneQueryMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 智能体图标缓存
 *
 * @author bianjp
 * @since 2025-06-16
 */
@Component
public final class SceneIconCache extends AbstractIconCache {
  private final SceneQueryMapper sceneQueryMapper;
  /** A2A 服务默认图标。图标很小，可以直接放在内存中 */
  private final byte[] defaultA2aIcon;

  public SceneIconCache(SceneQueryMapper sceneQueryMapper) throws IOException {
    super("智能体图标", new ClassPathResource("assets/images/default-scene-icon.png"));
    this.sceneQueryMapper = sceneQueryMapper;
    try (InputStream inputStream = new ClassPathResource("assets/images/default-a2a-icon.png").getInputStream()) {
      defaultA2aIcon = IOUtils.toByteArray(inputStream);
    }
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SCENE_ICON;
  }

  @Override
  protected Pair<String, byte[]> loadIcon(Long tenantId, Long id) {
    BotSceneEntity scene = sceneQueryMapper.selectSceneIcon(tenantId, id);
    if (scene == null) {
      throw new BssException("智能体不存在");
    }
    // A2A 服务使用特殊的默认图标
    if (StringUtils.isEmpty(scene.getSceneIcon()) && SceneConsts.SCENE_TYPE_A2A.equals(scene.getSceneType())) {
      return Pair.of("image/png", defaultA2aIcon);
    }
    return parseBase64Icon(id, scene.getSceneIcon());
  }

  @Override
  protected String loadIconContent(Long tenantId, Long id) {
    throw new UnsupportedOperationException();
  }
}
