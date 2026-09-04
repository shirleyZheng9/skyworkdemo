package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.DouYinVideoAnalysisPluginParams;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 抖音视频分析
 *
 * @author qian.sisheng
 * @since 2025-08-15
 */
@Component
public class DouYinVideoAnalysisPlugin extends AbstractPlugin<DouYinVideoAnalysisPluginParams> {

  private final IPluginRemoteService pluginRemoteService;

  public DouYinVideoAnalysisPlugin(IPluginRemoteService pluginRemoteService) {
    super(DouYinVideoAnalysisPluginParams.class);
    this.pluginRemoteService = pluginRemoteService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOU_YIN_VIDEO_ANALYSIS;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("url", "抖音视频链接", AttrDataType.STRING),
      ParameterSpec.newProperty("analysisMode", "解析模式(1：音频解析，2：音频解析+截图)", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    List<ParameterSpec> children = new ArrayList<>();
    List<ParameterSpec> sentenceChildren = new ArrayList<>();
    sentenceChildren.add(ParameterSpec.newProperty("text", "文本内容", AttrDataType.STRING));
    sentenceChildren.add(ParameterSpec.newProperty("start", "开始时间", AttrDataType.INTEGER));
    sentenceChildren.add(ParameterSpec.newProperty("end", "结束时间", AttrDataType.INTEGER));
    sentenceChildren.add(ParameterSpec.newProperty("imageUrl", "图片连接", AttrDataType.STRING));
    sentenceChildren.add(ParameterSpec.newProperty("fileId", "截图文件ID", AttrDataType.INTEGER));
    children.add(ParameterSpec.newList("sentences", "句子列表", ParameterSpec.newRoot(sentenceChildren)));
    children.add(ParameterSpec.newProperty("content", "完整内容", AttrDataType.STRING));
    return ParameterSpec.newRoot(children);
  }

  @Override
  public void validateParams(DouYinVideoAnalysisPluginParams params) {
    Assert.hasText(params.getUrl(), "抖音视频链接不能为空");
    if (!params.getUrl().startsWith("https://v.douyin.com") && !params.getUrl().startsWith("https://www.douyin.com/")) {
      throw new BssException("URL格式不正确，请提供以下任一格式的链接：\n" +
        "1. https://www.douyin.com/xx\n" +
        "2. 抖音分享短链接： https://v.douyin.com/xx");
    }
  }

  @Override
  public Object doRun(DouYinVideoAnalysisPluginParams pluginParams) {
    ResultVO<Object> result = pluginRemoteService.douYinVideoAnalysis(pluginParams.getUrl(), pluginParams.getAnalysisMode());
    if (result.isSuccess()) {
      DouYinVideoAnalysisDTO douYinVideoAnalysis = JsonUtil.convert(result.getResultObject(), DouYinVideoAnalysisDTO.class);
        for (SentenceDTO sentence : CollectionUtils.emptyIfNull(douYinVideoAnalysis.getSentences())) {
          if (sentence.getFileId() != null) {
            sentence.setImageUrl(getFileUrl(sentence.getFileId()));
          }
        }
      return douYinVideoAnalysis;
    }
    else {
      throw new BssException("抖音视频解析插件异常：" + result.getResultMsg());
    }
  }

  /**
   * 抖音视频解析结果
   */
  @Getter
  @Setter
  @ToString
  private static final class DouYinVideoAnalysisDTO {
    /** 句子列表 */
    private List<SentenceDTO> sentences;
    /** 整个视频内容 */
    private String content;
  }

  /**
   * 句子
   */
  @Getter
  @Setter
  @ToString
  private static final class SentenceDTO {
    /** 句子内容 */
    private String text;
    /** 开始时间 */
    private Long start;
    /** 结束时间 */
    private Long end;
    /** 图片ID */
    private Long fileId;
    /** 图片链接 */
    private String imageUrl;
  }
}
