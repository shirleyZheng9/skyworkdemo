package com.iwhalecloud.bote.doc.module.collaboration.socket;

import com.iwhalecloud.bote.doc.module.collaboration.socket.message.MessageProtocol;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 注册不同的消息协议处理实现
 *
 * @author Aiqing
 * @since 2025/9/3
 */
@Component
public class ProtocolRegistry implements InitializingBean {

  private final Map<SocketProtocolEnum, MessageProtocol> protocolMap = new ConcurrentHashMap<>();
  private final Map<String, MessageProtocol> pathMap = new ConcurrentHashMap<>();

  private final List<MessageProtocol> protocolList;

  public ProtocolRegistry(List<MessageProtocol> protocolList) {
    this.protocolList = protocolList;
  }

  /**
   * 获取对应的消息协议实现
   *
   * @param protocol 协议枚举
   * @return 协议实现
   */
  public MessageProtocol getProtocol(SocketProtocolEnum protocol) {
    return protocolMap.get(protocol);
  }

  /**
   * 根据路径获取对应的消息协议实现
   *
   * @param path 路径
   * @return 协议实现
   */
  public MessageProtocol getProtocolByPath(String path) {
    return pathMap.get(path);
  }

  @Override
  public void afterPropertiesSet() {
    if (CollectionUtils.isEmpty(protocolList)) {
      return;
    }
    protocolList.forEach(item -> {
      protocolMap.put(item.getProtocol(), item);
      pathMap.put(item.getSocketPath(), item);
    });
  }
}
