package com.iwhalecloud.bote.doc.module.collaboration.socket.handler;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.AbstractMessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.Command;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.ExtractedMessage;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.MessageHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.MessageProtocol;
import com.iwhalecloud.bote.doc.module.collaboration.socket.message.MessageRegistry;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

/**
 * 消息协议抽奖处理器
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractMessageProtocol implements MessageProtocol {

  private static final Logger logger = LoggerFactory.getLogger(AbstractMessageProtocol.class);

  @Autowired
  private MessageRegistry messageRegistry;

  /**
   * 处理接收到的消息
   */
  public void channelRead(ChannelHandlerContext ctx, String message) {
    try {
      ExtractedMessage extractedMessage = extractMessage(message);
      if (extractedMessage == null) {
        return;
      }
      MessageHandler<?> handler = getMessageHandler(ctx, extractedMessage.getCommand());
      if (handler == null) {
        return;
      }
      // 类型安全的消息处理
      processMessage(ctx, handler, extractedMessage.getData());
    }
    catch (Exception e) {
      logger.error("消息处理异常: message={}, channel={}, error={}",
        message, ctx.channel().id().asShortText(), e.getMessage(), e);
    }
  }

  /**
   * 解构消息
   *
   * @param message 消息内容
   * @return 格式化的消息
   */
  protected abstract ExtractedMessage extractMessage(String message);

  private @Nullable MessageHandler<?> getMessageHandler(ChannelHandlerContext ctx, Command command) {
    MessageHandler<?> handler = messageRegistry.getHandler(command);
    if (handler == null) {
      logger.warn("未找到命令处理器: cmd={}, protocol={}, channel={}",
        command.getCommand(), command.getProtocol(), ctx.channel().id().asShortText());
      return null;
    }
    return handler;
  }

  /**
   * 类型安全的消息处理
   */
  @SuppressWarnings("unchecked")
  private void processMessage(ChannelHandlerContext ctx, MessageHandler<?> handler, Object data) {
    try {
      if (handler instanceof AbstractMessageHandler) {
        AbstractMessageHandler<?> abstractHandler = (AbstractMessageHandler<?>) handler;
        Object message = abstractHandler.convertMessage(data);
        ((MessageHandler<Object>) handler).handle(ctx, message);
      }
      else {
        // 直接处理（可能会抛出ClassCastException）
        ((MessageHandler<Object>) handler).handle(ctx, data);
      }

    }
    catch (Exception e) {
      logger.error("消息转换或处理失败: handler={}, dataType={}, error={}",
        handler.getHandlerName(),
        data != null ? data.getClass().getSimpleName() : "null", e.getMessage(), e);
    }
  }
}
