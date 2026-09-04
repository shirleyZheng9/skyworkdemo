package com.iwhalecloud.bote.mapper.a2a;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.a2a.A2aAgentDTO;
import com.iwhalecloud.bote.dto.a2a.query.A2aAgentQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * A2A 服务相关数据库操作
 *
 * @author bianjp
 * @since 2025-09-08
 */
public interface A2aAgentMapper {

  /**
   * 根据主键查询 A2A 服务
   */
  @Nullable
  A2aAgentDTO selectAgent(@Param("tenantId") Long tenantId, @Param("id") Long agentId);

  /**
   * 根据平台 ID 和外系统智能体 ID 查询 A2A 服务
   */
  @Nullable
  A2aAgentDTO selectAgentByExtAgentId(@Param("tenantId") Long tenantId, @Param("platformId") Long platformId, @Param("extAgentId") String extAgentId);

  /**
   * 根据 A2A 服务 ID 查询平台 ID
   */
  @Nullable
  Long selectPlatformIdByAgentId(@Param("tenantId") Long tenantId, @Param("agentId") Long agentId);

  /**
   * 检查是否已存在同名 A2A 服务
   */
  boolean existsAgentName(@Param("tenantId") Long tenantId, @Param("agentName") String agentName);

  /**
   * 新增 A2A 服务
   */
  int insertAgent(@Param("dto") A2aAgentDTO agent);

  /**
   * 修改 A2A 服务
   */
  int updateAgent(@Param("dto") A2aAgentDTO agent);

  /**
   * 删除 A2A 服务
   */
  int deleteAgent(@Param("tenantId") Long tenantId, @Param("agentId") Long agentId, @Param("updatorId") Long updatorId);

  /**
   * 查询 A2A 服务列表
   */
  List<A2aAgentDTO> selectAgentList(@Param("query") A2aAgentQueryParams queryParams);

  /**
   * 分页查询 A2A 服务
   */
  Page<A2aAgentDTO> selectAgentPage(@Param("query") A2aAgentQueryParams queryParams, RowBounds rowBounds);
}
