package com.iwhalecloud.bote.config.properties;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

/**
 * DocChain 配置项
 *
 * @author bianjp
 * @since 2024-10-09
 */
@Component
@ConfigurationProperties("knowledge.doc-chain")
@Validated
@Getter(AccessLevel.NONE)
@Setter
@ToString
public class DocChainProperties {
  /** 接口地址 */
  @URL
  private String apiUrl;
  /** 用户名 */
  private String username;
  /** 密码 */
  private String password;
  /** API Key */
  private String apiKey;
  /** 是否启用 */
  private Boolean enabled;

  /** 会话接口地址 */
  private HttpUrl chatApiUrl;
  /** 知识召回接口地址 */
  private String recallApiUrl;
  /** chatExcel类知识召回接口地址 */
  private String recallChatExcelApiUrl;
  /** 保存主题接口地址 */
  private String updateTopicApiUrl;
  /** 查询主题列表接口地址 */
  private String queryTopicsApiUrl;
  /** 重新构建主题下的所有文档 */
  private String redoFilesApiUrl;
  /** 登录接口地址 */
  private String loginApiUrl;
  /** OCR 接口地址 */
  private String ocrApiUrl;
  /** 新用户注册接口地址 */
  private String registerApiUrl;
  /** 给指定用户生成 API Key */
  private String generateApiKeyApiUrl;
  /** 查询用户的 API Key 列表 */
  private String queryApiKeysApiUrl;
  /** 批量上传文档接口地址 */
  private String uploadDocumentApiUrl;
  /** 批量上传文档接口地址（对话） */
  private String uploadChatDocumentApiUrl;
  /** 保存文档接口地址 */
  private String updateDocumentApiUrl;
  /** 重新构建文档接口地址 */
  private String redoDocumentApiUrl;
  /** 生成摘要接口地址 */
  private String summaryDocumentApiUrl;
  /** 查询文档列表接口地址(分页) */
  private String queryDocumentApiUrl;
  /** 查询文档列表接口地址 */
  private String queryDocumentListApiUrl;
  /** 查询文档信息接口地址 */
  private String readDocumentApiUrl;
  /** 查询文档详情接口地址 */
  private String documentDetailApiUrl;
  /** 下载源文件接口地址 */
  private String downloadSourFileApiUlr;
  /** 查询会话日志接口地址 */
  private String queryChatLogApiUrl;

  /** 阅读模式基础地址 */
  private String readerViewBaseUrl;
  /** 阅读模式目录接口地址 */
  private String readerViewDirApiUrl;

  /** 大模型列表接口地址 */
  private String modelListApiUrl;

  /** 同步拆分接口地址 */
  private String syncSplitApiUrl;
  /** 异拆分接口地址 */
  private String asyncSplitApiUrl;
  /** 切分参数模板信息接口地址 */
  private String splitParamsApiUrl;
  /** 删除文档接口地址 */
  private String deleteDocumentApiUrl;

  /** 文档中的chunk查询 */
  private String docChunkListApiUrl;
  /** 文档中的chunk创建接口地址 */
  private String docChunkCreateApiUrl;
  /** 文档中的chunk删除接口地址 */
  private String deleteDocChunkApiUrl;
  /** 文档中的chunk更新接口地址 */
  private String updateDocChunkApiUrl;
  /** 文档中的关联chunk查询接口地址 */
  private String docChunkRelGetApiUrl;
  /** 文档中的chunk查询 */
  private String docGetChunkApiUrl;
  /** 重排接口 */
  private String rerankApiUrl;
  /** 会话上传接口地址 */
  private String chatUploadApiUrl;
  /** 图片搜索接口地址 */
  private String imgSearchApiUrl;

  /** 租户配置，key 为租户 ID。可选，用于给个别租户配置不同的 DocChain 接口地址 */
  @Valid
  private Map<Long, DocChainProperties> tenant = new HashMap<>();

  /**
   * 获取租户的 DocChain 配置
   */
  private DocChainProperties getProperties(@Nullable Long tenantId) {
    if (tenantId != null && tenant.containsKey(tenantId)) {
      DocChainProperties properties = tenant.get(tenantId);
      Assert.hasLength(properties.apiUrl, () -> "未配置 DocChain 接口地址: tenantId=" + tenantId);
      return properties;
    }
    Assert.hasLength(apiUrl, "未配置 DocChain 接口地址");
    return this;
  }

  public String getApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).apiUrl;
  }

  public String getUsername(@Nullable Long tenantId) {
    return getProperties(tenantId).username;
  }

  public String getPassword(@Nullable Long tenantId) {
    return getProperties(tenantId).password;
  }

  public String getApiKey(@Nullable Long tenantId) {
    return getProperties(tenantId).apiKey;
  }

  public String getDownloadSourFileApiUlr(@Nullable Long tenantId) {
    return getProperties(tenantId).downloadSourFileApiUlr;
  }

  public String getQueryChatLogApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).queryChatLogApiUrl;
  }

  public String getReadDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).readDocumentApiUrl;
  }

  public String getDocumentDetailApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).documentDetailApiUrl;
  }

  public String getQueryDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).queryDocumentApiUrl;
  }

  public String getQueryDocumentListApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).queryDocumentListApiUrl;
  }

  public String getSummaryDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).summaryDocumentApiUrl;
  }

  public String getRedoDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).redoDocumentApiUrl;
  }

  public String getUpdateDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).updateDocumentApiUrl;
  }

  public String getUploadDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).uploadDocumentApiUrl;
  }

  public String getUploadChatDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).uploadChatDocumentApiUrl;
  }

  public String getRegisterApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).registerApiUrl;
  }

  public String getGenerateApiKeyApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).generateApiKeyApiUrl;
  }

  public String getQueryApiKeysApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).queryApiKeysApiUrl;
  }

  public String getOcrApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).ocrApiUrl;
  }

  public String getLoginApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).loginApiUrl;
  }

  public String getQueryTopicsApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).queryTopicsApiUrl;
  }

  public String getUpdateTopicApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).updateTopicApiUrl;
  }

  public String getRedoFilesApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).redoFilesApiUrl;
  }

  public String getRecallApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).recallApiUrl;
  }

  public String getRecallChatExcelApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).recallChatExcelApiUrl;
  }
  public HttpUrl getChatApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).chatApiUrl;
  }

  public String getReaderViewBaseUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).readerViewBaseUrl;
  }

  public String getReaderViewDirApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).readerViewDirApiUrl;
  }

  public String getModelListApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).modelListApiUrl;
  }

  public String getSyncSplitApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).syncSplitApiUrl;
  }

  public String getAsyncSplitApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).asyncSplitApiUrl;
  }

  public String getSplitParamsApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).splitParamsApiUrl;
  }

  public String getDeleteDocumentApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).deleteDocumentApiUrl;
  }

  public String getDocChunkListApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).docChunkListApiUrl;
  }

  public String getDocChunkCreateApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).docChunkCreateApiUrl;
  }

  public String getDeleteDocChunkApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).deleteDocChunkApiUrl;
  }

  public String getUpdateDocChunkApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).updateDocChunkApiUrl;
  }

  public String getDocChunkRelGetApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).docChunkRelGetApiUrl;
  }

  public String getDocGetChunkApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).docGetChunkApiUrl;
  }

  public String getRerankApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).rerankApiUrl;
  }

  public String getChatUploadApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).chatUploadApiUrl;
  }

  public String getImgSearchApiUrl(@Nullable Long tenantId) {
    return getProperties(tenantId).imgSearchApiUrl;
  }

  /**
   * 规范接口地址
   */
  @PostConstruct
  public void normalizeUrl() {
    if (!tenant.isEmpty()) {
      for (DocChainProperties properties : tenant.values()) {
        properties.normalizeUrl();
      }
    }

    apiUrl = StringUtils.stripEnd(apiUrl, "/");
    if (StringUtils.isEmpty(apiUrl)) {
      return;
    }

    chatApiUrl = HttpUrl.parse(apiUrl + "/openai/v1/chat/completions");
    recallApiUrl = apiUrl + "/v1/search";
    recallChatExcelApiUrl = apiUrl + "/v1/chatexcel/search";
    updateTopicApiUrl = apiUrl + "/v1/topic/update";
    queryTopicsApiUrl = apiUrl + "/v1/topic/auth/list";
    redoFilesApiUrl = apiUrl + "/v1/topic/redo_files";
    loginApiUrl = apiUrl + "/v1/auth/login";
    ocrApiUrl = apiUrl + "/v1/image/ocr";
    registerApiUrl = apiUrl + "/v1/auth/register";
    generateApiKeyApiUrl = apiUrl + "/v1/generate/common/user/api_key";
    queryApiKeysApiUrl = apiUrl + "/v1/get/user/api_keys";
    uploadDocumentApiUrl = apiUrl + "/v1/doc/batchUpload";
    uploadChatDocumentApiUrl = apiUrl + "/v1/chat_doc/upload";
    updateDocumentApiUrl = apiUrl + "/v1/doc/update";
    redoDocumentApiUrl = apiUrl + "/v1/doc/redo";
    queryDocumentApiUrl = apiUrl + "/v1/doc/list/pagination";
    queryDocumentListApiUrl = apiUrl + "/v1/doc/list";
    readDocumentApiUrl = apiUrl + "/v1/doc/read";
    documentDetailApiUrl = apiUrl + "/v1/doc/detail";
    summaryDocumentApiUrl = apiUrl + "/v1/summary";
    downloadSourFileApiUlr = apiUrl + "/v1/doc/read";
    queryChatLogApiUrl = apiUrl + "/v1/chat_log/detail";
    modelListApiUrl = apiUrl + "/v1/get_model_list";
    syncSplitApiUrl = apiUrl + "/v1/doc/split";
    asyncSplitApiUrl = apiUrl + "/v1/doc/split/async";
    splitParamsApiUrl = apiUrl + "/v1/split/params";
    deleteDocumentApiUrl = apiUrl + "/v1/doc/delete";
    docChunkListApiUrl = apiUrl + "/v1/doc/chunks/list";
    docChunkCreateApiUrl = apiUrl + "/v1/doc/chunks/create";
    deleteDocChunkApiUrl = apiUrl + "/v1/doc/chunks/delete";
    updateDocChunkApiUrl = apiUrl + "/v1/doc/chunks/update";
    docGetChunkApiUrl = apiUrl + "/v1/doc/chunks/get";
    rerankApiUrl = apiUrl + "/v1/rerank";
    chatUploadApiUrl = apiUrl + "/v1/chat/upload";
    imgSearchApiUrl = apiUrl + "/v1/img/search";
    docChunkRelGetApiUrl = apiUrl + "/v1/doc/chunk/query";
  }
}
