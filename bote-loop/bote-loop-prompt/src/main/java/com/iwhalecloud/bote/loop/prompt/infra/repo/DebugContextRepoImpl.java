package com.iwhalecloud.bote.loop.prompt.infra.repo;

import com.iwhalecloud.bote.entity.loop.prompt.PromptDebugContextEntity;
import com.iwhalecloud.bote.loop.infra.idgen.IIDGenerator;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugContext;
import com.iwhalecloud.bote.loop.prompt.domain.repo.IDebugContextRepo;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.IDebugContextDAO;
import com.iwhalecloud.bote.loop.prompt.infra.repo.mysql.convertor.DebugContextConvertor;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 调试上下文仓库实现
 * 迁移对应关系: Go语言repo.DebugContextRepoImpl
 * - 功能: 调试上下文的数据访问实现
 * - 主要方法:
 * * saveDebugContext - 保存调试上下文
 * * getDebugContext - 获取调试上下文
 * <p>
 * Java实现说明:
 * - 对应Go的repo.DebugContextRepoImpl结构体
 * - 使用Spring Data JPA实现数据访问
 * - 使用Spring事务管理
 * - 集成ID生成器
 * <p>
 * 技术栈迁移:
 * - Go GORM -> Java Spring Data JPA
 * - Go context.Context -> Java方法参数
 * - Go error返回 -> Java异常处理
 * - Go int64 -> Java Long
 * - Go string -> Java String
 */
@Component
@RequiredArgsConstructor
public class DebugContextRepoImpl implements IDebugContextRepo {
  private final IIDGenerator idGenerator;
  private final IDebugContextDAO debugContextDAO;

  @Override
  public void saveDebugContext(DebugContext debugContext) {
    if (debugContext == null) {
      return;
    }
    Long id = idGenerator.genId();
    PromptDebugContextEntity debugContextPO = DebugContextConvertor.debugContextDO2PO(debugContext);
    debugContextPO.setId(id);
    debugContextPO.setCreatedAt(new Date());
    debugContextPO.setUpdatedAt(new Date());

    if (debugContextPO.getDeletedAt() == null) {
      debugContextPO.setDeletedAt(0L);
    }

    debugContextDAO.save(debugContextPO);
  }

  @Override
  public DebugContext getDebugContext(Long promptId, String userId) {
    PromptDebugContextEntity debugContextPO = debugContextDAO.get(promptId, userId);
    if (debugContextPO == null) {
      return null;
    }
    return DebugContextConvertor.debugContextPO2DO(debugContextPO);
  }
}
