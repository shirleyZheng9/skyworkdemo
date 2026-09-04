package com.iwhalecloud.bote.service.agent;

import com.iwhalecloud.bote.dto.agent.AddAiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.AiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 用户级的环境变量管理服务
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface IAiEnvVariableManageService {

  /**
   * 保存用户级的环境变量
   *
   * @param variable 用户级的环境变量
   * @return 结果
   */
  ResultVO<List<AiEnvVariableDTO>> saveAiEnvVariable(AddAiEnvVariableDTO variable);

  /**
   * 查询用户级的环境变量列表
   *
   * @param queryParams 查询条件
   * @return 用户级的环境变量列表
   */
  List<AiEnvVariableDTO> queryAiEnvVariableList(AiQueryParams queryParams);
}
