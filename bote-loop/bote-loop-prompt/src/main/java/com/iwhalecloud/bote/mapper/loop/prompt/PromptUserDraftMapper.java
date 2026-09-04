package com.iwhalecloud.bote.mapper.loop.prompt;

import com.iwhalecloud.bote.entity.loop.prompt.PromptUserDraftEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDUserIDPair;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Prompt用户草稿Mapper接口
 * 对应Go代码中的PromptUserDraft表操作
 */
@Mapper
public interface PromptUserDraftMapper {

  /**
   * 插入Prompt用户草稿
   *
   * @param promptDraftPO Prompt用户草稿对象
   * @return 影响行数
   */
  int insert(PromptUserDraftEntity promptDraftPO);

  /**
   * 根据空间ID、PromptID和用户ID查询草稿
   *
   * @param spaceId 空间ID
   * @param promptId Prompt ID
   * @param userId 用户ID
   * @return Prompt用户草稿
   */
  PromptUserDraftEntity selectByPromptIdAndUserId(@Param("spaceId") Long spaceId,
                                                  @Param("promptId") Long promptId,
                                                  @Param("userId") String userId);

  /**
   * 根据ID查询草稿
   *
   * @param draftId 草稿ID
   * @return Prompt用户草稿
   */
  PromptUserDraftEntity selectById(@Param("draftId") Long draftId);

  /**
   * 根据条件对查询草稿
   *
   * @param pairs PromptID和用户ID对列表
   * @return Prompt用户草稿列表
   */
  List<PromptUserDraftEntity> selectByPairs(@Param("pairs") List<PromptIDUserIDPair> pairs);

  /**
   * 根据ID更新草稿
   *
   * @param promptDraftPO Prompt用户草稿对象
   * @return 影响行数
   */
  int updateById(PromptUserDraftEntity promptDraftPO);

  /**
   * 根据ID删除草稿
   *
   * @param draftId 草稿ID
   * @return 影响行数
   */
  int deleteById(@Param("draftId") Long draftId);
}
