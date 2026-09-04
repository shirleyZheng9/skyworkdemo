package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.VideoAnalysisPluginParams;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频解析插件
 */
@Component
public class VideoAnalysisPlugin extends AbstractPlugin<VideoAnalysisPluginParams> {

  private final IPluginRemoteService pluginRemoteService;

  public VideoAnalysisPlugin(IPluginRemoteService pluginRemoteService) {
    super(VideoAnalysisPluginParams.class);
    this.pluginRemoteService = pluginRemoteService;
  }


  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_VIDEO_ANALYSIS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    children.add(ParameterSpec.newProperty("analysisMode", "解析模式(1：音频解析，2：音频解析+截图)", AttrDataType.STRING));
    children.add(ParameterSpec.newProperty("fileId", "文件ID", AttrDataType.NUMBER));
    return ParameterSpec.newRoot(children);
  }


  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    // fileIds数组
    children.add(ParameterSpec.newProperty("fileIds", "文件ID列表", AttrDataType.ARRAY));
    // sentences数组
    List<ParameterSpec> sentenceChildren = new ArrayList<>();
    sentenceChildren.add(ParameterSpec.newProperty("text", "文本内容", AttrDataType.STRING));
    sentenceChildren.add(ParameterSpec.newProperty("start", "开始时间", AttrDataType.STRING));
    sentenceChildren.add(ParameterSpec.newProperty("end", "结束时间", AttrDataType.STRING));
    sentenceChildren.add(ParameterSpec.newProperty("timestamp", "时间戳", AttrDataType.ARRAY));
    sentenceChildren.add(ParameterSpec.newProperty("raw_text", "原始文本", AttrDataType.STRING));
    sentenceChildren.add(ParameterSpec.newProperty("spk", "说话人", AttrDataType.NUMBER));
    sentenceChildren.add(ParameterSpec.newProperty("fileId", "截图文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newList("sentences", "句子列表", ParameterSpec.newRoot(sentenceChildren)));
    children.add(ParameterSpec.newProperty("content", "完整内容", AttrDataType.STRING));

    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(VideoAnalysisPluginParams params) {
    Assert.notNull(params.getFileId(), "文件ID不能为空");
  }

  @Override
  @SuppressFBWarnings("SECSQLISPRJDBC")
  public Object doRun(VideoAnalysisPluginParams pluginParams) {
    ResultVO<Object> resultVO = pluginRemoteService.videoAnalysis(pluginParams.getFileId(), pluginParams.getAnalysisMode());
    if (resultVO.isSuccess()) {
      return resultVO.getResultObject();
    } else {
      throw new BssException("视频解析插件异常：" + resultVO.getResultMsg());
    }
  }
}
