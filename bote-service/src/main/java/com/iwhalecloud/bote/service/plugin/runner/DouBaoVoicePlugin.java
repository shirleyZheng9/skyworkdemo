package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.DouBaoVoicePluginParams;
import com.iwhalecloud.bote.dto.plugin.params.DouBaoVoicePluginParams.User;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.file.service.IFileStoreService;
import com.iwhalecloud.bss.litchi.file.vo.FileInfoVO;
import com.iwhalecloud.bss.litchi.file.vo.UploadConfigVO;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 豆包语音合成插件
 * <p>使用的v1非流式接口</p>
 *
 * @author qian.sisheng
 * @since 2025-10-30
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DouBaoVoicePlugin extends AbstractPlugin<DouBaoVoicePluginParams> {
  private static final Logger logger = LoggerFactory.getLogger(DouBaoVoicePlugin.class);
  /** 豆包语音合成插件参数 */
  private static final ClassPathResource resource = new ClassPathResource("/plugin-params/douBaoVoiceParams.json");

  private final IFileStoreService fileStoreService;

  public DouBaoVoicePlugin(IFileStoreService fileStoreService) {
    super(DouBaoVoicePluginParams.class);
    this.fileStoreService = fileStoreService;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOUBAO_VOICE;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    try (InputStream inputStream = resource.getInputStream()) {
      return JsonUtil.parseJsonRequired(inputStream, ParameterSpec.class);
    }
    catch (Exception e) {
      logger.error("Failed to create douBao voice parameters, message={}", e.getMessage(), e);
      throw new BssException("创建豆包语音合成插件参数失败: ", e.getMessage(), e);
    }
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("fileId", "音频文件ID", AttrDataType.INTEGER),
      ParameterSpec.newProperty("fileUrl", "音频文件访问链接", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(DouBaoVoicePluginParams params) {
    Assert.notNull(params.getApp(), "app不能为空");
    Assert.hasText(params.getApp().getAppid(), "appid不能为空");
    Assert.hasText(params.getApp().getToken(), "token不能为空");
    Assert.hasText(params.getApp().getCluster(), "cluster不能为空");
    Assert.notNull(params.getAudio(), "audio不能为空");
    Assert.hasText(params.getAudio().getVoiceType(), "voiceType不能为空");
    Assert.notNull(params.getRequest(), "appid不能为空");
    Assert.hasText(params.getRequest().getText(), "text不能为空");
  }

  @Override
  public Object doRun(DouBaoVoicePluginParams pluginParams) {
    String url = SystemParameter.DOUBAO_VOICE_URL.getValueFromDb();
    if (StringUtils.isEmpty(url)) {
      throw new BssException("未配置豆包语音合成接口地址");
    }
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer;" + pluginParams.getApp().getToken());
    // 设置请求ID
    pluginParams.getRequest().setReqid(UUID.randomUUID().toString());
    // 非流式
    pluginParams.getRequest().setOperation("query");
    // 设置用户ID
    if (pluginParams.getUser() == null) {
      User user = new User();
      user.setUid(String.valueOf(SessionUtil.getLoginInfo().getUserId()));
      pluginParams.setUser(user);
    }
    Map<String, Object> request = snakeCaseMapper.convertValue(pluginParams, new TypeReference<>() {
    });
    DouBaoVoiceResponse response = HttpUtil.post(url, request, new ParameterizedTypeReference<>() {
    }, httpHeaders);
    if (response == null) {
      throw new BssException("调用豆包语音合成失败, 响应为空");
    }
    if (!"3000".equals(response.getCode())) {
      logger.error("调用豆包语音合成失败：response ={}", response);
      throw new BssException("调用豆包语音合成失败," + "code=" + response.getCode() + "message=" + response.getMessage());
    }
    if (response.getSequence() >= 0) {
      throw new BssException("调用豆包语音合成失败, sequence=" + response.getSequence());
    }
    // 上传音频文件
    FileInfoVO fileInfo = uploadFile(pluginParams, response.getData());
    return Map.of("fileUrl", getFileUrl(fileInfo.getFileId()), "fileId", fileInfo.getFileId());
  }

  /**
   * 上传文件
   */
  private FileInfoVO uploadFile(DouBaoVoicePluginParams pluginParams, String data) {
    byte[] decode = Base64.getDecoder().decode(data);
    UploadConfigVO uploadConfig = new UploadConfigVO();
    uploadConfig.setFileType(pluginParams.getAudio().getEncoding());
    uploadConfig.setFileSize((long) decode.length);
    uploadConfig.setDescription("豆包语音合成");
    uploadConfig.setSubFolder("doubao-voice");
    uploadConfig.setOriginalFileName("douBaoVoice_" + System.currentTimeMillis() + "." + pluginParams.getAudio().getEncoding());
    return fileStoreService.uploadFile(decode, uploadConfig);
  }

  @Getter
  @Setter
  @ToString
  private static final class DouBaoVoiceResponse {
    /** 请求 ID */
    private String reqid;
    /** 请求状态码 3000 请求正常 */
    private String code;
    /** 操作 query（非流式，http 只能 query） / submit（流式） */
    private String operation;
    /** 请求状态信息 */
    private String message;
    /** 合成音频数据，base64 编码 */
    private String data;
    /** 音频段序号, 负数表示合成完毕 */
    private Integer sequence;
    /** 额外信息 */
    private Addition addition;
  }

  @Getter
  @Setter
  @ToString
  private static final class Addition {
    /** 音频时长 */
    private String duration;
  }
}
