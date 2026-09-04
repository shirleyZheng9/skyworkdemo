package com.iwhalecloud.bote.service.agent;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.agent.AiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.SimpleAiWorkspaceDTO;
import com.iwhalecloud.bote.dto.agent.query.AiQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 用户级的提示词管理服务
 *
 * @author linmengfan
 * @since 2026-03-05
 */
public interface IAiWorkspaceManageService {

  /**
   * @param id 空间文件里面的文件id
   * @param fileName 提示词文件名
   * @return 返回提示词内容
   */
  ResultVO<AiWorkspaceDTO> findAiWorkspace(Long id, String fileName, Long spaceId, Long botId);

  /**
   * 保存用户级的提示词
   *
   * @param btAiWorkspace 用户级的提示词
   * @return 结果
   */
  ResultVO<AiWorkspaceDTO> saveAiWorkspace(AiWorkspaceDTO btAiWorkspace);

  /**
   * 查询用户级的提示词列表
   *
   * @param queryParams
   *   查询条件
   * @return 用户级的提示词列表
   */
  List<AiWorkspaceDTO> queryAiWorkspaceList(AiQueryParams queryParams);

  /**
   * 授权类应用，将提供方自定义的系统提示词，私有化到当前用户
   */
  void syncAiWorkspace(Long spaceId, Long botId, Long userId, List<SimpleAiWorkspaceDTO> list);

  /**
   * 获取系统预置的提示词列表
   */
  List<AiWorkspaceDTO> getSystemAiWorkspaceList();

  /**
   * 生成提示词
   *
   * @param queryParams 参数
   * @return 生成的智能体内容
   */
  ResultVO<String> generateAiWorkspace(AiQueryParams queryParams);

  /**
   * 分页查询记忆列表
   */
  PageInfo<AiWorkspaceDTO> queryMemoryAiWorkspacePage(AiQueryParams queryParams);
}
