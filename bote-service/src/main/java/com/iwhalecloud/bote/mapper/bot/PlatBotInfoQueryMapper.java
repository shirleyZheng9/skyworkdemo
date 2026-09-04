package com.iwhalecloud.bote.mapper.bot;

import com.iwhalecloud.bote.dto.bot.SimplePlatBotInfoDTO;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 模板应用信息查询 Mapper
 *
 * @author chen.linfa
 * @since 2025-09-18
 */
public interface PlatBotInfoQueryMapper {

  /**
   * 根据主键查询应用
   */
  SimplePlatBotInfoDTO getPlatBot(@Param("platBotId") Long platBotId);

  /**
   * 根据主键集合查询应用列表
   */
  List<SimplePlatBotInfoDTO> selectPlatBotListByIds(@Param("ids") Collection<Long> ids);

}
