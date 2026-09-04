package com.iwhalecloud.bote.service.publish;

import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.service.publish.platform.PlatformAdapter;
import java.util.Map;

/**
 * SDK连接管理器接口
 *
 * @author system
 * @since 2025-01-09
 */
public interface SdkConnectionManager {

  /**
   * 启动SDK连接
   *
   * @param record 发布记录
   */
  void startConnection(ResourcePublishRecordDTO record);

  /**
   * 停止SDK连接
   *
   * @param callbackCode 回调编码
   */
  void stopConnection(String callbackCode);

  /**
   * 广播停止连接
   *
   * @param callbackCode 回调编码
   */
  void broadcastStopConnection(String callbackCode);

  /**
   * 获取连接状态
   *
   * @param callbackCode 回调编码
   * @return 连接状态
   */
  String getConnectionStatus(String callbackCode);

  /**
   * 重启连接
   *
   * @param callbackCode 回调编码
   */
  void restartConnection(String callbackCode);

  /**
   * 获取所有连接状态
   *
   * @return 连接状态映射
   */
  Map<String, String> getAllConnectionStatus();

  /**
   * 根据回调代码获取平台适配器
   *
   * @param callbackCode 回调代码
   * @return 平台适配器
   */
  PlatformAdapter getAdapter(String callbackCode);

}
