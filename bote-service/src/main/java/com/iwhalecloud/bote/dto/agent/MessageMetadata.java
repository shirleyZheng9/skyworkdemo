package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.dto.chat.SessionMsgExtParamsDTO;
import java.util.Date;
import lombok.Builder;

/**
 * 消息元数据
 *
 * @param msgType 消息类型
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param extParams 扩展参数
 * @author bianjp
 * @since 2026-03-18
 */
@Builder
public record MessageMetadata(ChatMessageType msgType,
                              Date startTime,
                              Date endTime,
                              SessionMsgExtParamsDTO extParams) {
}
