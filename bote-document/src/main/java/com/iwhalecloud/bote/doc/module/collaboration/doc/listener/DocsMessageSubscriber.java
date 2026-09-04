package com.iwhalecloud.bote.doc.module.collaboration.doc.listener;

import com.iwhalecloud.bote.doc.module.collaboration.constant.SocketBizEnum;
import com.iwhalecloud.bote.doc.module.collaboration.doc.dto.NodePublishEvent;
import com.iwhalecloud.bote.doc.module.collaboration.pubsub.MessageSubscriber;
import com.iwhalecloud.bote.doc.module.collaboration.socket.service.SocketMessageSendService;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.GenericCmdType;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.generic.model.SocketSendInfo;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 文档协作消息订阅监听器
 * 实现通用的 MessageSubscriber 接口，不依赖具体的消息中间件实现
 *
 * @author Aiqing
 * @since 2025/8/30
 */
@Component
@RequiredArgsConstructor
public class DocsMessageSubscriber implements MessageSubscriber {
  private static final Logger logger = LoggerFactory.getLogger(DocsMessageSubscriber.class);

  private static final String SUBSCRIBE_KEY_PREFIX = "dc:documentCollab:";

  private final SocketMessageSendService socketMessageSendService;

  public String encodeSubscribeKey(String documentId) {
    return SUBSCRIBE_KEY_PREFIX + documentId;
  }

  public String decodeSubscribeKey(String channelKey) {
    return channelKey.replace(SUBSCRIBE_KEY_PREFIX, "");
  }

  @Override
  public void onMessage(String channel, String message) {
    if (logger.isTraceEnabled()) {
      logger.trace("【文档协作消息监听】收到消息 - channel={}, messageLength={}", channel, message.length());
    }

    String documentId = this.decodeSubscribeKey(channel);
    try {
      NodePublishEvent nodePublishEvent = JsonUtil.parseJson(message, NodePublishEvent.class);

      SocketSendInfo<NodePublishEvent> genericMessage =
        socketMessageSendService.createGenericMessage(GenericCmdType.TIPTAP_COLLAB_PUB, nodePublishEvent);

      socketMessageSendService.sendToBusiness(SocketBizEnum.NODEJS_SIDECAR.name(), documentId, genericMessage);

      logger.trace("【文档协作消息监听】消息转发完成 - documentId={}", documentId);
    }
    catch (Exception e) {
      logger.error("【文档协作消息监听】处理文档广播消息异常, channel={}, message={}", channel, message, e);
      // 异常后忽略
    }
  }
}
