package com.iwhalecloud.bote.common.diffc.persist.impl.bot;

import com.iwhalecloud.bote.dto.bot.ClawEnvVariableDTO;
import com.iwhalecloud.bote.mapper.bot.ClawEnvVariableManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：claw 环境变量
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Component
public final class ClawVariableDifferencePersistence extends BaseRootPersistence<ClawEnvVariableDTO> {
  public ClawVariableDifferencePersistence(ClawEnvVariableManageMapper envVariableManageMapper) {
    // 新增情况
    this.setBatchAddConsumer(envVariableManageMapper::batchInsertClawEnvVariable);
    // 修改情况
    this.setModifyConsumer(envVariableManageMapper::updateClawEnvVariable);
  }
}
