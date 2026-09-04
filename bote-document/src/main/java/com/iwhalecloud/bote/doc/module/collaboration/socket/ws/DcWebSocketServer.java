package com.iwhalecloud.bote.doc.module.collaboration.socket.ws;

import com.iwhalecloud.bote.doc.module.collaboration.socket.SocketServer;
import com.iwhalecloud.bote.doc.module.collaboration.socket.auth.TokenValidator;
import com.iwhalecloud.bote.doc.module.collaboration.socket.config.WebSocketProperties;
import com.iwhalecloud.bote.doc.module.collaboration.socket.handler.CustomWebSocketServerProtocolHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.handler.ProtocolRouterHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.handler.WebSocketAuthHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.handler.WebSocketSessionHandler;
import com.iwhalecloud.bote.doc.module.collaboration.socket.session.ChannelSessionManager;
import com.iwhalecloud.bote.doc.module.collaboration.socket.transport.NettyTransportFactory;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.endecode.MessageProtocolDecoder;
import com.iwhalecloud.bote.doc.module.collaboration.socket.ws.endecode.MessageProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

/**
 * WebSocket服务器
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Component
@ConditionalOnBooleanProperty("bote.dc.websocket.enable")
public class DcWebSocketServer implements SocketServer {

  private static final Logger logger = LoggerFactory.getLogger(DcWebSocketServer.class);

  private final ProtocolRouterHandler protocolRouterHandler;
  private final ChannelSessionManager sessionManager;
  private final TokenValidator tokenValidator;
  private final WebSocketProperties properties;

  private volatile boolean ready = false;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel serverChannel;

  public DcWebSocketServer(ProtocolRouterHandler protocolRouterHandler,
                           ChannelSessionManager sessionManager,
                           TokenValidator tokenValidator,
                           WebSocketProperties properties) {
    this.protocolRouterHandler = protocolRouterHandler;
    this.sessionManager = sessionManager;
    this.tokenValidator = tokenValidator;
    this.properties = properties;
  }

  @Override
  public boolean isReady() {
    return ready;
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public void start() {
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();

      // 选择最佳传输类型
      NettyTransportFactory.TransportType transportType = NettyTransportFactory.getBestTransportType(
        properties.isEnableEpoll());

      // 创建EventLoopGroup
      bossGroup = NettyTransportFactory.createEventLoopGroup(
        transportType, properties.getBossThreads(), "websocket-boss");
      workerGroup = NettyTransportFactory.createEventLoopGroup(
        transportType, properties.getWorkerThreads(), "websocket-worker");

      // 设置ServerChannel类型
      Class<? extends io.netty.channel.ServerChannel> channelClass =
        NettyTransportFactory.getServerChannelClass(transportType);
      bootstrap.channel(channelClass);

      logger.info("WebSocket服务器使用 {} 传输层", transportType.getDescription());

      bootstrap.group(bossGroup, workerGroup)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new WebSocketChannelInitializer())
        // TCP参数优化
        .option(ChannelOption.SO_BACKLOG, properties.getSoBacklog())
        .option(ChannelOption.SO_REUSEADDR, properties.isSoReuseaddr())
        .childOption(ChannelOption.SO_KEEPALIVE, properties.isSoKeepalive())
        .childOption(ChannelOption.TCP_NODELAY, properties.isTcpNodelay())
        .childOption(ChannelOption.SO_RCVBUF, properties.getSoRcvBuf())
        .childOption(ChannelOption.SO_SNDBUF, properties.getSoSndBuf())
        // 写缓冲区水位线设置
        .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
          new WriteBufferWaterMark(properties.getWriteBufferLowWaterMark(),
            properties.getWriteBufferHighWaterMark()));

      // 绑定端口并启动服务器
      ChannelFuture future = bootstrap.bind(properties.getPort()).sync();
      serverChannel = future.channel();

      this.ready = true;
      logger.info("WebSocket服务器启动成功: port={}, path={}, maxContentLength={}, idleTimeout={}s",
        properties.getPort(), properties.getPath(), properties.getMaxContentLength(), properties.getIdleTimeout());

    }
    catch (Exception e) {
      logger.error("WebSocket服务器启动失败", e);
      stop();
      throw new RuntimeException("WebSocket服务器启动失败", e);
    }
  }

  @Override
  public void stop() {
    this.ready = false;

    try {
      if (serverChannel != null) {
        serverChannel.close().sync();
      }
    }
    catch (InterruptedException e) {
      logger.warn("关闭服务器通道时被中断", e);
      Thread.currentThread().interrupt();
    }

    shutdownEventLoopGroup(bossGroup, "boss");
    shutdownEventLoopGroup(workerGroup, "worker");

    logger.info("WebSocket服务器已停止");
  }

  /**
   * 优雅关闭EventLoopGroup
   */
  private void shutdownEventLoopGroup(EventLoopGroup group, String name) {
    if (group != null && !group.isShuttingDown()) {
      try {
        group.shutdownGracefully(2, 10, TimeUnit.SECONDS).sync();
        logger.info("{}EventLoopGroup已停止", name);
      }
      catch (InterruptedException e) {
        logger.warn("停止{}EventLoopGroup时被中断", name, e);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * WebSocket通道初始化器
   */
  private final class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
      ChannelPipeline pipeline = ch.pipeline();

      // 空闲检测 - 读空闲超时断开连接
      pipeline.addLast("idle-state",
        new IdleStateHandler(properties.getIdleTimeout(), 0, 0, TimeUnit.SECONDS));

      // HTTP编解码器
      pipeline.addLast("http-codec", new HttpServerCodec());

      // HTTP消息聚合器，将多个HTTP消息聚合成一个FullHttpRequest
      pipeline.addLast("http-aggregator", new HttpObjectAggregator(properties.getMaxContentLength()));

      // WebSocket认证处理器（在WebSocket协议处理器之前）
      pipeline.addLast("websocket-auth", new WebSocketAuthHandler(tokenValidator));

      // ChunkedWrite支持
      pipeline.addLast("http-chunked", new ChunkedWriteHandler());

      // 使用支持多路径的WebSocket协议处理器
      pipeline.addLast("websocket-protocol", new CustomWebSocketServerProtocolHandler(properties));

      // 会话建立处理器
      pipeline.addLast("session-handler", new WebSocketSessionHandler(sessionManager));

      // 自定义消息编码器
      pipeline.addLast("message-encoder", new MessageProtocolEncoder());

      // 自定义消息解码器
      pipeline.addLast("message-decoder", new MessageProtocolDecoder());

      // 直接使用路径路由处理器，处理原始WebSocket帧
      pipeline.addLast("protocol-router", protocolRouterHandler);
    }
  }
}
