package com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.impl;

import com.iwhalecloud.bote.common.util.ModelClientUtil;
import com.iwhalecloud.bote.common.util.ModelConfigUtil;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.config.LlmProperties;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.client.dto.Tool;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.ILLMProvider;
import com.iwhalecloud.bote.loop.evaluation.domain.component.rpc.convertor.LlmConvertor;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.LLMCallParam;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ModelConfig;
import com.iwhalecloud.bote.loop.evaluation.domain.entity.ReplyItem;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LLMProviderImpl implements ILLMProvider {
  private final LargeModelManageMapper largeModelManageMapper;

  @Override
  public ReplyItem call(LLMCallParam llmCallParam) {
    // 大模型配置
    Long spaceId = llmCallParam.getSpaceId();
    ModelConfig modelConfig = llmCallParam.getModelConfig();
    if (modelConfig == null) {
      throw new BssException("ModelConfig is null");
    }
    Long modelId = modelConfig.getModelId();
    if (modelId == null) {
      throw new BssException("ModelId is null");
    }
    SimpleLargeModelDTO model = largeModelManageMapper.selectLargeModelById(spaceId, modelId);
    if (model == null) {
      throw new BssException("Model not found, modelId=" + modelId);
    }
    LlmConvertor.toModel(modelConfig, model);
    LlmProperties properties = ModelConfigUtil.buildLlmProperties(model);

    // 大模型客户端
    LlmClient llmClient = ModelClientUtil.createLlmClient(model.getProtocolType(), properties);

    // 消息
    List<Message> messages = LlmConvertor.toMessage(llmCallParam.getMessages());

    // 函数
    List<Tool> tools = LlmConvertor.toTool(llmCallParam.getTools());

    // 请求
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .messages(messages)
      .tools(tools)
      .build();

    ChatCompletionResponse response = llmClient.chatCompletion(request);

    return LlmConvertor.toReplyItem(response);
  }

}
