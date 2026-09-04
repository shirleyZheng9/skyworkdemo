package com.iwhalecloud.bote.common.diffc.persist.impl.scene;

import com.iwhalecloud.bote.dto.bot.PlatSceneInfoDTO;
import com.iwhalecloud.bote.mapper.bot.PlatSceneInfoManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异保存服务：模板智能体
 *
 * @author auto
 * @since 2025-06-21
 */
@Component
public final class PlatSceneInfoDifferencePersistence extends BaseRootPersistence<PlatSceneInfoDTO> {

  public PlatSceneInfoDifferencePersistence(PlatSceneInfoManageMapper platSceneInfoManageMapper) {
    setAddConsumer(platSceneInfoManageMapper::insertPlatSceneInfo);
    setModifyConsumer(platSceneInfoManageMapper::updatePlatSceneInfo);
  }
}
