package com.iwhalecloud.bote.mapper.bot;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.bot.PlatBotInfoDTO;
import com.iwhalecloud.bote.dto.bot.TemplateBotDTO;
import com.iwhalecloud.bote.dto.bot.query.PlatBotInfoQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 模板应用信息管理
 *
 * @author auto
 * @since 2025-05-26
 */
public interface PlatBotInfoManageMapper {
  /**
   * 校验应用模板的编码唯一性
   *
   * @param platBotInfo 应用模板
   * @return 结果
   */
  boolean existsPlatBotInfoCode(@Param("dto") PlatBotInfoDTO platBotInfo);

  /**
   * 校验应用模板的唯一性
   *
   * @param platBotInfo 应用模板
   * @return 结果
   */
  boolean existsPlatBotInfo(@Param("dto") PlatBotInfoDTO platBotInfo);

  /**
   * 获取平台应用模板
   *
   * @param platBotId 应用模板ID
   * @return 应用模板
   */
  TemplateBotDTO getPlatBot(@Param("id") Long platBotId);

  /**
   * 根据主键获取应用模板
   *
   * @param platBotId 模板应用主键
   * @return 应用模板
   */
  PlatBotInfoDTO getPlatBotInfo(@Param("id") Long platBotId);

  /**
   * 根据应用 ID 获取应用模板
   *
   * @param ownerTenantId 租户 ID
   * @param botId 应用 ID
   * @return 应用模板
   */
  PlatBotInfoDTO getPlatBotByBotId(@Param("ownerTenantId") Long ownerTenantId, @Param("botId") Long botId);

  /**
   * 新增应用模板
   *
   * @param platBotInfo 应用模板
   * @return 结果
   */
  int insertPlatBotInfo(@Param("dto") PlatBotInfoDTO platBotInfo);

  /**
   * 批量新增应用模板
   *
   * @param platBotInfos 应用模板列表
   * @return 结果
   */
  int batchInsertPlatBotInfo(@Param("list") List<PlatBotInfoDTO> platBotInfos);

  /**
   * 修改应用模板
   *
   * @param platBotInfo 应用模板
   * @return 结果
   */
  int updatePlatBotInfo(@Param("dto") PlatBotInfoDTO platBotInfo);

  /**
   * 删除属性
   *
   * @param platBotId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deletePlatBotInfo(@Param("platBotId") Long platBotId, @Param("updatorId") Long updatorId);

  /**
   * 更新应用模板状态
   *
   * @param platBotId 主键 ID
   * @param updatorId 操作人
   * @param status 状态
   * @return 结果
   */
  int updatePlatBotInfoStatus(@Param("platBotId") Long platBotId, @Param("updatorId") Long updatorId, @Param("status") String status);

  /**
   * 获取应用模板列表
   *
   * @param queryParams 查询条件
   * @return 应用模板列表
   */
  List<PlatBotInfoDTO> selectPlatBotInfoList(@Param("query") PlatBotInfoQueryParams queryParams);

  /**
   * 获取应用模板列表（分页）
   *
   * @param queryParams 查询条件
   * @return 应用模板分页列表
   */
  Page<PlatBotInfoDTO> selectPlatBotInfoPage(@Param("query") PlatBotInfoQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询平台和用户发布的应用模板（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 查询分页
   * @return 应用模板分页列表
   */
  Page<TemplateBotDTO> queryPlatAndUserBotPage(@Param("query") PlatBotInfoQueryParams queryParams,  RowBounds rowBounds);

  /**
   * 根据授权ID获取用户发布的应用模板
   *
   * @param authId 授权ID
   * @return 应用模板
   */
  TemplateBotDTO getUserBot(@Param("authId") Long authId);

  /**
   * 应用广场：分页查询用户发布的应用（AI门户使用）
   *
   * @param queryParams 查询条件
   * @param rowBounds 查询分页
   * @return 应用列表
   */
  Page<TemplateBotDTO> selectUserAuthBotPage(@Param("query") PlatBotInfoQueryParams queryParams, RowBounds rowBounds);
}
