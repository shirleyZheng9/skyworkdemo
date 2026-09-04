package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.impl;

import com.iwhalecloud.bote.entity.loop.prompt.PromptBasicEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListPromptBasicParam;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IPromptBasicDAO;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptBasicMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * Prompt基础信息DAO实现
 * 迁移对应关系: Go语言mysql.PromptBasicDAOImpl
 * - 功能: Prompt基础信息的数据访问实现
 * - 主要方法:
 * * create - 创建Prompt
 * * delete - 删除Prompt
 * * get - 获取Prompt
 * * mGet - 批量获取Prompt
 * * mGetByPromptKey - 根据PromptKey批量获取
 * * list - 分页查询Prompt列表
 * * update - 更新Prompt
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.PromptBasicDAOImpl结构体
 * - 使用原生MyBatis Mapper实现数据访问
 * - 支持分页查询和复杂条件查询
 * - 集成缓存和写追踪机制
 */
@Repository
@RequiredArgsConstructor
public class PromptBasicDAOImpl implements IPromptBasicDAO {
  private final PromptBasicMapper promptBasicMapper;

  @Override
  public void create(PromptBasicEntity basicPO) {
    if (basicPO == null) {
      throw new IllegalArgumentException("basicPO is empty");
    }

    promptBasicMapper.insert(basicPO);
  }

  @Override
  public void delete(Long promptId, Long spaceId) {
    if (promptId <= 0) {
      throw new IllegalArgumentException("promptID is invalid, promptID = " + promptId);
    }
    if (spaceId == null) {
      throw new IllegalArgumentException("spaceID is invalid, spaceID is null");
    }

    promptBasicMapper.deleteById(promptId, spaceId);
  }

  @Override
  public PromptBasicEntity get(Long promptId, Long spaceId, boolean lock) {
    if (promptId <= 0) {
      throw new IllegalArgumentException("promptID is invalid, promptID = " + promptId);
    }
    if (spaceId == null) {
      throw new IllegalArgumentException("spaceID is invalid, spaceID is null");
    }

    return promptBasicMapper.selectById(promptId, spaceId, lock);
  }

  @Override
  public Map<Long, PromptBasicEntity> mGet(Long spaceId, List<Long> promptIds) {
    if (spaceId == null) {
      throw new IllegalArgumentException("spaceID is invalid, spaceID is null");
    }
    if (promptIds == null || promptIds.isEmpty()) {
      throw new IllegalArgumentException("PromptBasicDAOImpl.mGet invalid param");
    }

    List<PromptBasicEntity> results = promptBasicMapper.selectByIds(spaceId, promptIds);
    return results.stream()
      .collect(java.util.stream.Collectors.toMap(
        PromptBasicEntity::getId,
        po -> po
      ));
  }

  @Override
  public List<PromptBasicEntity> mGetByPromptKey(Long spaceId, List<String> promptKeys) {
    if (promptKeys == null || promptKeys.isEmpty()) {
      throw new IllegalArgumentException("PromptBasicDAOImpl.mGetByPromptKey invalid param");
    }

    return promptBasicMapper.selectByPromptKeys(spaceId, promptKeys);
  }

  @Override
  public boolean existsPromptKey(Long spaceId, String promptKey) {
    return promptBasicMapper.existsPromptKey(spaceId, promptKey);
  }
  @Override
  public boolean existsPromptName(Long spaceId, String promptName) {
    return promptBasicMapper.existsPromptName(spaceId, promptName);
  }

  @Override
  public List<PromptBasicEntity> list(ListPromptBasicParam param) {

    // 再查询数据
    Integer offset = param.getOffset();
    Integer limit = param.getLimit();
    RowBounds rowBounds = new RowBounds(offset, limit);

    return promptBasicMapper.selectByCondition(param, rowBounds);
  }

  @Override
  public long countByCondition(ListPromptBasicParam param) {
    return promptBasicMapper.countByCondition(param);
  }

  @Override
  public void update(Long promptId, Long spaceId, PromptBasicEntity updatePromptBasicPO) {
    if (promptId <= 0) {
      throw new IllegalArgumentException("promptID is invalid, promptID = " + promptId);
    }
    if (spaceId == null) {
      throw new IllegalArgumentException("spaceID is invalid, spaceID is null");
    }

    promptBasicMapper.updateById(promptId, spaceId, updatePromptBasicPO);
  }
}
