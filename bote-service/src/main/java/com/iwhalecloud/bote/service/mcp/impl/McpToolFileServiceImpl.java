package com.iwhalecloud.bote.service.mcp.impl;

import com.iwhalecloud.bote.common.consts.SceneConsts;
import com.iwhalecloud.bote.mapper.mcp.McpToolFileMapper;
import com.iwhalecloud.bote.service.mcp.IMcpToolFileService;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MCP 工具文件管理服务
 *
 * @author bianjp
 * @since 2025-06-10
 */
@Service
@RequiredArgsConstructor
public class McpToolFileServiceImpl implements IMcpToolFileService {
  /** 工作流调试生成的 MCP 工具文件保留天数 */
  private static final int TEST_FILE_KEEP_DAYS = 1;

  private final McpToolFileMapper mcpToolFileMapper;

  @Override
  @Transactional
  public void deleteBySessionIds(List<Long> sessionIds) {
    mcpToolFileMapper.disableFilesBySessionIds(sessionIds);
    mcpToolFileMapper.deleteBySessionIds(sessionIds);
  }

  @Override
  @Transactional
  public void deleteBySessionId(Long sessionId) {
    deleteBySessionIds(Collections.singletonList(sessionId));
  }

  @Override
  public void deleteTestFiles() {
    Date maxCreatedTime = DateUtils.addDays(new Date(), -TEST_FILE_KEEP_DAYS);
    // 数据量应该不大，一次性更新全部
    mcpToolFileMapper.disableTestFiles(SceneConsts.TEST_CONVERSATION_ID, maxCreatedTime);
    mcpToolFileMapper.deleteTestFiles(SceneConsts.TEST_CONVERSATION_ID, maxCreatedTime);
  }
}
