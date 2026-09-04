package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.WordToAudioPluginParams;
import com.iwhalecloud.bote.dto.reply.TtsRequest;
import com.iwhalecloud.bote.service.reply.IWordToAudioService;
import java.util.Arrays;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 文字转语音插件
 *
 * @author qian.sisheng
 * @since 2025-11-21
 */
@Component
public class WordToAudioPlugin extends AbstractPlugin<WordToAudioPluginParams> {

  private final IWordToAudioService wordToAudioService;

  public WordToAudioPlugin(IWordToAudioService wordToAudioService) {
    super(WordToAudioPluginParams.class);
    this.wordToAudioService = wordToAudioService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_WORD_TO_AUDIO;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("text", "文本", AttrDataType.STRING),
      ParameterSpec.newProperty("audioName", "音色", AttrDataType.STRING)
    ));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("url", "音频文件地址", AttrDataType.STRING),
      ParameterSpec.newProperty("fileId", "音频文件ID", AttrDataType.INTEGER)
    ));
  }

  @Override
  public void validateParams(WordToAudioPluginParams params) {
    Assert.hasText(params.getText(), "text不能为空");
  }

  @Override
  public Object doRun(WordToAudioPluginParams pluginParams) {
    TtsRequest request = new TtsRequest(pluginParams.getText(), pluginParams.getAudioName(), pluginParams.getSpeedFactor());
    Long fileId = wordToAudioService.ttsAndUpload(request);
    String fileUrl = getFileUrl(fileId);
    return Map.of("fileId", fileId, "url", fileUrl);
  }
}
