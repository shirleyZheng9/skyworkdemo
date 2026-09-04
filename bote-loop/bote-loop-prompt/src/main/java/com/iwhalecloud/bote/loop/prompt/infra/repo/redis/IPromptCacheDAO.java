package com.iwhalecloud.bote.loop.prompt.infra.repo.redis;

import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.RedisPromptQuery;
import java.util.List;
import java.util.Map;

public interface IPromptCacheDAO {

  Map<RedisPromptQuery, Prompt> mGet(List<RedisPromptQuery> queries);

  void mSet(List<Prompt> prompts);

}
