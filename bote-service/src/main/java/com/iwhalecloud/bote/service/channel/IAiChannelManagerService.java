package com.iwhalecloud.bote.service.channel;

import com.iwhalecloud.bote.dto.channel.AiChannelDTO;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;

/**
 * 渠道管理服务
 *
 * @author wangtingyun
 * @since 2026-03-09
 */
public interface IAiChannelManagerService {

  /**
   * 保存渠道
   *
   * @param dto 渠道
   * @return 结果
   */
  ResultVO<AiChannelDTO> saveAiChannel(AiChannelDTO dto);

  /**
   * 查询渠道列表
   *
   * @param spaceId 空间 ID
   * @param botId 应用 ID
   * @return 渠道列表
   */
  List<AiChannelDTO> queryAiChannelList(Long spaceId, Long botId);

  /**
   * 查询已启用的渠道列表
   *
   * @param spaceId 空间 ID
   * @param botId 应用 ID
   * @return 渠道列表
   */
  List<AiChannelDTO> queryEnabledAiChannelList(Long spaceId, Long botId);
}
