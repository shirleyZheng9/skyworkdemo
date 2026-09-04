package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.DataSourceTestParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 技能：数据源 服务
 *
 * @author auto
 * @since 2024-09-16
 */
public interface IDataSourceManageService {

  /**
   * 查询单个数据源
   *
   * @param tenantId 租户 ID
   * @param dataSourceId 数据源主键
   * @return 数据源
   */
  @Nullable
  DataSourceDTO findDataSource(Long tenantId, Long dataSourceId);

  /**
   * 保存数据源
   *
   * @param dataSource 数据源
   * @return 结果
   */
  ResultVO<DataSourceDTO> saveDataSource(DataSourceDTO dataSource);

  /**
   * 删除数据源
   *
   * @param tanentId 租户 ID
   * @param dataSourceId 数据源主键
   * @return 结果
   */
  ResultVO<Void> deleteDataSource(Long tanentId, Long dataSourceId);

  /**
   * 查询数据源列表
   *
   * @param queryParams 查询条件
   * @return 数据源列表
   */
  List<DataSourceDTO> queryDataSourceList(SkillQueryParams queryParams);

  /**
   * 查询数据源列表（分页）
   *
   * @param queryParams 查询条件
   * @return 数据源分页列表
   */
  PageInfo<DataSourceDTO> queryDataSourcePage(SkillQueryParams queryParams);

  /**
   * 查询数据源配置
   */
  @Nullable
  DataSourceProperties findDataSourceProperties(Long tenantId, Long dataSourceId);

  /**
   * 测试数据库连接
   *
   * @param params 参数
   * @return 结果
   */
  ResultVO<String> testDataSourceLink(DataSourceTestParams params);

  /**
   * 获取租户所属的平台数据源，如果没有则创建
   *
   * @param tenantId 租户ID
   * @return 租户所属的平台数据源
   */
  DataSourceDTO getPlatformDataSource(Long tenantId);

}
