package com.iwhalecloud.bote.common.diffc.persist.impl.app;

import com.iwhalecloud.bote.dto.app.WorkbenchAppDTO;
import com.iwhalecloud.bote.mapper.app.WorkbenchAppMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * 数据差异服务：工作台应用
 *
 * @author tingyun.wang
 * @since 2025-09-08
 */
@Component
public final class WorkbenchAppDifferencePersistence extends BaseRootPersistence<WorkbenchAppDTO> {

  public WorkbenchAppDifferencePersistence(WorkbenchAppMapper workbenchAppMapper) {
    // 新增情况
    this.setAddConsumer(workbenchAppMapper::insertWorkbenchApp);
    // 修改情况
    this.setModifyConsumer(workbenchAppMapper::updateWorkbenchApp);
  }

}
