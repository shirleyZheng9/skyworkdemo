package com.iwhalecloud.bote.mapper.agent;

import com.iwhalecloud.bote.dto.agent.AiEnvVariableDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 用户级的环境变量管理
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface AiEnvVariableManageMapper {

  int batchInsertAiEnvVariable(@Param("list") List<AiEnvVariableDTO> btAiEnvVariables);

  int updateAiEnvVariable(@Param("dto") AiEnvVariableDTO btAiEnvVariable);

  int deleteAiEnvVariableByIds(@Param("ids") List<Long> ids, @Param("updatorId") Long updatorId);

  List<AiEnvVariableDTO> selectAiEnvVariableList(@Param("query") AiQueryParams queryParams);
}
