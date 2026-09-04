package com.iwhalecloud.bote.doc.module.collaboration.socket.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.SessionInfo;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.channel.ChannelHandlerContext;
import java.io.Serial;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象消息处理器
 * 提供通用的消息处理逻辑和类型安全的转换
 *
 * @param <T> 消息数据类型
 * @author Aiqing
 * @since 2025/08/29
 */
@SuppressWarnings("PMD.GuardLogStatement")
public abstract class AbstractMessageHandler<T> implements MessageHandler<T> {

  private static final Logger logger = LoggerFactory.getLogger(AbstractMessageHandler.class);
  private final Class<T> messageType;
  @Autowired
  protected ChannelSessionManager sessionManager;
  @Autowired
  protected ObjectMapper objectMapper;

  @SuppressWarnings("unchecked")
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  protected AbstractMessageHandler() {
    // 通过反射获取泛型类型
    Type genericSuperclass = getClass().getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
      Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
      if (actualTypeArguments.length > 0) {
        this.messageType = (Class<T>) actualTypeArguments[0];
      }
      else {
        throw new IllegalStateException("无法获取消息类型");
      }
    }
    else {
      throw new IllegalStateException("无法获取消息类型");
    }
  }

  /**
   * 安全的消息转换
   *
   * @param data 原始数据
   * @return 转换后的消息对象
   */
  public T convertMessage(Object data) {
    try {
      if (data == null) {
        return null;
      }

      // 如果已经是目标类型，直接返回
      if (messageType.isInstance(data)) {
        return messageType.cast(data);
      }

      // 如果是Map类型，通过Jackson转换
      if (data instanceof java.util.Map) {
        return objectMapper.convertValue(data, messageType);
      }

      // 如果是字符串，尝试JSON解析
      if (data instanceof String) {
        return objectMapper.readValue((String) data, messageType);
      }

      // 其他情况，通过Jackson转换
      return objectMapper.convertValue(data, messageType);

    }
    catch (Exception e) {
      logger.error("消息转换失败: sourceType={}, targetType={}, error={}",
        data.getClass().getSimpleName(), messageType.getSimpleName(), e.getMessage());
      throw new MessageConversionException("消息转换失败", e);
    }
  }

  /**
   * 获取当前会话信息
   *
   * @param ctx 通道上下文
   * @return 会话信息
   */
  protected SessionInfo getSessionInfo(ChannelHandlerContext ctx) {
    return ctx.channel().attr(ChannelSessionManager.SESSION_INFO_ATTR).get();
  }

  /**
   * 更新会话活跃时间
   *
   * @param ctx 通道上下文
   */
  protected void updateSessionActiveTime(ChannelHandlerContext ctx) {
    sessionManager.updateSessionActiveTime(ctx);
  }

  /**
   * 处理前的预处理逻辑
   *
   * @param ctx 通道上下文
   * @param message 消息数据
   * @return 是否继续处理
   */
  protected boolean preHandle(ChannelHandlerContext ctx, T message) {
    // 更新会话活跃时间
    updateSessionActiveTime(ctx);
    return true;
  }

  /**
   * 处理后的后处理逻辑
   *
   * @param ctx 通道上下文
   * @param message 消息数据
   */
  protected void postHandle(ChannelHandlerContext ctx, T message) {
    // 默认空实现，子类可以重写
  }

  @Override
  public final void handle(ChannelHandlerContext ctx, T message) {
    try {
      if (!preHandle(ctx, message)) {
        return;
      }

      doHandle(ctx, message);

      postHandle(ctx, message);

    }
    catch (Exception e) {
      logger.error("消息处理异常: handler={}, message={}, error={}",
        getHandlerName(), message, e.getMessage(), e);
      handleException(ctx, message, e);
    }
  }

  @Override
  public Class<T> getMessageType() {
    return messageType;
  }

  /**
   * 具体的消息处理逻辑，由子类实现
   *
   * @param ctx 通道上下文
   * @param message 消息数据
   */
  protected abstract void doHandle(ChannelHandlerContext ctx, T message);

  /**
   * 异常处理
   *
   * @param ctx 通道上下文
   * @param message 消息数据
   * @param e 异常
   */
  protected void handleException(ChannelHandlerContext ctx, T message, Exception e) {
    // 默认实现：记录日志，子类可以重写进行特殊处理
    logger.error("消息处理异常: {}", e.getMessage(), e);
  }

  /**
   * 消息转换异常
   */
  public static class MessageConversionException extends BssException {
    @Serial
    private static final long serialVersionUID = 1L;
    public MessageConversionException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
