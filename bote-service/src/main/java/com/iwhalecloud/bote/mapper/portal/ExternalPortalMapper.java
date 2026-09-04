package com.iwhalecloud.bote.mapper.portal;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.portal.query.ExternalPortalQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 外部门户
 *
 * @author bianjp
 * @since 2025-02-24
 */
public interface ExternalPortalMapper {

  /**
   * 新增门户
   */
  int insertPortal(@Param("dto") ExternalPortalDTO externalPortal);

  /**
   * 更新门户
   */
  int updatePortal(@Param("dto") ExternalPortalDTO externalPortal);

  /**
   * 更新门户状态（启用、禁用）
   */
  int updatePortalStatus(@Param("id") Long id, @Param("status") String status, @Param("updatorId") Long updatorId);

  /**
   * 删除门户
   */
  int deletePortal(@Param("id") Long id, @Param("updatorId") Long updatorId);

  /**
   * 检查门户编码是否存在
   *
   * @param portalCode 门户编码
   * @return 是否存在
   */
  boolean existsPortalCode(@Param("portalCode") String portalCode);

  /**
   * 根据 ID 查询门户
   */
  @Nullable
  ExternalPortalDTO selectPortalById(@Param("id") Long id);

  /**
   * 分页查询门户
   */
  Page<ExternalPortalDTO> selectPortalPage(@Param("query") ExternalPortalQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询所有的门户
   *
   * @param tenantId 租户 ID
   */
  List<ExternalPortalDTO> selectAllPortalList(@Param("tenantId") Long tenantId);

  /**
   * 根据门户编码查询门户
   */
  @Nullable
  ExternalPortalDTO selectPortalByPortalCode(@Param("portalCode") String portalCode);

  /**
   * 根据门户类型查询门户
   */
  @Nullable
  List<ExternalPortalDTO> selectPortalByPortalType(@Param("portalType") String portalType, @Param("portalCode") String portalCode);
}
