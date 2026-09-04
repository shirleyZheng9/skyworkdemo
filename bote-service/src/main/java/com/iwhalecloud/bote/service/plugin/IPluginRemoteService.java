package com.iwhalecloud.bote.service.plugin;

import com.iwhalecloud.bss.litchi.base.vo.ResultVO;

public interface IPluginRemoteService {


  /**
   * 构建数据源向量到插件
   */
  ResultVO<String> buildSchemaVector(Long tenantId, Long dataSourceInstId);


  /**
   * 生成sql
   */
  ResultVO<String> createSqlByQuestion(Long tenantId, Long dataSourceId, String question, String evnCode);

  /**
   * 解析视频
   */
  ResultVO<Object> videoAnalysis(Long fileId, String analysisMode);
  /**
   * 解析音频
   */
  ResultVO<Object> audioAnalysis(Long fileId);

  /**
   * 视频转音频
   *
   * @param fileId      视频文件ID
   * @param audioFormat 音频格式
   * @return 视频文件ID
   */
  ResultVO<Object> videoToAudio(Long fileId, String audioFormat);

  /**
   * 解析抖音视频
   *
   * @param url 视频地址
   * @param analysisMode 分析模式
   * @return 解析结果
   */
  ResultVO<Object> douYinVideoAnalysis(String url, String analysisMode);

  /**
   * 多平台视频下载（支持抖音、小红书、Bilibili、快手、西瓜视频等）
   *
   * @param url 视频地址
   * @return 下载结果
   */
  ResultVO<Object> platformVideoDownload(String url);
}
