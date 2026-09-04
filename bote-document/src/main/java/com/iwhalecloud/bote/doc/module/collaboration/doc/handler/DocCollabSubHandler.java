package com.iwhalecloud.bote.doc.module.collaboration.doc.handler;

import com.iwhalecloud.bote.doc.module.collaboration.cache.SocketServerCache;
import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketBizEnum;
import com.iwhalecloud.bote.doc.module.collaboration.doc.dto.NodeSubEvent;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.handler.AbstractGenericProtocolMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 文档协作订阅消息处理
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Component
@RequiredArgsConstructor
public class DocCollabSubHandler extends AbstractGenericProtocolMessageHandler<NodeSubEvent> {

  private static final Logger logger = LoggerFactory.getLogger(DocCollabSubHandler.class);

  private static final String SUB_ACTION_SUBSCRIBE = "subscribe";

  private final ChannelSessionManager channelSessionManager;
  private final SocketServerCache socketServerCache;

  @Override
  protected void doHandle(ChannelHandlerContext ctx, NodeSubEvent message) {
    String documentId = message.getDocumentId();
    if (StringUtils.isBlank(documentId)) {
      return;
    }
    String action = message.getAction();
    SessionInfo sessionInfo = getSessionInfo(ctx);
    if (sessionInfo == null) {
      logger.warn("会话信息不存在，无法处理");
      return;
    }
    if (SUB_ACTION_SUBSCRIBE.equalsIgnoreCase(action)) {
      channelSessionManager.subscribeBusiness(sessionInfo.getSessionId(), SocketBizEnum.NODEJS_SIDECAR.name(), documentId);
      socketServerCache.subscribeForCollab(documentId);
      socketServerCache.addDocumentCollabUsers(documentId, message.getUserId());
    }
    else {
      channelSessionManager.unsubscribeBusiness(sessionInfo.getSessionId(), SocketBizEnum.NODEJS_SIDECAR.name(), documentId);
      socketServerCache.unsubscribeForCollab(documentId);
      socketServerCache.removeDocumentCollabUser(documentId, message.getUserId());
    }
  }

  @Override
  public GenericCmdType getCmdType() {
    return GenericCmdType.TIPTAP_COLLAB_SUB;
  }
}
