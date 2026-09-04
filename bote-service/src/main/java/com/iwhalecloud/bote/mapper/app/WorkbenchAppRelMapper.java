package com.iwhalecloud.bote.mapper.app;

import com.iwhalecloud.bote.dto.app.WorkbenchAppRelDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作台应用关联 Mapper
 *
 * @author wang.tingyun
 * @since 2025-09-08
 */
public interface WorkbenchAppRelMapper {

  /**
   * 新增工作台应用关联对象
   * @param dto 关联对象
   * @return 新增结果
   */
  int insertAppRel(@Param("dto") WorkbenchAppRelDTO dto);

  /**
   * 批量新增工作台应用关联对象
   * @param dtoList 关联对象列表
   * @return 新增结果
   */
  int insertAppRelBatch(@Param("list") List<WorkbenchAppRelDTO> dtoList);

  /**
   * 根据关联应用ID检查是否存在
   *
   * @param relAppId 关联应用ID
   * @return 检查结果
   */
  boolean existByRelAppId(@Param("relAppId") Long relAppId, @Param("spaceId") Long spaceId, @Param("tenantId") Long tenantId);

  /**
   * 删除 WorkbenchAppId 下的全部关联对象
   *
   * @param workbenchAppId 工作台应用ID
   * @param updatorId 更新人
   * @return 删除结果
   */
  int deleteByWorkbenchAppId(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId, @Param("updatorId") Long updatorId);

  /**
   * 根据工作台应用ID查询关联列表
   *
   * @param workbenchAppId 作台应用ID
   * @param spaceId 企业空间ID
   * @return 关联列表
   */
  List<WorkbenchAppRelDTO> selectByWorkbenchAppId(@Param("workbenchAppId") Long workbenchAppId, @Param("spaceId") Long spaceId);

  /**
   * 根据工作台应用ID列表查询关联列表
   *
   * @param appIdList 作台应用ID列表
   * @param spaceId 企业ID
   * @return 关联列表
   */
  List<WorkbenchAppRelDTO> selectByWorkbenchAppIds(@Param("appIdList") List<Long> appIdList, @Param("spaceId") Long spaceId);

  /**
   * 根据关联应用ID和类型查询关联信息
   *
   * @param tenantId 租户ID
   * @param relAppId 关联应用ID
   * @param relAppType 关联应用类型
   * @return 关联信息，如果未发布则返回null
   */
  WorkbenchAppRelDTO selectByRelAppIdAndType(@Param("tenantId") Long tenantId, @Param("relAppId") Long relAppId, @Param("relAppType") String relAppType);

  /**
   * 解除发布应用（将状态修改为00X）
   *
   * @param tenantId 租户ID
   * @param relAppId 关联应用ID
   * @param relAppType 关联应用类型
   * @return 更新结果
   */
  int unpublishByRelAppIdAndType(@Param("tenantId") Long tenantId, @Param("relAppId") Long relAppId, @Param("relAppType") String relAppType);

  /**
   * 更新工作台应用状态为未启用
   *
   * @param spaceId 租户ID
   * @param workbenchAppId 工作台应用ID
   * @return 更新结果
   */
  int updateAppStatusToDisabled(@Param("spaceId") Long spaceId, @Param("workbenchAppId") Long workbenchAppId);

}


