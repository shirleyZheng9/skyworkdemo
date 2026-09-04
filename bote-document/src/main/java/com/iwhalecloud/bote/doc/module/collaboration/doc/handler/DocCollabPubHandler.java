package com.iwhalecloud.bote.doc.module.collaboration.doc.handler;

import com.iwhalecloud.bote.doc.module.collaboration.cache.SocketServerCache;
import com.iwhalecloud.bote.doc.module.collaboration.doc.dto.NodePublishEvent;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.handler.AbstractGenericProtocolMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 在线文档协作广播消息处理
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Component
@RequiredArgsConstructor
public class DocCollabPubHandler extends AbstractGenericProtocolMessageHandler<NodePublishEvent> {

  private final SocketServerCache socketServerCache;

  @Override
  protected void doHandle(ChannelHandlerContext ctx, NodePublishEvent message) {
    String documentId = message.getDocumentId();
    String content = message.getMessage();
    if (StringUtils.isBlank(documentId) || StringUtils.isBlank(content)) {
      return;
    }
    // 借助分布式中间件，发布消息
    socketServerCache.publish(documentId, message);
  }

  @Override
  public GenericCmdType getCmdType() {
    return GenericCmdType.TIPTAP_COLLAB_PUB;
  }
}
