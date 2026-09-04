package com.iwhalecloud.bote.doc.module.collaboration.socket;

import com.iwhalecloud.bote.doc.module.collaboration.cache.SocketServerCache;
import jakarta.annotation.PreDestroy;
import java.util.List;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SocketServerGroup implements CommandLineRunner {

  @Getter
  private static volatile Long serverId = 0L;


  private final List<SocketServer> socketServers;
  private final SocketServerCache socketServerCache;

  public SocketServerGroup(List<SocketServer> socketServers, SocketServerCache socketServerCache) {
    this.socketServers = socketServers;
    this.socketServerCache = socketServerCache;
  }

  /***
   * 判断服务器是否就绪
   *
   **/
  public boolean isReady() {
    for (SocketServer socketServer : socketServers) {
      if (!socketServer.isReady()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void run(String... args) {
    // 初始化SERVER_ID
    synchronized (SocketServerGroup.class) {
      serverId = socketServerCache.incrementServerId();
    }
    // 启动服务
    for (SocketServer socketServer : socketServers) {
      socketServer.start();
    }
  }

  @PreDestroy
  public void destroy() {
    // 停止服务
    for (SocketServer socketServer : socketServers) {
      socketServer.stop();
    }
  }
}
