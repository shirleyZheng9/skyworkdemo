package com.iwhalecloud.bote.doc.module.collaboration.socket.transport;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.nio.channels.spi.SelectorProvider;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty传输层工厂类
 * 提供EventLoopGroup和ServerChannel的创建
 *
 * @author Aiqing
 * @since 2025/1/27
 */
@UtilityClass
@SuppressWarnings("PMD.GuardLogStatement")
public class NettyTransportFactory {

  private static final Logger logger = LoggerFactory.getLogger(NettyTransportFactory.class);

  /**
   * 检查Epoll是否可用
   */
  public static boolean isEpollAvailable() {
    try {
      Class.forName("io.netty.channel.epoll.Epoll");
      return io.netty.channel.epoll.Epoll.isAvailable();
    }
    catch (ClassNotFoundException e) {
      logger.debug("Epoll类不可用: {}", e.getMessage());
      return false;
    }
    catch (UnsatisfiedLinkError e) {
      logger.debug("Epoll本地库不可用: {}", e.getMessage());
      return false;
    }
  }

  /**
   * 自动选择最佳传输类型
   */
  public static TransportType getBestTransportType(boolean preferEpoll) {
    if (preferEpoll && isEpollAvailable()) {
      return TransportType.EPOLL;
    }
    return TransportType.NIO;
  }

  /**
   * 创建EventLoopGroup
   *
   * @param transportType 传输类型
   * @param nThreads 线程数（0表示使用默认值）
   * @param namePrefix 线程名前缀
   * @return EventLoopGroup实例
   */
  public static EventLoopGroup createEventLoopGroup(TransportType transportType, int nThreads, String namePrefix) {
    int threads = calculateThreadCount(nThreads);
    DefaultThreadFactory threadFactory = new DefaultThreadFactory(namePrefix, true);

    switch (transportType) {
      case EPOLL:
        return createEpollEventLoopGroup(threads, threadFactory);
      case NIO:
      default:
        return createNioEventLoopGroup(threads, threadFactory);
    }
  }

  /**
   * 获取对应的ServerChannel类
   */
  public static Class<? extends ServerChannel> getServerChannelClass(TransportType transportType) {
    switch (transportType) {
      case EPOLL:
        return EpollServerSocketChannel.class;
      case NIO:
      default:
        return NioServerSocketChannel.class;
    }
  }

  /**
   * 计算线程数量
   */
  private static int calculateThreadCount(int nThreads) {
    if (nThreads <= 0) {
      // 使用CPU核心数的2倍作为默认值
      int processors = Runtime.getRuntime().availableProcessors();
      return Math.max(1, processors * 2);
    }
    return nThreads;
  }

  /**
   * 创建Epoll EventLoopGroup
   */
  private static EventLoopGroup createEpollEventLoopGroup(int nThreads, DefaultThreadFactory threadFactory) {
    try {
      return new MultiThreadIoEventLoopGroup(nThreads, threadFactory,
        EpollIoHandler.newFactory(0, DefaultSelectStrategyFactory.INSTANCE));
    }
    catch (Exception e) {
      logger.warn("创建EpollEventLoopGroup失败，降级到NIO: {}", e.getMessage());
      return createNioEventLoopGroup(nThreads, threadFactory);
    }
  }

  /**
   * 创建NIO EventLoopGroup
   */
  private static EventLoopGroup createNioEventLoopGroup(int nThreads, DefaultThreadFactory threadFactory) {
    return new MultiThreadIoEventLoopGroup(nThreads, threadFactory,
      NioIoHandler.newFactory(SelectorProvider.provider(), DefaultSelectStrategyFactory.INSTANCE));
  }

  /**
   * 传输类型枚举
   */
  @Getter
  public enum TransportType {
    NIO("NIO传输"),
    EPOLL("Epoll传输");

    private final String description;

    TransportType(String description) {
      this.description = description;
    }

  }
}
