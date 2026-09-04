package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.dto.chat.SessionMsgFileDTO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会话消息附件
 *
 * @author chen.linfa
 * @since 2024-08-19
 */
public interface SessionMsgFileMapper {

  /**
   * 获取会话消息附件列表
   *
   * @param sessionId 会话 ID
   * @return 会话消息附件列表
   */
  List<SessionMsgFileDTO> selectSessionMsgFiles(@Param("sessionId") Long sessionId);

  /**
   * 批量保存会话消息附件
   *
   * @param msgFiles 附件
   * @return 结果
   */
  int batchInsertSessionMsgFile(@Param("list") List<SessionMsgFileDTO> msgFiles);
}
