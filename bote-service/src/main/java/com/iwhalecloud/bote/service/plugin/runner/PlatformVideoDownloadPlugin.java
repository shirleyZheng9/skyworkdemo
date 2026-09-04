package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.PlatformVideoDownloadPluginParams;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;

import java.util.Arrays;
import java.util.Collections;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 多平台视频下载（支持抖音、小红书、Bilibili、快手、西瓜视频等）
 *
 * @author auto
 * @since 2025-10-21
 */
@Component
public class PlatformVideoDownloadPlugin extends AbstractPlugin<PlatformVideoDownloadPluginParams> {

  private final IPluginRemoteService pluginRemoteService;

  public PlatformVideoDownloadPlugin(IPluginRemoteService pluginRemoteService) {
    super(PlatformVideoDownloadPluginParams.class);
    this.pluginRemoteService = pluginRemoteService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_PLATFORM_VIDEO_DOWNLOAD;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Collections.singletonList(
      ParameterSpec.newProperty("url", "视频链接（支持抖音、小红书、Bilibili、快手、西瓜视频等平台）", AttrDataType.STRING)
    ));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("fileId", "视频文件ID", AttrDataType.INTEGER),
      ParameterSpec.newProperty("fileUrl", "视频文件访问链接", AttrDataType.STRING),
      ParameterSpec.newProperty("coverFileId", "封面文件ID", AttrDataType.INTEGER),
      ParameterSpec.newProperty("coverUrl", "封面文件访问链接", AttrDataType.STRING),
      ParameterSpec.newProperty("title", "视频标题", AttrDataType.STRING)
    ));
  }

  @Override
  public void validateParams(PlatformVideoDownloadPluginParams params) {
    Assert.hasText(params.getUrl(), "视频链接不能为空");
    // 多平台支持，不再限制特定平台的URL格式
    String url = params.getUrl();
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      throw new BssException("URL格式不正确，必须以 http:// 或 https:// 开头");
    }
  }

  @Override
  public Object doRun(PlatformVideoDownloadPluginParams pluginParams) {
    ResultVO<Object> result = pluginRemoteService.platformVideoDownload(pluginParams.getUrl());
    if (result.isSuccess()) {
      PlatformVideoDownloadDTO downloadResult = JsonUtil.convert(result.getResultObject(), PlatformVideoDownloadDTO.class);
      // 构建文件访问URL
      if (downloadResult.getFileId() != null) {
        downloadResult.setFileUrl(getFileUrl(downloadResult.getFileId()));
      }
      if (downloadResult.getCoverFileId() != null) {
        downloadResult.setCoverUrl(getFileUrl(downloadResult.getCoverFileId()));
      }
      return downloadResult;
    }
    else {
      throw new BssException("多平台视频下载插件异常：" + result.getResultMsg());
    }
  }

  /**
   * 多平台视频下载结果
   */
  @Getter
  @Setter
  @ToString
  public static final class PlatformVideoDownloadDTO {
    /** 视频文件ID */
    private Long fileId;
    /** 视频文件访问链接 */
    private String fileUrl;
    /** 封面文件ID */
    private Long coverFileId;
    /** 封面文件访问链接 */
    private String coverUrl;
    /** 视频标题 */
    private String title;
  }
}

