package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.BotAuthApplyDTO;
import com.iwhalecloud.bote.dto.bot.query.BotAuthApplyParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 智能应用授权申请 Mapper
 *
 * @author wang.tingyun
 * @since 2025-08-28
 */
public interface BotAuthApplyMapper {

  /**
   * 新增应用授权申请
   *
   * @param dto 应用授权申请实体
   * @return 新增结果
   */
  int insertBotAuthApply(@Param("dto") BotAuthApplyDTO dto);

  /**
   * 根据主键applyId获取授权申请记录
   * @param applyId 主键
   * @return 授权申请记录
   */
  BotAuthApplyDTO selectByApplyId(@Param("applyId") Long applyId);

  /**
   * 根据主键applyId查询授权申请记录基本信息
   *
   * @param applyId 主键
   * @return 授权申请记录信息
   */
  BotAuthApplyDTO selectBasicInfoByApplyId(@Param("applyId") Long applyId);

  /**
   * 分页查询应用授权申请列表
   *
   * @param query 查询参数
   * @return 审核列表
   */
  Page<BotAuthApplyDTO> selectBotApplyPage(@Param("query") BotAuthApplyParams query, RowBounds rowBounds);

  /**
   * 分页查询应用授权审核列表
   *
   * @param query 查询参数
   * @return 审核列表
   */
  Page<BotAuthApplyDTO> selectBotAuditPage(@Param("query") BotAuthApplyParams query, RowBounds rowBounds);

  /**
   * 根据主键applyId获取授权申请详情
   *
   * @param applyId 主键
   * @return 授权申请记录
   */
  BotAuthApplyDTO selectDetailByApplyId(@Param("applyId") Long applyId);

  /**
   * 修改申请审核状态
   *
   * @param params 修改审核参数
   * @return 修改结果
   */
  int updateBotAuditStatus(@Param("params") BotAuthApplyParams params);
  
  /**
   * 查询用户待审核的应用授权申请数量
   * 
   * @param auditStatus 用户ID
   * @return 待审核数量
   */
  int countByAuthStatus(@Param("auditStatus") Integer auditStatus);

}
