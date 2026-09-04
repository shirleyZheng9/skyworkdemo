package com.iwhalecloud.bote.service.a2a;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aAgentQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * A2A 服务管理服务
 *
 * @author bianjp
 * @since 2025-09-08
 */
public interface IA2aAgentManageService {

  /**
   * 查询单个 A2A 服务
   *
   * @param tenantId 租户 ID
   * @param agentId A2A 服务 ID
   * @return A2A 服务
   */
  A2aAgentDTO findA2aAgent(Long tenantId, Long agentId);

  /**
   * 保存A2A 服务
   *
   * @param agent A2A 服务
   */
  ResultVO<A2aAgentDTO> saveA2aAgent(A2aAgentDTO agent);

  /**
   * 删除A2A 服务
   *
   * @param tenantId 租户 ID
   * @param agentId A2A 服务 ID
   * @param userId 操作人 ID
   */
  ResultVO<Void> deleteA2aAgent(Long tenantId, Long agentId, Long userId);

  /**
   * 查询 A2A 服务列表
   *
   * @param queryParams 查询条件
   * @return A2A 服务列表
   */
  List<A2aAgentDTO> queryA2aAgentList(A2aAgentQueryParams queryParams);

  /**
   * 分页查询 A2A 服务
   *
   * @param queryParams 查询条件
   * @return A2A 服务分页列表
   */
  PageInfo<A2aAgentDTO> queryA2aAgentPage(A2aAgentQueryParams queryParams);

}
