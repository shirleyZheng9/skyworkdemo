package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.impl;

import com.iwhalecloud.bote.entity.loop.prompt.PromptUserDraftEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDUserIDPair;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IPromptUserDraftDAO;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptUserDraftMapper;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Prompt用户草稿DAO实现
 * 迁移对应关系: Go语言mysql.PromptUserDraftDAOImpl
 * - 功能: Prompt用户草稿的数据访问实现
 * - 主要方法:
 * * create - 创建用户草稿
 * * delete - 删除用户草稿
 * * get - 获取用户草稿
 * * getByID - 根据ID获取用户草稿
 * * mGet - 批量获取用户草稿
 * * update - 更新用户草稿
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.PromptUserDraftDAOImpl结构体
 * - 使用原生MyBatis Mapper实现数据访问
 * - 支持复杂查询条件
 * - 集成写追踪机制
 */
@Repository
@RequiredArgsConstructor
public class PromptUserDraftDAOImpl implements IPromptUserDraftDAO {
  private final PromptUserDraftMapper promptUserDraftMapper;

  @Override
  public void create(PromptUserDraftEntity promptDraftPO) {
    if (promptDraftPO == null) {
      throw new IllegalArgumentException("promptDraftPO is empty");
    }
    promptDraftPO.setCreatedAt(new Date());
    promptDraftPO.setUpdatedAt(new Date());
    promptDraftPO.setDeletedAt(0L);
    promptUserDraftMapper.insert(promptDraftPO);
  }

  @Override
  public PromptUserDraftEntity get(Long spaceId, Long promptId, String userId) {
    if (spaceId == null) {
      throw new IllegalArgumentException("spaceID is invalid, spaceID is null");
    }
    if (promptId <= 0 || userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("promptID or userID is invalid param, promptID = " + promptId + ", userID = " + userId);
    }

    return promptUserDraftMapper.selectByPromptIdAndUserId(spaceId, promptId, userId);
  }

  @Override
  public PromptUserDraftEntity getById(Long draftId) {
    if (draftId <= 0) {
      throw new IllegalArgumentException("draftID is invalid, draftID = " + draftId);
    }

    return promptUserDraftMapper.selectById(draftId);
  }

  @Override
  public Map<PromptIDUserIDPair, PromptUserDraftEntity> mGet(List<PromptIDUserIDPair> pairs) {
    if (pairs == null || pairs.isEmpty()) {
      throw new IllegalArgumentException("PromptUserDraftDAOImpl.mGet invalid param");
    }

    List<PromptUserDraftEntity> results = promptUserDraftMapper.selectByPairs(pairs);
    return results.stream()
      .filter(po -> po != null)
      .collect(java.util.stream.Collectors.toMap(
        po -> new PromptIDUserIDPair(po.getPromptId(), po.getUserId()),
        po -> po
      ));
  }

  @Override
  public void update(PromptUserDraftEntity promptDraftPO) {
    if (promptDraftPO == null) {
      throw new IllegalArgumentException("promptDraftPO is empty");
    }

    promptUserDraftMapper.updateById(promptDraftPO);
  }

  @Override
  public void delete(Long draftId) {
    if (draftId <= 0) {
      throw new IllegalArgumentException("draftID is invalid, draftID = " + draftId);
    }

    promptUserDraftMapper.deleteById(draftId);
  }
}
