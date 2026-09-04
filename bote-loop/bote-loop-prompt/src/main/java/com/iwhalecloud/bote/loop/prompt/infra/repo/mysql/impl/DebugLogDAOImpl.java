package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.impl;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugLogEntity;
import com.iwhalecloud.bote.loop.prompt.domain.repo.dto.ListParam;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IDebugLogDAO;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptDebugLogMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 * 调试日志DAO实现
 * 迁移对应关系: Go语言mysql.DebugLogDAOImpl
 * - 功能: 调试日志的数据访问实现
 * - 主要方法:
 * * list - 查询调试日志列表
 * * save - 保存调试日志
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.DebugLogDAOImpl结构体
 * - 使用原生MyBatis Mapper实现数据访问
 * - 支持复杂查询条件
 * - 集成错误处理机制
 */
@Repository
@RequiredArgsConstructor
public class DebugLogDAOImpl implements IDebugLogDAO {
  private final PromptDebugLogMapper promptDebugLogMapper;

  @Override
  public List<PromptDebugLogEntity> list(ListParam param) {
    Integer limit = param.getLimit();
    RowBounds rowBounds = new RowBounds(0, limit);
    return promptDebugLogMapper.selectByCondition(param, rowBounds);
  }

  @Override
  public void save(PromptDebugLogEntity debugLog) {
    if (debugLog == null) {
      return;
    }

    promptDebugLogMapper.insert(debugLog);
  }
}
