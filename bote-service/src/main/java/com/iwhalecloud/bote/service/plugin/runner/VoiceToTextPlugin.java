package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.FileTypeUtil;
import com.iwhalecloud.bote.common.util.OcrUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.VoiceToTextPluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 语音转文字插件
 *
 * @author qian.sisheng
 * @since 2025-06-11
 */
@Component
public class VoiceToTextPlugin extends AbstractPlugin<VoiceToTextPluginParams> {
  private final IFileStoreService service;

  public VoiceToTextPlugin(IFileStoreService service) {
    super(VoiceToTextPluginParams.class);
    this.service = service;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_VOICE_TO_TEXT;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.INTEGER)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(ParameterSpec.newProperty("voiceContent", "语音内容", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(VoiceToTextPluginParams params) {
    Assert.notNull(params.getFileId(), "fileId不能为空");
  }

  @Override
  public Object doRun(VoiceToTextPluginParams pluginParams) {
    FileInfoVO fileInfo = service.getFileInfoById(pluginParams.getFileId());
    Assert.notNull(fileInfo, "查询不到有效的文件信息");
    if (!FileTypeUtil.isVideo(fileInfo.getFileType())) {
      throw new BssException("文件不是音频文件");
    }
    Object voiceContent = OcrUtil.doFromVideo(fileInfo, null);
    Map<String, Object> params = new HashMap<>();
    params.put("voiceContent", voiceContent);
    return params;
  }
}
