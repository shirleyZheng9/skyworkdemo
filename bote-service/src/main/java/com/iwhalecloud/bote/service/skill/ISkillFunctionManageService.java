package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.SimpleSkillFunctionDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bote.dto.skill.FunctionTestParams;
import com.iwhalecloud.bote.dto.skill.SkillFunctionDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;

/**
 * 技能：服务函数 服务
 *
 * @author auto
 * @since 2024-09-15
 */
public interface ISkillFunctionManageService {
  /**
   * 保存服务函数
   *
   * @param function 服务函数
   * @return 结果
   */
  ResultVO<SkillFunctionDTO> saveSkillFunction(SkillFunctionDTO function);

  /**
   * 查询单个服务函数
   *
   * @param tenantId 租户 ID
   * @param funcId 服务函数主键
   * @return 服务函数
   */
  SkillFunctionDTO findSkillFunction(Long tenantId, Long funcId);

  /**
   * 查询服务函数列表
   *
   * @param queryParams 查询条件
   * @return 服务函数列表
   */
  List<SimpleSkillFunctionDTO> querySkillFunctionList(SkillQueryParams queryParams);

  /**
   * 查询服务函数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 服务函数分页列表
   */
  PageInfo<SkillFunctionDTO> querySkillFunctionPage(SkillQueryParams queryParams);

  /**
   * 查询服务函数列表（分页）
   *
   * @param queryParams 查询条件
   * @return 服务函数分页列表
   */
  PageInfo<SimpleSkillFunctionDTO> querySimpleSkillFunctionPage(SkillQueryParams queryParams);

  /**
   * 删除服务函数
   *
   * @param tenantId 租户 ID
   * @param funcId 服务函数主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillFunction(Long tenantId, Long funcId);

  /**
   * 服务测试
   *
   * @param params 参数
   * @return 结果
   */
  ResultVO<Object> test(FunctionTestParams params);
}
