package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql;

import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptBasicParam;
import java.util.List;
import java.util.Map;

public interface IPromptBasicDAO {

  void create(PromptBasicEntity basicPO);

  void delete(Long promptId, Long spaceId);

  PromptBasicEntity get(Long promptId, Long spaceId, boolean lock);

  Map<Long, PromptBasicEntity> mGet(Long spaceId, List<Long> promptIds);

  List<PromptBasicEntity> mGetByPromptKey(Long spaceId, List<String> promptKeys);

  boolean existsPromptKey(Long spaceId, String promptKey);

  boolean existsPromptName(Long spaceId, String promptName);

  List<PromptBasicEntity> list(ListPromptBasicParam param);

  /**
   * 根据条件统计Prompt数量
   *
   * @param param 查询参数
   * @return 总数
   */
  long countByCondition(ListPromptBasicParam param);

  void update(Long promptId, Long spaceId, PromptBasicEntity updatePromptBasicPO);

}
