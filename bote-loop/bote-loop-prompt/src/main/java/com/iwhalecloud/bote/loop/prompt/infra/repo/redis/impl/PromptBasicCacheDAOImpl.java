package com.iwhalecloud.bote.loop.prompt.infra.repo.redis.impl;

import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.infra.repo.redis.IPromptBasicCacheDAO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PromptBasicCacheDAOImpl implements IPromptBasicCacheDAO {

  @Override
  public boolean mSetByPromptKey(List<Prompt> promptBasics) {
    return false;
  }

  @Override
  public Map<String, Prompt> mGetByPromptKey(Long spaceId, List<String> promptKeys) {
    return Collections.emptyMap();
  }

  @Override
  public boolean delByPromptKey(Long spaceId, String promptKey) {
    return false;
  }
}
