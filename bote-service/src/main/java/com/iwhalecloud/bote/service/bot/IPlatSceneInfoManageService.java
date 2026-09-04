package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.bot.BotSceneDTO;
import com.iwhalecloud.bote.dto.bot.PlatSceneInfoDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatSceneInfoQueryParams;
import com.iwhalecloud.bote.dto.scene.SimpleSceneDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 模板智能体管理服务
 *
 * @author auto
 * @since 2025-06-21
 */
public interface IPlatSceneInfoManageService {

  /**
   * 查询单个模板智能体
   *
   * @param platSceneId 模板ID
   * @return 模板智能体
   */
  PlatSceneInfoDTO findPlatSceneInfo(Long platSceneId);

  /**
   * 保存模板智能体
   *
   * @param platSceneInfo 模板智能体
   * @return 结果
   */
  ResultVO<PlatSceneInfoDTO> savePlatSceneInfo(PlatSceneInfoDTO platSceneInfo);

  /**
   * 删除模板智能体
   *
   * @param platSceneId 模板ID
   * @return 结果
   */
  ResultVO<Void> deletePlatSceneInfo(Long platSceneId);

  /**
   * 置顶模板智能体
   *
   * @param platSceneId 模板ID
   * @return 结果
   */
  ResultVO<Void> topPlatSceneInfo(Long platSceneId);

  /**
   * 查询模板智能体列表
   *
   * @param queryParams 查询条件
   * @return 模板智能体列表
   */
  List<PlatSceneInfoDTO> queryPlatSceneInfoList(PlatSceneInfoQueryParams queryParams);

  /**
   * 查询模板智能体列表（分页）
   *
   * @param queryParams 查询条件
   * @return 模板智能体分页列表
   */
  PageInfo<PlatSceneInfoDTO> queryPlatSceneInfoPage(PlatSceneInfoQueryParams queryParams);

  /**
   * 获取可选择的场景列表（用于新增模板时的下拉选择）
   *
   * @param searchContent 搜索关键词
   * @return 场景列表
   */
  List<PlatSceneInfoDTO> queryAvailableScenes(String searchContent);


  ResultVO<BotSceneDTO> copyPlatScene(Long tenantId, Long platSceneId);

  /**
   * 查询智能体列表（分页）
   *
   * @param queryParams 查询条件
   * @return 查询智能体分页列表
   */
  PageInfo<SimpleSceneDTO> querySimpleScenePage(PlatSceneInfoQueryParams queryParams);
}
