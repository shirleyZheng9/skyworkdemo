package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.bot.PlatBotInfoDTO;
import com.iwhalecloud.bote.dto.bot.TemplateBotDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatBotInfoQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 应用模板管理服务
 *
 * @author auto
 * @since 2025-05-26
 */
public interface IPlatBotInfoManageService {

  /**
   * 查询单个应用模板
   *
   * @param platBotId 应用模板主键
   * @return 语料基本信息
   */
  TemplateBotDTO findPlatBotInfo(Long platBotId);

  /**
   * 查询用户发布的应用模板
   *
   * @param platBotId 应用模板主键
   */
  TemplateBotDTO findUserBotInfo(Long platBotId);

  /**
   * 保存应用模板
   *
   * @param platBotInfo 应用模板
   * @return 结果
   */
  ResultVO<PlatBotInfoDTO> savePlatBotInfo(PlatBotInfoDTO platBotInfo);

  /**
   * 删除应用模板
   *
   * @param platBotId 应用模板主键
   * @return 结果
   */
  ResultVO<Void> deletePlatBotInfo(Long platBotId);

  /**
   * 查询应用模板列表
   *
   * @param queryParams 查询条件
   * @return 应用模板列表
   */
  List<PlatBotInfoDTO> queryPlatBotInfoList(PlatBotInfoQueryParams queryParams);

  /**
   * 查询应用模板列表（分页）
   *
   * @param queryParams 查询条件
   * @return 应用模板基本信息分页列表
   */
  PageInfo<PlatBotInfoDTO> queryPlatBotInfoPage(PlatBotInfoQueryParams queryParams);

  /**
   * 查询语料基本信息列表（分页）
   *
   * @param queryParams 查询条件
   * @return 语料基本信息分页列表
   */
  PageInfo<TemplateBotDTO> queryPlatAndUserBotPage(PlatBotInfoQueryParams queryParams);

  /**
   * 发布应用模板
   *
   * @param platBotId 应用模板主键
   * @param status 发布状态
   * @return 结果
   */
  ResultVO<Void> publishPlatBot(Long platBotId, String status);

  /**
   * 下架用户发布的应用模板
   *
   * @param botId 机器人ID
   * @return 结果
   */
  ResultVO<Void> removeBot(Long botId);

  /**
   * 应用广场：分页查询用户授权的应用
   *
   * @param queryParams 查询条件
   * @return 发布的应用信息
   */
  PageInfo<TemplateBotDTO> queryUserAuthBotPage(PlatBotInfoQueryParams queryParams);
}
