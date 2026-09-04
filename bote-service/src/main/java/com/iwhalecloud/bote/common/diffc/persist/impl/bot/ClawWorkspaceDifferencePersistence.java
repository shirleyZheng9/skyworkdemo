package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bote.dto.bot.ClawWorkspaceDTO;
import com.iwhalecloud.bote.mapper.bot.ClawWorkspaceManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：claw 工作空间
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Component
public final class ClawWorkspaceDifferencePersistence extends BaseRootPersistence<ClawWorkspaceDTO> {
  public ClawWorkspaceDifferencePersistence(ClawWorkspaceManageMapper workspaceManageMapper) {
    // 新增情况
    this.setBatchAddConsumer(workspaceManageMapper::batchInsertClawWorkspace);
    // 修改情况
    this.setModifyConsumer(workspaceManageMapper::updateClawWorkspace);
  }
}
