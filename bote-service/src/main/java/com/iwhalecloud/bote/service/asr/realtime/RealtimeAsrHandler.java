package com.iwhalecloud.bote.service.asr.realtime;

/**
 * 实时会话服务接口
 *
 * @author qian.sisheng
 * @since 2026-01-14
 */
public interface RealtimeAsrHandler {
  /**
   * 启动会话
   */
  void start();

  /**
   * 发送音频数据
   *
   * @param pcmData PCM 音频数据
   * @param startTs 开始时间戳(毫秒)
   * @param endTs   结束时间戳(毫秒)
   */
  void sendAudio(byte[] pcmData, long startTs, long endTs);

  /**
   * 停止会话
   */
  void stop();

  /**
   * 关闭会话并释放资源
   */
  void close();
}
