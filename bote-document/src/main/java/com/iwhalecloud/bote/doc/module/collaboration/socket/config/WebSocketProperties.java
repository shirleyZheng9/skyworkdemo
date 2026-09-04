package com.iwhalecloud.bote.doc.module.collaboration.socket.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket配置属性类
 *
 * @author Aiqing
 * @since 2025/08/29
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "bote.dc.websocket")
public class WebSocketProperties {

  /**
   * 是否启用WebSocket服务
   */
  private boolean enable = false;

  /**
   * WebSocket服务端口
   */
  private int port = 9888;

  /**
   * WebSocket路径
   */
  private String path = "/docs";

  /**
   * 空闲超时时间（秒）
   */
  private int idleTimeout = 60;

  /**
   * 是否启用WebSocket压缩
   */
  private boolean enableCompression = true;

  /**
   * 是否启用详细日志
   */
  private boolean enableLogging = false;


  /**
   * 内部socket连接的认证token
   */
  @NotEmpty(message = "内部socket连接的认证token不能为空")
  private String internalAuthKey;

  /**
   * 通用socket链接路径
   */
  @NotEmpty(message = "通用socket链接路径不能为空")
  private String genericSocketPath;
  /**
   * univer socket连接路径
   */
  @NotEmpty(message = "univer socket链接路径不能为空")
  private String univerSocketPath;

  // ========== 内部配置（不对外暴露） ==========

  // HTTP内容最大长度 - 使用64KB作为默认值
  public int getMaxContentLength() {
    return 65536;
  }

  // Boss线程数 - 通常1个就够了
  public int getBossThreads() {
    return 1;
  }

  // Worker线程数 - 使用CPU核心数*2
  public int getWorkerThreads() {
    // 0表示使用Netty默认值
    return 0;
  }

  // 是否启用Epoll - 自动检测
  public boolean isEnableEpoll() {
    // 在服务器中会自动检测是否可用
    return false;
  }

  // TCP连接队列大小
  public int getSoBacklog() {
    return 1024;
  }

  // TCP接收缓冲区大小
  public int getSoRcvBuf() {
    return 32 * 1024;
  }

  // TCP发送缓冲区大小
  public int getSoSndBuf() {
    return 32 * 1024;
  }

  // 写缓冲区低水位线
  public int getWriteBufferLowWaterMark() {
    return 8 * 1024;
  }

  // 写缓冲区高水位线
  public int getWriteBufferHighWaterMark() {
    return 32 * 1024;
  }

  // TCP_NODELAY - 禁用Nagle算法，提高实时性
  public boolean isTcpNodelay() {
    return true;
  }

  // SO_KEEPALIVE - 启用TCP心跳
  public boolean isSoKeepalive() {
    return true;
  }

  // SO_REUSEADDR - 允许地址重用
  public boolean isSoReuseaddr() {
    return true;
  }

  // 握手超时时间
  public long getHandshakeTimeoutMillis() {
    return 10000L;
  }

  // 掩码匹配 - 严格按照WebSocket协议
  public boolean isAllowMaskMismatch() {
    return false;
  }

  // 路径前缀检查 - 启用路径验证
  public boolean isCheckStartsWith() {
    return true;
  }
}
