package com.iwhalecloud.bote.mapper.agent;

import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 新增表记录启用模型管理
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface AiModelManageMapper {

  /**
   * 根据主键获取新增表记录启用模型
   */
  AiModelDTO getAiModel(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId,
    @Param("modelType") String modelType);

  /**
   * 新增新增表记录启用模型
   */
  int insertAiModel(@Param("dto") AiModelDTO model);

  /**
   * 修改新增表记录启用模型
   */
  int updateAiModel(@Param("dto") AiModelDTO model);

  /**
   * 获取新增表记录启用模型列表
   */
  List<AiModelDTO> selectAiModelList(@Param("query") AiQueryParams queryParams);
}
