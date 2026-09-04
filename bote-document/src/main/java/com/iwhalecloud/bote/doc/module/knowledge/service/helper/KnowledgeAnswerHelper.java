package com.iwhalecloud.bote.doc.module.knowledge.service.helper;

import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.common.consts.ModelConsts;
import com.iwhalecloud.bote.common.enums.BaseSystemParameter;
import com.iwhalecloud.bote.common.sse.event.SseEvent;
import com.iwhalecloud.bote.doc.common.utils.KnowledgeClientUtil;
import com.iwhalecloud.bote.doc.module.knowledge.adapter.client.KnowledgeClient;
import com.iwhalecloud.bote.doc.module.knowledge.cache.KnowledgeCache;
import com.iwhalecloud.bote.dto.chat.KnowledgeChatParamsDTO;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeChatResponse;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.knowledge.SimpleKnowledgeDTO;
import com.iwhalecloud.bote.llm.client.dto.CustomModelConfig;
import com.iwhalecloud.bote.llm.client.util.LlmTraceUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import okhttp3.sse.EventSource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 知识库问答辅助类
 *
 * @author bianjp
 * @since 2024-10-08
 */
@Component
@RequiredArgsConstructor
public class KnowledgeAnswerHelper {
  /** 博特平台的大模型会话补全接口地址 */
  private static final String LLM_API_URL = StringUtils.stripEnd(BaseSystemParameter.BOTE_API_URL.getValueFromEnv(), "/") + "/bote/modelProxy/v1/chat/completions";

  private final KnowledgeCache knowledgeCache;
  private final TenantSettingInfoCache tenantSettingInfoCache;
  private final BtDcKbPermissionHelper btDcKbPermissionHelper;
  /**
   * 知识问答
   *
   * @param params 问答参数
   * @return 回复内容
   */
  public KnowledgeChatResponse chatByKnowledge(KnowledgeChatParamsDTO params) {
    KnowledgeClient knowledgeClient = prepareKnowledgeClient(params);
    return knowledgeClient.chat(params);
  }

  /**
   * 知识问答，流式响应
   *
   * @param params 问答参数
   * @param partialHandler 片段处理器
   * @param completionHandler 完成回调，参数为异常对象（成功时为 null）
   */
  public EventSource chatStreamByKnowledge(KnowledgeChatParamsDTO params, Consumer<SseEvent> partialHandler, Consumer<BssException> completionHandler) {
    KnowledgeClient knowledgeClient = prepareKnowledgeClient(params);
    return knowledgeClient.chatStream(params, partialHandler, completionHandler);
  }

  /**
   * 知识问答，流式响应 - 同步阻塞模式
   *
   * @param params 问答参数
   * @param eventHandler 片段处理器
   * @param requestListener 请求监听器，用于调用方存储 Call 对象以实现中断请求
   */
  public KnowledgeChatResponse chatStreamByKnowledgeBlocking(KnowledgeChatParamsDTO params,
                                                             @Nullable Consumer<SseEvent> eventHandler,
                                                             @Nullable Consumer<Object> requestListener) {
    KnowledgeClient knowledgeClient = prepareKnowledgeClient(params);
    return knowledgeClient.chatStreamBlocking(params, eventHandler, requestListener);
  }

  /**
   * 查询知识库列表
   */
  private List<SimpleKnowledgeDTO> getKnowledgeList(Long tenantId, List<Long> knowledgeIds) {
    List<SimpleKnowledgeDTO> knowledgeList = knowledgeCache.batchGet(tenantId, knowledgeIds);
    // 有多个知识库时，类型必须相同
    if (knowledgeList.size() > 1) {
      String knowledgeType = knowledgeList.getFirst().getKnowledgeType();
      Assert.isTrue(knowledgeList.stream().allMatch(k -> knowledgeType.equals(k.getKnowledgeType())), "知识问答不支持混合不同类型的知识库");
    }
    return knowledgeList;
  }

  private void setModelInfo(KnowledgeChatParamsDTO params) {
    if (BooleanUtils.isNotTrue(BaseSystemParameter.DOCCHAIN_USE_BOTE_LLM_ENABLED.getBooleanValueFromDb())) {
      return;
    }
    Long modelId = params.getModelId();
    if (modelId == null || ModelConsts.DEFAULT_MODEL.equals(modelId)) {
      modelId = tenantSettingInfoCache.getModelId(params.getTenantId());
    }
    Map<String, Object> modelInfo = new HashMap<>();
    // 使用博特开放的模型对话 API
    List<String> paramList = getModelUrlParamList(params);
    modelInfo.put("url", paramList.isEmpty() ? LLM_API_URL : LLM_API_URL + "?" + StringUtils.join(paramList, "&"));
    // 按照约定的格式构造模型名称：tenantId/modelId
    modelInfo.put("model", params.getTenantId() + "/" + modelId);
    // 采用平台预置的 API 密钥，方便 docchain 调用博特 API 时进行鉴权
    modelInfo.put("api_key", BaseSystemParameter.DOCCHAIN_LLM_API_KEY.getValueFromDb());
    params.setModelInfo(modelInfo);
  }

  /**
   * 获取大模型会话补全接口地址数列表
   */
  private static List<String> getModelUrlParamList(KnowledgeChatParamsDTO params) {
    List<String> paramList = new ArrayList<>();
    String traceId = LlmTraceUtil.getTraceId();
    if (StringUtils.isNotEmpty(traceId)) {
      paramList.add("traceId=" + traceId);
    }
    CustomModelConfig customModelConfig = params.getCustomModelConfig();
    if (customModelConfig != null) {
      if (Boolean.TRUE.equals(customModelConfig.getMaxTokensEnabled()) && customModelConfig.getMaxTokens() != null) {
        paramList.add("maxTokens=" + customModelConfig.getMaxTokens());
      }
      if (Boolean.TRUE.equals(customModelConfig.getTemperatureEnabled()) && customModelConfig.getTemperature() != null) {
        paramList.add("temperature=" + customModelConfig.getTemperature());
      }
    }
    return paramList;
  }

  private KnowledgeClient prepareKnowledgeClient(KnowledgeChatParamsDTO params) {
    KnowledgeInfoDTO knowledgeInfo = tenantSettingInfoCache.getKnowledgeInfo(params.getTenantId());
    if (StringUtils.isNotEmpty(knowledgeInfo.getKnowledgeType())) {
      return KnowledgeClientUtil.getClient(knowledgeInfo.getKnowledgeType());
    }
    if (KnowledgeConsts.KNOWLEDGE_TYPE_WEKNORA.equals(params.getKnowledgeType())) {
      return KnowledgeClientUtil.getClient("weKnora");
    }
    if (KnowledgeConsts.KNOWLEDGE_TYPE_KNOWLEDGE_GRAPH.equals(params.getKnowledgeType())) {
      return KnowledgeClientUtil.getClient("knowledgeGraph");
    }
    List<SimpleKnowledgeDTO> knowledgeList = getKnowledgeList(params.getTenantId(), params.getKnowledgeIds());
    Pair<List<String>, List<String>> topicAndDocIds = btDcKbPermissionHelper.getTopicAndDocIds(knowledgeList, params.getDocumentIds(), params.getTenantId());
    params.setTopicIds(topicAndDocIds.getLeft());
    params.setDocIds(topicAndDocIds.getRight());
    params.setKnowledgeList(knowledgeList);
    setModelInfo(params);
    return KnowledgeClientUtil.getClient(knowledgeList.getFirst().getKnowledgeType());
  }
}
