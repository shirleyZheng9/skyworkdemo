package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.BotAuthDTO;
import com.iwhalecloud.bote.dto.bot.query.BotAuthQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 机器人授权管理
 *
 * @author auto
 * @since 2025-03-04
 */
public interface BotAuthManageMapper {
  /**
   * 查询授权列表
   *
   * @param botId 机器人ID
   * @return 结果
   */
  List<BotAuthDTO> selectBotAuthList(@Param("botId") Long botId);

  /**
   * 根据主键获取机器人授权
   *
   * @param authId 机器人授权主键
   * @return 机器人授权
   */
  BotAuthDTO getBotAuth(@Param("id") Long authId);

  /**
   * 新增机器人授权
   */
  int insertBotAuth(@Param("dto") BotAuthDTO botAuth);

  /**
   * 批量新增机器人授权
   */
  int batchInsertBotAuth(@Param("list") List<BotAuthDTO> botAuths);

  /**
   * 修改机器人授权
   */
  int updateBotAuth(@Param("dto") BotAuthDTO botAuth);

  /**
   * 删除机器人授权
   *
   * @param authId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteBotAuth(@Param("authId") Long authId, @Param("updatorId") Long updatorId);

  /**
   * 获取机器人授权列表（分页）
   *
   * @param queryParams 查询条件
   * @return 机器人授权分页列表
   */
  Page<BotAuthDTO> selectBotAuthPage(@Param("query") BotAuthQueryParams queryParams, RowBounds rowBounds);

  /**
   * 批量删除机器人授权
   *
   * @param deleteAuthIds 删除授权 ID 列表
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int batchDeleteAuth(@Param("list") List<Long> deleteAuthIds, @Param("updatorId") Long updatorId);

  /**
   * 根据用户 ID 查询授权
   *
   * @param userId 用户 ID
   * @return 授权列表
   */
  List<BotAuthDTO> selectByUserId(@Param("userId") Long userId);

  /**
   * 根据租户 ID 查询授权
   *
   * @param tenantId 租户 ID
   * @return 授权列表
   */
  List<BotAuthDTO> selectByTenantId(@Param("tenantId") Long tenantId);

  /**
   * 根据组织 ID 查询授权
   *
   * @param orgId 组织 ID
   * @return 授权列表
   */
  List<BotAuthDTO> selectByOrgId(@Param("orgId") Long orgId);

  /**
   * 根据用户 ID 和机器人 ID 删除授权
   *
   * @param userId 用户ID
   * @param botId 机器人ID
   * @param updatorId 操作人ID
   * @return 结果
   */
  int deleteAuthByUserIdAndBotId(@Param("userId") Long userId, @Param("botId") Long botId, @Param("updatorId") Long updatorId);

  /**
   * 根据机器人 ID 删除授权
   *
   * @param botId 机器人 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteAuthByBotId(@Param("botId") Long botId, @Param("updatorId") Long updatorId);

  /**
   * 批量更新机器人授权
   * @param botAuth 机器人授权
   */
  int batchUpdateBotAuth(@Param("dto") BotAuthDTO botAuth);

  /**
   * 分页查询授权应用列表（授权管理使用）
   *
   * @return 授权列表
   */
  Page<BotAuthDTO> selectPageForManage(@Param("query") BotAuthQueryParams queryParams, RowBounds rowBounds);

  /**
   * 根据授权ID列表查询授权信息列表
   *
   * @param authIds 授权ID列表
   * @return 授权信息列表
   */
  List<BotAuthDTO> selectListByAuthIds(@Param("authIds") List<Long> authIds);

  /**
   * 修改授权状态
   *
   * @param botId 应用ID
   * @param authStatus 授权状态
   * @param authOperator 操作人
   * @return 修改结果
   */
  int updateAuthStatus(@Param("botId")Long botId, @Param("authStatus")String authStatus, @Param("authOperator")Long authOperator);

  /**
   * 修改授权操作信息
   *
   * @param botId 应用ID
   * @param authOperator 操作人
   * @return 修改结果
   */
  int updateAuthOperateInfo(@Param("botId")Long botId, @Param("authOperator")Long authOperator);

  /**
   * 修改授权基本信息
   */
  int updateBotAuthBasicInfo(@Param("dto") BotAuthDTO botAuth);

}
