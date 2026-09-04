package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceInstDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：数据源 Mapper
 *
 * @author auto
 * @since 2024-09-16
 */
public interface DataSourceManageMapper {

  /**
   * 新增数据源
   *
   * @param dataSource 数据源
   * @return 结果
   */
  int insertDataSource(@Param("dto") DataSourceDTO dataSource);

  /**
   * 修改数据源
   *
   * @param dataSource 数据源
   * @return 结果
   */
  int updateDataSource(@Param("dto") DataSourceDTO dataSource);

  /**
   * 根据主键获取数据源
   *
   * @param dataSourceId 数据源主键
   * @return 数据源
   */
  DataSourceDTO getDataSource(@Param("tenantId") Long tenantId, @Param("id") Long dataSourceId);

  /**
   * 获取数据源列表
   *
   * @param queryParams 查询条件
   * @return 数据源列表
   */
  List<DataSourceDTO> selectDataSourceList(@Param("query") SkillQueryParams queryParams);

  /**
   * 获取数据源列表（分页）
   *
   * @param queryParams 查询条件
   * @return 数据源分页列表
   */
  Page<DataSourceDTO> selectDataSourcePage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据主键删除数据源
   */
  int deleteDataSource(@Param("tenantId") Long tenantId, @Param("dataSourceId") Long dataSourceId, @Param("updatorId") Long updatorId);

  /**
   * 校验数据源的编码唯一性
   *
   * @param dataSource 数据源
   * @return 结果
   */
  boolean existsDataSourceCode(@Param("dto") DataSourceDTO dataSource);

  /**
   * 批量新增数据源实例
   *
   * @param insts 数据源实例列表
   * @return 结果
   */
  int batchInsertDataSourceInst(@Param("list") List<DataSourceInstDTO> insts);

  /**
   * 修改数据源实例
   *
   * @param dataSourceInst 数据源实例
   * @return 结果
   */
  int updateDataSourceInst(@Param("dto") DataSourceInstDTO dataSourceInst);

  /**
   * 获取数据源实例列表
   *
   * @param dataSourceId 数据源ID
   * @return 数据源实例列表
   */
  List<DataSourceInstDTO> selectDataSourceInstList(@Param("tenantId") Long tenantId, @Param("dataSourceId") Long dataSourceId);

  /**
   * 根据数据源 ID 和环境编码查询数据源实例
   *
   * @param dataSourceId 数据源ID
   * @param envCode 环境编码
   * @return 数据源配置
   */
  DataSourceProperties selectInstByDataSourceIdAndEnvCode(@Param("tenantId") Long tenantId, @Param("dataSourceId") Long dataSourceId, @Param("envCode") String envCode);

  /**
   * 根据环境编码查询数据源实例
   *
   * @param tenantId 租户 ID
   * @param envCode 环境编码
   * @return 数据源实例列表
   */
  List<DataSourceInstDTO> selectDataSourceInstByEnvCode(@Param("tenantId") Long tenantId, @Param("envCode") String envCode);

  /**
   * 获取租户对应的平台数据源
   *
   * @param tenantId 租户ID
   * @return 租户对应的平台数据源
   */
  DataSourceDTO selectPlatformDataSource(@Param("tenantId") Long tenantId);

  /**
   * 查询租户对应的数据源基本信息
   *
   * @param tenantId 租户ID
   * @param dataSourceId 数据源ID
   * @return 数据源基本信息
   */
  DataSourceDTO selectDataSourceBasicInfo(@Param("tenantId") Long tenantId, @Param("dataSourceId") Long dataSourceId);

}
