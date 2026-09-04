package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.impl;

import com.iwhalecloud.bote.entity.loop.prompt.PromptCommitEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListCommitParam;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.PromptIDCommitVersionPair;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IPromptCommitDAO;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptCommitMapper;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * Prompt提交记录DAO实现
 * 迁移对应关系: Go语言mysql.PromptCommitDAOImpl
 * - 功能: Prompt提交记录的数据访问实现
 * - 主要方法:
 * * create - 创建提交记录
 * * get - 获取提交记录
 * * mGet - 批量获取提交记录
 * * list - 分页查询提交记录列表
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.PromptCommitDAOImpl结构体
 * - 使用原生MyBatis Mapper实现数据访问
 * - 支持复杂查询条件和分页
 * - 集成写追踪机制
 */
@Repository
@RequiredArgsConstructor
public class PromptCommitDAOImpl implements IPromptCommitDAO {
  private final PromptCommitMapper promptCommitMapper;

  @Override
  public void create(PromptCommitEntity promptCommitPO, Date timeNow) {
    if (promptCommitPO == null) {
      throw new IllegalArgumentException("promptCommitPO is empty");
    }

    promptCommitPO.setCreatedAt(new Date());
    promptCommitPO.setUpdatedAt(new Date());
    promptCommitMapper.insert(promptCommitPO);
  }

  @Override
  public PromptCommitEntity get(Long promptId, String commitVersion) {
    if (promptId <= 0) {
      throw new IllegalArgumentException("promptID is invalid, promptID = " + promptId);
    }

    return promptCommitMapper.selectByPromptIdAndVersion(promptId, commitVersion);
  }

  @Override
  public Map<PromptIDCommitVersionPair, PromptCommitEntity> mGet(List<PromptIDCommitVersionPair> pairs) {
    if (pairs == null || pairs.isEmpty()) {
      throw new IllegalArgumentException("invalid param");
    }

    List<PromptCommitEntity> results = promptCommitMapper.selectByPairs(pairs);
    return results.stream()
      .collect(java.util.stream.Collectors.toMap(
        po -> new PromptIDCommitVersionPair(po.getPromptId(), po.getVersion()),
        po -> po
      ));
  }

  @Override
  public List<PromptCommitEntity> list(ListCommitParam param) {
    if (param.getPromptId() <= 0 || param.getLimit() <= 0) {
      throw new IllegalArgumentException("Param(PromptID or Limit) is invalid");
    }
    RowBounds rowBounds = new RowBounds(0, param.getLimit());

    return promptCommitMapper.selectByCondition(param, rowBounds);
  }
}
