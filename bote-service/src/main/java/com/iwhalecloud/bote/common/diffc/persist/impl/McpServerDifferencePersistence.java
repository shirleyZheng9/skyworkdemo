package com.iwhalecloud.bote.common.diffc.persist.impl;

import com.iwhalecloud.bote.dto.mcp.McpServerDTO;
import com.iwhalecloud.bote.mapper.mcp.McpServerManageMapper;
import com.iwhalecloud.bss.litchi.diffc.persist.BaseRootPersistence;
import org.springframework.stereotype.Component;

/**
 * MCP服务
 *
 * @author qian.sisheng
 * @since 2025-05-19
 */
@Component
public final class McpServerDifferencePersistence extends BaseRootPersistence<McpServerDTO> {
  public McpServerDifferencePersistence(McpServerManageMapper modelManageMapper) {
    setAddConsumer(modelManageMapper::insertMcpServer);
    setModifyConsumer(modelManageMapper::updateMcpServer);
  }
}
