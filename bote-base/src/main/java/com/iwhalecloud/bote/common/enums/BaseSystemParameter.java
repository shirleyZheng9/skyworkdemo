package com.iwhalecloud.bote.common.enums;

import com.iwhalecloud.bote.cache.DcParamCache;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/**
 * 系统参数相关常量
 * <p>1. 大部分变量，维护在数据库 dc_param 表，通过 DcParamCache 读取 </p>
 * <p>2. 部分变量，维护在配置文件，通过 Environment.class 读取 </p>
 *
 * @author chen.linfa
 * @since 2025-11-06
 */
@RequiredArgsConstructor
@Getter
public enum BaseSystemParameter {

  /** DB：超级管理员账号信息 */
  SUPER_ADMIN("SUPER_ADMIN", "1|admin"),

  /** DB：前后端加解密 AES */
  ENCRYPTION_AES("ENCRYPTION_AES", ""),

  /** 工作流执行时间限制(秒), 0 表示不限制 */
  FLOW_EXECUTION_TIME_LIMIT("FLOW_EXECUTION_TIME_LIMIT", "300"),

  /** 允许上传的文件类型 */
  ALLOW_UPLOAD_FILE_TYPE("ALLOW_UPLOAD_FILE", "doc,docx,pdf,ppt,pptx,csv,md,zip"),
  /** 允许 DocChain 上传的文件类型 */
  ALLOW_DOCCHAIN_UPLOAD_FILE_TYPE("ALLOW_DOCCHAIN_UPLOAD_FILE", "doc,docx,html,jpeg,jpg,md,pdf,png,ppt,pptx,txt,wps,xls,xlsx"),
  /** 平台允许上传的所有文件类型 */
  PLATFORM_ALLOW_UPLOAD_FILE_TYPE("PLATFORM_ALLOW_UPLOAD_FILE_TYPE", "png,jpg,jpeg,gif,svg,bmp,fiff,pdf,txt,md,docx,doc,xls,xlsx,ppt,pptx,html,htm,wav,mp3,acc,mp4,avi,mov,m4a,js,ts,py,java,zip,csv,wps"),

  /** DocChain 账号默认密码 */
  DOC_CHAIN_USER_PASSWORD("DOC_CHAIN_USER_PASSWORD", "MTIzNDU2YUEh"),

  /** docchain 问答回调博特的密钥 */
  DOCCHAIN_LLM_API_KEY("DOCCHAIN_LLM_API_KEY", ""),

  /** docchain 主题保存协议   */
  DOCCHAIN_TOPIC_API_REQUEST("DOCCHAIN_TOPIC_API_REQUEST", ""),
  /** 知识库主题类型为：chatExcel的策略   */
  DOCCHAIN_CHATEXCEL_STRATEGY("DOCCHAIN_CHATEXCEL_STRATEGY", ""),
  /** docchain使用博特平台LLM开关 */
  DOCCHAIN_USE_BOTE_LLM_ENABLED("DOCCHAIN_USE_BOTE_LLM_ENABLED", "T"),

  /** ENV：博特平台接口地址（以 "/" 结尾） */
  BOTE_API_URL("bote.apiUrl", ""),

  /** ENV：是否启用docChain */
  ENABLE_DOCCHAIN("knowledge.doc-chain.enabled", "true"),
  /** ENV：是否启用weknora */
  ENABLE_WEKNORA("knowledge.weknora.enabled", "false"),
  /** ENV：是否启用 knowledgeGraph */
  ENABLE_KNOWLEDGE_GRAPH("knowledge.knowledgeGraph.enabled", "false"),

  /** 允许上传转在线的文件类型 */
  ALLOW_UPLOAD_FILE_CONVERT_TO_ONLINE_TYPE("ALLOW_UPLOAD_FILE_CONVERT_TO_ONLINE", "doc,docx,xls,xlsx"),
  /** 允许上传转在线WORD的文件类型 */
  CONVERT_TO_ONLINE_WORD_TYPE("CONVERT_TO_ONLINE_WORD_TYPE", "doc,docx"),
  /** 允许上传转在线excel的文件类型 */
  CONVERT_TO_ONLINE_EXCEL_TYPE("CONVERT_TO_ONLINE_EXCEL_TYPE", "xls,xlsx"),
  /** DocChain 文档重建队列每次处理条数 */
  DOCCHAIN_REBUILD_QUEUE_BATCH_SIZE("DOCCHAIN_REBUILD_QUEUE_BATCH_SIZE", "1000"),
  /** DocChain 文档重建队列并行处理时每个线程处理的条数 */
  DOCCHAIN_REBUILD_QUEUE_CHUNK_SIZE("DOCCHAIN_REBUILD_QUEUE_CHUNK_SIZE", "50"),

  /** 提示词 - 知识问答场景的默认提示词 */
  KNOWLEDGE_SCENE_DEFAULT_PROMPT("KNOWLEDGE_SCENE_DEFAULT_PROMPT", ""),
  /** 知识库提问embedding模型id */
  KNOWLEDGE_QUESTION_EMBEDDING_MODEL_ID("KNOWLEDGE_QUESTION_EMBEDDING_MODEL_ID", ""),
  /** 知识库提问embedding分数阈值 */
  KNOWLEDGE_DOCUMENT_EMBEDDING_SCORE("KNOWLEDGE_DOCUMENT_EMBEDDING_SCORE", ""),

  /**
   * 文档中心上传类文档是否写入文件版本日志（{@code bt_dc_document_file_log}）。数据库值为 T/Y/TRUE/1 时记录，默认 F 不记录。
   */
  DOCUMENT_HIS_LOG_ENABLED("DOCUMENT_HIS_LOG_ENABLED", "F"),

  DO_NOT_USE("", "");

  /** 系统参数编码 */
  private final String code;
  /** 系统参数默认值 */
  private final String defaultValue;

  /**
   * 获取系统参数值，来源于环境
   *
   * @return 字符串类型值
   */
  public String getValueFromEnv() {
    return SpringUtil.getProperty(this.code, this.defaultValue);
  }

  /**
   * 获取系统参数值，来源于数据库
   *
   * @return 字符串类型值
   */
  public String getValueFromDb() {
    return SpringUtil.getBean(DcParamCache.class).getDcParamValByCode(this.code, this.defaultValue);
  }


  /**
   * 获取系统参数值，来源于数据库
   *
   * @return 整数类型值
   */
  public Integer getIntegerValueFromDb() {
    String value = getValueFromDb();
    return StringUtils.isEmpty(value) ? null : Integer.valueOf(value);
  }

  /**
   * 获取系统参数值，来源于数据库（不为空）
   *
   * @return 整数类型值
   */
  @NonNull
  public Integer getRequiredIntegerValueFromDb() {
    String value = getValueFromDb();
    return StringUtils.isEmpty(value) ? 0 : Integer.parseInt(value);
  }

  /**
   * 获取系统参数值，来源于数据库
   *
   * @return 布尔类型值
   */
  @NonNull
  public Boolean getBooleanValueFromDb() {
    String value = getValueFromDb();
    List<String> flag = Arrays.asList("T", "Y", "TRUE", "1");
    return StringUtils.isEmpty(value) || !flag.contains(value.toUpperCase()) ? Boolean.FALSE : Boolean.TRUE;
  }
}

