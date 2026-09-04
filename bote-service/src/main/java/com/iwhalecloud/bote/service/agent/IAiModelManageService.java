package com.iwhalecloud.bote.service.agent;

import com.iwhalecloud.bote.dto.agent.AiModelDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 新增表记录启用模型管理服务
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface IAiModelManageService {

  /**
   * 保存新增表记录启用模型
   *
   * @param model 新增表记录启用模型
   * @return 结果
   */
  ResultVO<AiModelDTO> saveAiModel(AiModelDTO model);

  /**
   * 查询新增表记录启用模型列表
   *
   * @param queryParams 查询条件
   * @return 新增表记录启用模型列表
   */
  List<AiModelDTO> queryAiModelList(AiQueryParams queryParams);
}
