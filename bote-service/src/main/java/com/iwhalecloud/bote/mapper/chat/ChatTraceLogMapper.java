package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.dto.chat.ChatTraceLogDTO;
import com.iwhalecloud.bote.dto.chat.vo.ChatTraceLogVO;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会话日志
 *
 * @author bianjp
 * @since 2025-01-08
 */
public interface ChatTraceLogMapper {

  /**
   * 批量插入会话日志
   */
  int batchInsert(@Param("logs") List<ChatTraceLogDTO> logs);

  /**
   * 根据消息 ID 查询日志列表
   */
  List<ChatTraceLogVO> selectByMsgId(@Param("msgId") Long msgId);

  /**
   * 删除指定日期前的日志
   */
  int deleteByMaxDate(@Param("maxDate") Date maxDate, @Param("limit") int limit);
}
