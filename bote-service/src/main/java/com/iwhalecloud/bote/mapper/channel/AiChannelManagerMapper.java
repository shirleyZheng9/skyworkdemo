package com.iwhalecloud.bote.mapper.channel;

import com.iwhalecloud.bote.dto.channel.AiChannelDTO;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 渠道管理 Mapper
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
public interface AiChannelManagerMapper {

  /**
   * 根据主键获取渠道
   */
  AiChannelDTO getAiChannel(@Param("id") Long id);

  /**
   * 新增渠道
   *
   * @param dto 渠道
   * @return 结果
   */
  int insertAiChannel(@Param("dto") AiChannelDTO dto);

  /**
   * 修改渠道
   *
   * @param dto 渠道
   * @return 结果
   */
  int updateAiChannel(@Param("dto") AiChannelDTO dto);

  /**
   * 获取用户配置的渠道列表
   *
   * @param spaceId 空间ID
   * @param botId 应用ID
   * @param userId 用户ID
   * @return 渠道列表
   */
  List<AiChannelDTO> selectAiChannelList(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 获取启用的渠道列表（用于选择渠道场景）
   *
   * @param spaceId 空间ID
   * @param botId 应用ID
   * @param userId 用户ID
   * @return 渠道列表
   */
  List<AiChannelDTO> selectEnabledAiChannelList(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId);

  /**
   * 逻辑删除：将指定空间、应用、创建人下有效渠道置为 00X（与 selectAiChannelList 条件一致）
   */
  int deleteAiChannelsBySpaceBotUser(@Param("spaceId") Long spaceId, @Param("botId") Long botId, @Param("userId") Long userId);
}
