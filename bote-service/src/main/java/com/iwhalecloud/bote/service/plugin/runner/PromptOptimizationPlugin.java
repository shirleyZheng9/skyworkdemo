package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.cache.ModelClientCache;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.PromptOptimizationPluginParams;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 文生图提示词优化插件
 *
 * @author lizuyin
 * @since 2025-11-19
 */
@Component
public class PromptOptimizationPlugin extends AbstractPlugin<PromptOptimizationPluginParams> {

  private static final Long DEFAULT_TENANT = 2L;
  private static final Long DEFAULT_MODEL = 1180740625294376960L;
  private static final String SYSTEM_MESSAGE = "你是一名专业的Stable Diffusion提示词撰写专家。请根据用户输入的提示词优化提示词并输出。\n\n以下是一个优秀的Stable Diffusion提示词撰写结构：\n\n1. 图片质量：最佳质量，超详细，杰作，4K，超细致...\n\n2. 风格：写实照片，油画，日本动画，老式漫画，皮克斯，半厚画风，ArtStation流行风格...\n\n3. 主体：主体细节、动作、情感\n\n4. 环境：场景、视角、光线、构图、色彩\n\n\n\n以下是撰写提示词的规则：\n\n1. 提示主体的重要词语需要放在前面。\n\n2. 使用英文逗号连接，不使用句号和引号。\n\n3. 尽可能使用单词而非短句。\n\n4. 当数量相关时使用数字而非量词，例如\"1个女孩\"。\n\n\n\n注意提示词输出为中文，不允许出现无关内容。";

  /**
   * 模型客户端缓存
   */
  private static final ModelClientCache modelClientCache = SpringUtil.getBean(ModelClientCache.class);

  public PromptOptimizationPlugin() {
    super(PromptOptimizationPluginParams.class);
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_PROMPT_OPTIMIZATION;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(
      ParameterSpec.newProperty("prompt", "提示词内容（必填，字符串类型），用户输入的提示词", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(
      Collections.singletonList(ParameterSpec.newProperty("optimizedPrompt", "优化后的提示词", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(PromptOptimizationPluginParams params) {
    Assert.notNull(params.getPrompt(), "prompt不能为空");
    String promptStr = params.getPrompt();
    if (StringUtils.isBlank(promptStr)) {
      throw new IllegalArgumentException("prompt不能为空");
    }
  }

  @Override
  public Object doRun(PromptOptimizationPluginParams pluginParams) {
    try {
      // 获取LLM客户端
      LlmClient llmClient = modelClientCache.getLlmClient(DEFAULT_TENANT, DEFAULT_MODEL);

      // 构建请求
      ChatCompletionRequest request = ChatCompletionRequest.builder().model(llmClient.defaultModel()).stream(false)
        .addSystemMessage(SYSTEM_MESSAGE).addUserMessage(pluginParams.getPrompt().trim()).build();

      // 调用大模型
      ChatCompletionResponse response = llmClient.chatCompletion(request);

      // 检查响应是否为null
      if (response == null) {
        throw new BssException("大模型响应内容为空");
      }

      // 获取优化后的提示词
      String optimizedPrompt = response.getMessageContent();
      if (StringUtils.isBlank(optimizedPrompt)) {
        throw new BssException("文生图提示词优化服务返回内容为空");
      }

      // 返回结果
      Map<String, Object> result = new HashMap<>();
      result.put("optimizedPrompt", optimizedPrompt.trim());
      return result;
    }
    catch (BssException e) {
      throw e;
    }
    catch (Exception e) {
      throw new BssException("文生图提示词优化插件执行异常: " + e.getMessage(), e);
    }
  }

}

