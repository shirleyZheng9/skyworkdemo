package com.iwhalecloud.bote.service.plugin.runner.imageIdentify;

import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.OcrUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.image.ImageIdentifyPluginParams;
import com.iwhalecloud.bote.llm.client.LlmClient;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import com.iwhalecloud.bote.llm.helper.MarkdownHelper;
import com.iwhalecloud.bote.service.plugin.runner.AbstractPlugin;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * 图片识别抽象类
 *
 * @author qian.sisheng
 * @since 2025-11-17
 */
public abstract class AbstractImageIdentifyPlugin extends AbstractPlugin<ImageIdentifyPluginParams> {

  private final Logger logger = LoggerFactory.getLogger(AbstractImageIdentifyPlugin.class);

  public AbstractImageIdentifyPlugin() {
    super(ImageIdentifyPluginParams.class);
  }

  @Override
  public ParameterSpec createRequestParameter() {
    ParameterSpec file = ParameterSpec.newProperty("file", "图片文件", AttrDataType.STRING);
    ParameterSpec type = ParameterSpec.newProperty("type", "图片类型", AttrDataType.STRING);
    return ParameterSpec.newRoot(Arrays.asList(file, type));
  }

  @Override
  public void validateParams(ImageIdentifyPluginParams params) {
    Assert.hasText(params.getFile(), "file不能为空");
    Assert.hasText(params.getType(), "type不能为空");
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(ImageIdentifyPluginParams pluginParams) {
    Object result;
    String text;
    // 图片文件，使用文件ID
    if ("bote".equals(pluginParams.getType())) {
      if (!StringUtils.isNumeric(pluginParams.getFile())) {
        throw new BssException("file参数必须是文件ID且只包含数字");
      }
      result = OcrUtil.doFromImage(Long.valueOf(pluginParams.getFile()), null, null);
    }
    else if ("base64".equals(pluginParams.getType())) {
      String base64String = pluginParams.getFile();
      if (base64String.contains(";base64,")) {
        base64String = base64String.substring(base64String.indexOf(";base64,") + ";base64,".length());
      }
      result = OcrUtil.doFromImage(null, null, base64String);
    }
    else {
      throw new BssException("不支持的type参数: " + pluginParams.getType());
    }
    text = getString(result);
    logger.info("图片识别结果: {}", text);
    LlmClient llmClient = getLlmClient();
    Assert.notNull(llmClient, "llmClient不能为空, 请检查模型配置");
    ChatCompletionRequest request = ChatCompletionRequest.builder()
      .addSystemMessage("你是一位乐于助人的助手，负责根据提供的特定标准提取结构化信息。")
      .addUserMessage(getPrompt(text))
      .build();
    ChatCompletionResponse response = llmClient.chatCompletion(request);
    logger.info("大模型转换结果: {}", response.getMessageContent());
    return MarkdownHelper.parseJson(response.getMessageContent());
  }

  /**
   * 获取文本结果
   */
  private String getString(Object result) {
    if (result instanceof ResultVO<?> resultVO) {
      if (!resultVO.isSuccess()) {
        throw new BssException("图片识别出现异常：" + resultVO.getResultMsg());
      }
    }
    else if (result instanceof String) {
      return (String) result;
    }
    throw new BssException("图片识别结果类型错误：" + result.getClass().getName());
  }

  /**
   * 获取提示词，用于指导大模型将文本信息转换成json数据结构
   */
  protected abstract String getPrompt(String result);
}
