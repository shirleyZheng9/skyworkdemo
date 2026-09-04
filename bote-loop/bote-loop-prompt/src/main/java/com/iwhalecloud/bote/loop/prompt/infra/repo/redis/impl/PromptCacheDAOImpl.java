package com.iwhalecloud.bote.loop.prompt.infra.repo.redis.impl;

import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.RedisPromptQuery;
import com.iwhalecloud.bote.loop.prompt.infra.repo.redis.IPromptCacheDAO;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PromptCacheDAOImpl implements IPromptCacheDAO {
  private static final Logger logger = LoggerFactory.getLogger(PromptCacheDAOImpl.class);

  @Override
  public Map<RedisPromptQuery, Prompt> mGet(List<RedisPromptQuery> queries) {
    return null;
  }

  @Override
  public void mSet(List<Prompt> prompts) {
    // TODO实现与博特缓存对接
    logger.info("设置缓存,{}", prompts);
  }
}
