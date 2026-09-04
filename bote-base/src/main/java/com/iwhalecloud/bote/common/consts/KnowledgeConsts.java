package com.iwhalecloud.bote.common.consts;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * 知识库相关常量
 *
 * @author bianjp
 * @since 2024-09-27
 */
public final class KnowledgeConsts {
  private KnowledgeConsts() {
  }

  /** 知识库类型: DocChain */
  public static final String KNOWLEDGE_TYPE_DOC_CHAIN = "docChain";
  /** 知识库类型: 知识中台 */
  public static final String KNOWLEDGE_TYPE_PLATFORM = "zhxy";
  /** 知识库类型: 百应 */
  public static final String KNOWLEDGE_TYPE_BEYOND = "beyond";
  /** 知识库类型: weKnora */
  public static final String KNOWLEDGE_TYPE_WEKNORA = "weKnora";
  /** 知识库类型: knowledgeGraph */
  public static final String KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH = "knowledgeGraph";
  /** 第三方知识库类型列表 */
  public static final List<String> OTHER_KNOWLEDGE_TYPES = ImmutableList.of(KNOWLEDGE_TYPE_PLATFORM, KNOWLEDGE_TYPE_BEYOND, KNOWLEDGE_TYPE_WEKNORA,
    KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH);

  /** docchain 主题类型 - Common */
  public static final String TOPIC_TYPE_COMMON = "common";
  /** docchain 主题类型 - ChatExcel */
  public static final String TOPIC_TYPE_CHAT_EXCEL = "neo4j";

  /** 知识库状态: 未发布 */
  public static final String KNOWLEDGE_STATUS_UNPUBLISHED = "10A";
  /** 知识库状态: 发布中 */
  public static final String KNOWLEDGE_STATUS_PUBLISHING = "10B";
  /** 知识库状态: 已修改 */
  public static final String KNOWLEDGE_STATUS_MODIFIED = "10C";
  /** 知识库状态: 已发布 */
  public static final String KNOWLEDGE_STATUS_PUBLISHED = "10D";
  /** 知识库状态: 发布失败 */
  public static final String KNOWLEDGE_STATUS_PUBLISH_FAILED = "10E";

  /** 文档来源: 文档上传 */
  public static final String DOCUMENT_FORM_UPLOAD = "10A";
  /** 文档来源：网页爬取 */
  public static final String DOCUMENT_FROM_CRAWLING = "10B";

  /** 技能或者mcp来源来源: 通用智能体 */
  public static final String AI_SKILL_DATA_FROM_10A = "10A";

  /** 文档处理状态： 10A未处理 */
  public static final String DOCUMENT_STATUS_UNTREATED = "10A";
  /** 文档处理状态： 10B解析中 */
  public static final String DOCUMENT_STATUS_ANALYZING = "10B";
  /** 文档处理状态： 10C要素抽取中 */
  public static final String DOCUMENT_STATUS_FEATURE_EXTRACTION = "10C";
  /** 文档处理状态： 10D构建完成 */
  public static final String DOCUMENT_STATUS_FINISH = "10D";
  /** 文档处理状态： 10E构建失败 */
  public static final String DOCUMENT_STATUS_FAILED = "10E";

  /** 知识库查询来源: 召回测试 */
  public static final String QUERY_SOURCE_RECALL_TEST = "recallTest";
  /** 知识库查询来源: 问答测试 */
  public static final String QUERY_SOURCE_CHAT_TEST = "chatTest";

  /** 引用类型: 文档 */
  public static final String REFERENCE_DOC = "doc";
  /** 引用类型: 图片 */
  public static final String REFERENCE_IMAGE = "image";


  /** 文档导入类型：覆盖 */
  public static final String IMPORT_TYPE_OVERWRITE = "overwrite";
  /** 文档导入类型：增量 */
  public static final String IMPORT_TYPE_INCREMENT = "increment";

  /** 文档导入类型：文件导入 */
  public static final String CORPUS_OPERATE_TYPE_IMPORT = "import";
  /** 文档导入类型：在线配置 */
  public static final String CORPUS_OPERATE_TYPE_CONFIG = "config";

  /** 文档类型：结构化数据 */
  public static final String DOCUMENT_TYPE_STRUCT_DATA = "structData";
  /** 文档类型：非结构化数据 */
  public static final String DOCUMENT_TYPE_UN_STRUCT_DATA = "unStructData";

  /** 阅读格式 - 默认 markdown */
  public static final String DEFAULT_READ_FORMAT_MARKDOWN = "md";
  /** 阅读格式 - html */
  public static final String READ_FORMAT_HTML = "html";

  /** 知识中台资源类型：个人资源 */
  public static final String RESOURCE = "RESOURCE";
  /** 知识中台资源类型：知识库 */
  public static final String KNOW_BASE = "KNOW_BASE";
  /** 知识中台资源类型：知识库资源 */
  public static final String KNOW_BASE_RESOURCE = "KNOW_BASE_RESOURCE";
  /** 知识中台资源类型：文档 */
  public static final String DOCUMENT = "DOCUMENT";
}
