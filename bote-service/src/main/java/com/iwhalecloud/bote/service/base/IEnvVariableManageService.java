package com.iwhalecloud.bote.service.base;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.base.EnvVariableDTO;
import com.iwhalecloud.bote.dto.base.EnvVariableValDTO;
import com.iwhalecloud.bote.dto.base.query.EnvVariableQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 环境变量管理服务
 *
 * @author qian.sisheng
 * @since 2025-11-03
 */
public interface IEnvVariableManageService {

  /**
   * 保存环境变量
   *
   * @param variable 环境变量
   * @return 结果
   */
  ResultVO<EnvVariableDTO> saveEnvVariable(EnvVariableDTO variable);

  /**
   * 查询环境变量列表
   *
   * @param param 查询参数
   * @return 返回环境变量列表
   */
  List<EnvVariableDTO> queryEnvVariableList(EnvVariableQueryParams param);

  /**
   * 查询环境变量列表(分页)
   *
   * @param param 查询参数
   * @return 返回环境变量列表
   */
  PageInfo<EnvVariableDTO> queryEnvVariablePage(EnvVariableQueryParams param);

  /**
   * 查询环境变量参数列表（流程变量使用）
   *
   * @param tenantId 租户ID
   * @return 返回环境变量列表
   */
  List<EnvVariableDTO> getEnvVariableParamList(Long tenantId);

  /**
   * 查询环境变量详情
   *
   * @param variableId 环境变量主键
   * @param tenantId 租户ID
   * @return 返回环境变量详情
   */
  EnvVariableDTO findEnvVariable(Long variableId, Long tenantId);

  /**
   * 删除环境变量
   *
   * @param variableId 环境变量ID
   * @return 影响的行数
   */
  ResultVO<Void> deleteEnvVariableById(Long variableId, Long tenantId);

  /**
   * 根据编码获取当前环境下的环境变量值
   *
   * @param variableCode 变量编码
   * @param tenantId 租户ID
   * @return 当前环境的环境变量值
   */
  @Nullable
  EnvVariableValDTO getEnvVariableValueByCode(String variableCode, Long tenantId);

}
