package com.iwhalecloud.bote.mapper.chat;

import com.iwhalecloud.bote.dto.chat.ReplyDownloadInfoDTO;
import com.iwhalecloud.bote.entity.chat.SessionMsgTextEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 会话消息内容管理
 *
 * @author auto
 * @since 2024-10-28
 */
public interface SessionMsgTextMapper {

  /**
   * 批量新增会话消息内容
   *
   * @param sessionMsgTexts 会话消息内容列表
   * @return 结果
   */
  int batchInsertSessionMsgText(@Param("list") List<SessionMsgTextEntity> sessionMsgTexts);

  /**
   * 更新消息的记忆内容
   */
  int updateMemoryContent(@Param("msgId") Long msgId, @Param("memoryContent") String memoryContent);

  /**
   * 根据消息 ID 查询文件下载信息
   */
  ReplyDownloadInfoDTO selectDownloadInfoByMsgId(@Param("msgId") Long msgId);

  /**
   * 收集段落相关回复内容
   *
   * @param transactionId 事务 ID
   * @param group 段落分组
   * @return 段落回复内容
   */
  List<String> selectParagraphReply(@Param("transactionId") Long transactionId, @Param("group") String group);
}
