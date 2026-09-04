package com.iwhalecloud.bote.doc.module.collaboration.socket.message;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消息处理器注册中心
 * 负责管理所有的消息处理器，并根据命令类型路由到对应的处理器
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class MessageRegistry {
  private static final Logger logger = LoggerFactory.getLogger(MessageRegistry.class);

  /**
   * 命令类型到处理器的映射
   */
  private final Map<String, MessageHandler<?>> handlerMap = new ConcurrentHashMap<>();

  /**
   * 处理器名称到处理器的映射
   */
  private final Map<String, MessageHandler<?>> handlerNameMap = new ConcurrentHashMap<>();

  private final List<MessageHandler<?>> messageHandlers;

  public MessageRegistry(List<MessageHandler<?>> messageHandlers) {
    this.messageHandlers = messageHandlers;
  }

  /**
   * 初始化注册所有处理器
   */
  @PostConstruct
  @SuppressWarnings("PMD.GuardLogStatement")
  public void init() {
    for (MessageHandler<?> handler : messageHandlers) {
      registerHandler(handler);
    }
    logger.info("消息处理器注册完成，共注册{}个处理器", handlerMap.size());
  }

  /**
   * 注册消息处理器
   *
   * @param handler 消息处理器
   */
  public void registerHandler(MessageHandler<?> handler) {
    // 如果处理器实现了CmdTypeAware接口，使用其提供的命令类型
    if (handler instanceof CommandAware) {
      Command cmdType = ((CommandAware) handler).getCommand();
      registerHandler(cmdType, handler);
    }
    else {
      // 否则根据处理器名称推断命令类型
      logger.warn("处理器{}未实现CmdTypeAware接口，无法自动注册", handler.getClass().getSimpleName());
    }
  }

  /**
   * 注册消息处理器
   *
   * @param cmd 命令类型
   * @param handler 消息处理器
   */
  public void registerHandler(Command cmd, MessageHandler<?> handler) {
    MessageHandler<?> existingHandler = handlerMap.put(buildHandleCacheKey(cmd), handler);
    handlerNameMap.put(handler.getHandlerName(), handler);

    if (existingHandler != null) {
      logger.warn("命令类型{}的处理器被覆盖: {} -> {}",
        cmd, existingHandler.getClass().getSimpleName(), handler.getClass().getSimpleName());
    }

    logger.info("注册消息处理器: command={}, handler={}, messageType={}",
      cmd, handler.getClass().getSimpleName(), handler.getMessageType().getSimpleName());
  }

  private String buildHandleCacheKey(Command command) {
    return command.getProtocol() + "_" + command.getCommand();
  }

  /**
   * 获取消息处理器
   *
   * @param command 命令类型
   * @return 消息处理器
   */
  public MessageHandler<?> getHandler(Command command) {
    return handlerMap.get(buildHandleCacheKey(command));
  }

  /**
   * 通过处理器名称获取处理器
   *
   * @param handlerName 处理器名称
   * @return 消息处理器
   */
  public MessageHandler<?> getHandlerByName(String handlerName) {
    return handlerNameMap.get(handlerName);
  }

  /**
   * 获取所有已注册的处理器
   *
   * @return 处理器列表
   */
  public java.util.Collection<MessageHandler<?>> getAllHandlers() {
    return handlerMap.values();
  }

  /**
   * 命令类型感知接口
   * 处理器实现此接口可以自动注册到对应的命令类型
   */
  public interface CommandAware {
    /**
     * 获取处理器支持的命令类型
     *
     * @return 命令类型
     */
    Command getCommand();
  }
}
