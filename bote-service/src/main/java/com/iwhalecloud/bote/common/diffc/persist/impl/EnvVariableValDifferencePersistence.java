package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
import com.iwhalecloud.bote.mapper.base.EnvVariableManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：环境变量值
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
@Component
public final class EnvVariableValDifferencePersistence extends BaseRootPersistence<EnvVariableValDTO> {
  public EnvVariableValDifferencePersistence(EnvVariableManageMapper envVariableMapper) {
    // 新增情况
    setBatchAddConsumer(envVariableMapper::batchInsertVariableVal);
    // 修改情况
    setModifyConsumer(envVariableMapper::updateVariableVal);
  }
}
