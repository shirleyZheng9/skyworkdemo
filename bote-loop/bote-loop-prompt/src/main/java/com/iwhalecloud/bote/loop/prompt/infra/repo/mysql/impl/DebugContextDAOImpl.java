package com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.impl;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugContextEntity;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IDebugContextDAO;
import com.iwhalecloud.bote.mapper.loop.prompt.PromptDebugContextMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 调试上下文DAO实现
 * 迁移对应关系: Go语言mysql.DebugContextDAOImpl
 * - 功能: 调试上下文的数据访问实现
 * - 主要方法:
 * * save - 保存调试上下文
 * * get - 获取调试上下文
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.DebugContextDAOImpl结构体
 * - 使用原生MyBatis Mapper实现数据访问
 * - 使用Spring事务管理
 * - 集成错误处理机制
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java MyBatis原生Mapper
 * - Go context.Context -> Java方法参数（可选）
 * - Go error返回 -> Java异常处理
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Repository
@RequiredArgsConstructor
public class DebugContextDAOImpl implements IDebugContextDAO {
  private final PromptDebugContextMapper promptDebugContextMapper;

  @Override
  public void save(PromptDebugContextEntity debugContext) {
    if (debugContext == null) {
      return;
    }

    // 使用原生MyBatis的insert方法
    promptDebugContextMapper.insert(debugContext);
  }

  @Override
  public PromptDebugContextEntity get(Long promptId, String userId) {
    // 使用原生MyBatis的selectByCondition方法
    List<PromptDebugContextEntity> debugContexts = promptDebugContextMapper.selectByCondition(
      promptId, userId);

    if (debugContexts == null || debugContexts.isEmpty()) {
      return null;
    }

    return debugContexts.get(0);
  }
}
