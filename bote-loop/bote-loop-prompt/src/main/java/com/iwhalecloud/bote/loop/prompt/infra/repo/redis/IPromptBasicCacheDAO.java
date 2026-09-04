// IPromptBasicDAO.java
package com.iwhalecloud.bote.loop.prompt.infra.repo.redis;

import com.iwhalecloud.bote.loop.prompt.domain.entity.Prompt;
import java.util.List;
import java.util.Map;

/**
 * Redis PromptBasic DAO接口
 * 对应Go中的IPromptBasicDAO接口
 */
public interface IPromptBasicCacheDAO {

  /**
   * 批量设置Prompt到缓存，通过PromptKey
   *
   * @param promptBasics Prompt列表
   * @return 操作结果
   */
  boolean mSetByPromptKey(List<Prompt> promptBasics);

  /**
   * 批量获取Prompt从缓存，通过PromptKey
   *
   * @param spaceId 空间ID
   * @param promptKeys PromptKey列表
   * @return Prompt映射表，key为promptKey，value为Prompt对象
   */
  Map<String, Prompt> mGetByPromptKey(Long spaceId, List<String> promptKeys);

  /**
   * 删除缓存中的Prompt，通过PromptKey
   *
   * @param spaceId 空间ID
   * @param promptKey PromptKey
   * @return 操作结果
   */
  boolean delByPromptKey(Long spaceId, String promptKey);
}
