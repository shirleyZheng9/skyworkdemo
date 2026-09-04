package com.iwhalecloud.bote.mapper.app;

import com.iwhalecloud.bote.dto.app.WorkbenchAppAuthDTO;
import com.iwhalecloud.bote.dto.organization.OrganizationUserDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作台应用授权 Mapper
 *
 * @author wang.tingyun
 * @since 2025-09-15
 */
public interface WorkbenchAppAuthMapper {

  /**
   * 新增工作台应用授权
   * @param dto 授权
   * @return 新增结果
   */
  int insertAppAuth(@Param("dto") WorkbenchAppAuthDTO dto);

  /**
   * 批量新增工作台应用授权
   * @param dtoList 授权列表
   * @return 新增结果
   */
  int insertAppAuthBatch(@Param("list") List<WorkbenchAppAuthDTO> dtoList);

  /**
   * 根据工作台应用ID删除授权
   *
   * @param workbenchAppId 工作台应用ID
   * @param updatorId 更新人
   * @return 删除结果
   */
  int deleteByWorkbenchAppId(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId, @Param("updatorId") Long updatorId);

  /**
   * 根据用户ID查询授权列表
   *
   * @param userId 用户ID
   * @param spaceId 企业空间 ID
   * @return 授权列表
   */
  List<WorkbenchAppAuthDTO> selectByUserId(@Param("userId") Long userId, @Param("spaceId") Long spaceId);

  /**
   * 根据组织ID查询授权列表
   *
   * @param orgId 组织ID
   * @param spaceId 企业空间 ID
   * @return 授权列表
   */
  List<WorkbenchAppAuthDTO> selectByOrgId(@Param("orgId") Long orgId, @Param("spaceId") Long spaceId);

  /**
   * 查询工作台应用授权的用户和组织信息
   *
   * @param workbenchAppId 工作台ID
   * @param spaceId 企业空间 ID
   * @return 授权信息
   */
  List<OrganizationUserDTO> selectByWorkbenchAppId(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

}