package com.iwhalecloud.bote.service.bot;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.bot.BotAuthApplyDTO;
import com.iwhalecloud.bote.dto.bot.query.BotAuthApplyParams;

/**
 * 智能应用发布申请 Service
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
public interface IBotAuthApplyService {

  /**
   * 申请应用授权
   *
   * @param applyDTO 申请参数
   */
  Boolean applyBotAuth(BotAuthApplyDTO applyDTO);

  /**
   * 分页查询应用授权审核列表
   *
   * @param params 查询参数
   * @return 应用授权申请列表
   */
  PageInfo<BotAuthApplyDTO> getBotAuditPage(BotAuthApplyParams params);

  /**
   * 分页查询应用授权申请列表
   *
   * @param params 查询参数
   * @return 应用授权申请列表
   */
  PageInfo<BotAuthApplyDTO> getBotApplyPage(BotAuthApplyParams params);

  /**
   * 审核应用授权申请
   *
   * @param params 审核参数
   */
  void auditBotAuthApply(BotAuthApplyParams params);

  /**
   * 获取授权申请详情（后台管理审核使用）
   *
   * @param applyId 申请ID
   * @return 授权申请详情
   */
  BotAuthApplyDTO getBotAuthApplyDetail(Long applyId);

  /**
   * 获取授权申请详情
   * @param applyId 申请ID
   * @return 授权申请详情
   */
  BotAuthApplyDTO getBotAuthApply(Long applyId);

}
