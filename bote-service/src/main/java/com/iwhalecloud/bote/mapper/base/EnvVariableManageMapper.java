package com.iwhalecloud.bote.mapper.base;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.base.EnvVariableDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
import com.iwhalecloud.bote.dto.base.query.EnvVariableQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 环境变量 mapper
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
public interface EnvVariableManageMapper {
  /**
   * 添加环境变量
   *
   * @param variable 变量列表
   * @return 影响行数
   */
  int insertEnvVariable(@Param("dto") EnvVariableDTO variable);

  /**
   * 批量添加环境变量值
   *
   * @param variableVals 变量值列表
   * @return 影响行数
   */
  int batchInsertVariableVal(@Param("list") List<EnvVariableValDTO> variableVals);

  /**
   * 检查环境变量编码是否存在
   *
   * @param variable 环境变量
   * @return 是否存在
   */
  boolean existsEnvVariableCode(@Param("dto") EnvVariableDTO variable);

  /**
   * 更新环境变量
   *
   * @param variable 环境变量
   * @return 影响行数
   */
  int updateVariable(@Param("dto") EnvVariableDTO variable);

  /**
   * 更新环境变量值
   *
   * @param variableVal 环境变量值
   * @return 影响行数
   */
  int updateVariableVal(@Param("dto") EnvVariableValDTO variableVal);

  /**
   * 根据变查询参数条件查找
   *
   * @param param 查询参数
   * @return 环境变量详情
   */
  List<EnvVariableDTO> selectEnvVariableList(@Param("param") EnvVariableQueryParams param);

  /**
   * 根据变查询参数条件查找
   *
   * @param param 查询参数
   * @param rowBounds 分页参数
   * @return 环境变量详情
   */
  Page<EnvVariableDTO> selectEnvVariablePage(@Param("param") EnvVariableQueryParams param,  RowBounds rowBounds);

  /**
   * 根据境变量主键查找环境变量
   *
   * @param variableId 环境变量ID
   * @param tenantId 租户ID
   * @return 环境变量详情
   */
  EnvVariableDTO findEnvVariableById(@Param("variableId") Long variableId, @Param("tenantId") Long tenantId);

  /**
   * 根据环境变量主键查找环境变量值
   *
   * @param variableId 环境变量ID
   * @param tenantId 租户ID
   * @return 环境变量值列表
   */
  List<EnvVariableValDTO> selectVariableValByVariableId(@Param("variableId") Long variableId, @Param("tenantId") Long tenantId);

  /**
   * 根据环境变量主键列表查找环境变量值
   *
   * @param variableIds 环境变量ID列表
   * @param tenantId 租户ID
   * @return 环境变量值列表
   */
  List<EnvVariableValDTO> selectVariableValByVariableIds(@Param("variableIds") List<Long> variableIds, @Param("tenantId") Long tenantId);

  /**
   * 根据环境变量值ID删除环境变量
   *
   * @param updatorId 修改者ID
   * @param variableId 环境变量ID
   * @param tenantId 租户ID
   * @return 影响的行数
   */
  int deleteEnvVariableById(@Param("updatorId") Long updatorId, @Param("variableId") Long variableId, @Param("tenantId") Long tenantId);

  /**
   * 根据环境变量值ID删除环境变量值
   *
   * @param updatorId 修改者ID
   * @param variableId 环境变量ID
   * @param tenantId 租户ID
   * @return 影响的行数
   */
  int deleteEnvVariableValById(@Param("updatorId") Long updatorId, @Param("variableId") Long variableId, @Param("tenantId") Long tenantId);

  /**
   * 根据租户ID查询环境变量列表
   *
   * @param tenantId 租户ID
   * @return 环境变量列表
   */
  List<EnvVariableDTO> selectListByTenantId(@Param("tenantId") Long tenantId);

  /**
   * 根据编辑变量编码查询对应环境的变量值
   *
   * @param tenantId 租户ID
   * @param varCode 变量编码
   * @param envCode 环境编码
   * @return 变量
   */
  EnvVariableValDTO selectEnvVariableValByCode(@Param("tenantId") Long tenantId, @Param("varCode") String varCode, @Param("envCode") String envCode);

  /**
   * 根据变量编码列表查询对应环境的变量
   *
   * @param tenantId 租户ID
   * @param variableCodes 变量编码列表
   * @return 列表
   */
  List<Long> selectEnvVariableIdsByCodes(@Param("tenantId") Long tenantId, @Param("variableCodes") List<String> variableCodes);

}
