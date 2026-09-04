package com.iwhalecloud.bote.service.base;

import com.iwhalecloud.bote.dto.base.DcCfgDTO;
import java.util.List;

/**
 * 在线配置查询服务
 *
 * @author chen.linfa
 * @since 2025-11-07
 */
public interface IDcCfgQueryService {
  List<DcCfgDTO> querySwitchList();
}
