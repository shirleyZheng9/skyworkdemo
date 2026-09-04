package com.iwhalecloud.bote.service.plugin.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.common.util.PluginContextUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.SimpleLargeModelDTO;
import com.iwhalecloud.bote.dto.plugin.PluginExecutionContext;
import com.iwhalecloud.bote.dto.plugin.params.DouBaoText2videoParams;
import com.iwhalecloud.bote.mapper.model.LargeModelManageMapper;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 文生视频
 *
 * @author fan.cong
 * @since 2025-08-06
 */
@Component
public class DouBaoText2videoPlugin extends AbstractPlugin<DouBaoText2videoParams> {
  private final LargeModelManageMapper largeModelManageMapper;

  public DouBaoText2videoPlugin(LargeModelManageMapper largeModelManageMapper) {
    super(DouBaoText2videoParams.class);
    this.largeModelManageMapper = largeModelManageMapper;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("prompt", "提示语", AttrDataType.STRING),
      ParameterSpec.newProperty("duration", "时长", AttrDataType.INTEGER),
      ParameterSpec.newProperty("resolution", "分辨率", AttrDataType.STRING),
      ParameterSpec.newProperty("ratio", "宽高比", AttrDataType.STRING)));
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(ParameterSpec.newProperty("message", "消息", AttrDataType.STRING),
      ParameterSpec.newProperty("videoUrl", "视频url", AttrDataType.STRING)));
  }

  @Override
  public void validateParams(DouBaoText2videoParams params) {
    Assert.notNull(params.getPrompt(), "提示语不能为空");
    Assert.notNull(params.getDuration(), "时长不能为空");
    Assert.notNull(params.getResolution(), "分辨率不能为空");
    Assert.notNull(params.getRatio(), "宽高比不能为空");
  }

  @Override
  @SuppressWarnings("PMD.GuardLogStatement")
  public Object doRun(DouBaoText2videoParams pluginParams) {

    Map<String, Object> result = new HashMap<>();
    PluginExecutionContext pluginContext = PluginContextUtil.getContext();
    SimpleLargeModelDTO simpleLargeModelDTO = largeModelManageMapper.selectLargeModelById(pluginContext.getTenantId(), pluginContext.getModelId());
    if (simpleLargeModelDTO == null) {
      throw new BssException("选择的模型不存在！");
    }
    Assert.notNull(simpleLargeModelDTO.getAccessKey(), "模型API key不能为空!");
    try {
      // 1. 获取请求参数
      Map<String, Object> payload = getStringObjectMap(pluginParams, simpleLargeModelDTO.getModelCode());

      // 3. 发送任务创建请求
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(simpleLargeModelDTO.getAccessKey());
      headers.setContentType(MediaType.APPLICATION_JSON);
      String url = simpleLargeModelDTO.getAccessUrl() + "/api/v3/contents/generations/tasks";
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
      ResponseEntity<String> resp = HttpUtil.getRestTemplate().postForEntity(url, entity, String.class);

      if (!resp.getStatusCode().is2xxSuccessful()) {
        throw new BssException("请求生成视频失败");
      }

      String responseBody = resp.getBody();
      Assert.hasLength(responseBody, "请求生成视频失败，响应为空");
      JsonNode root = JsonUtil.readTree(responseBody);
      String taskId = root.path("id").asText(null);
      if (StringUtils.isEmpty(taskId)) {
        throw new BssException("未获取到任务ID");
      }
      logger.info("任务已创建，ID: {}", taskId);

      // 4. 轮询任务状态
      String statusUrl = simpleLargeModelDTO.getAccessUrl() + "/api/v3/contents/generations/tasks/" + taskId;
      int retry = 0;
      int maxRetry = 60;
      String videoUrl = null;
      while (retry < maxRetry) {
        Thread.sleep(5000);
        ResponseEntity<String> statusResp = HttpUtil.getRestTemplate()
          .exchange(statusUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        String body = statusResp.getBody();
        Assert.hasLength(body, "查询生成视频任务状态失败，响应为空");
        JsonNode statusNode = JsonUtil.readTree(body);
        String status = statusNode.path("status").asText();
        if ("succeeded".equals(status)) {
          videoUrl = statusNode.path("content").path("video_url").asText(null);
          break;
        }
        if ("failed".equals(status)) {
          throw new BssException("生成失败: " + statusNode.path("error").path("message").asText(""));
        }
        if ("canceled".equals(status)) {
          throw new BssException("任务被取消");
        }
        logger.info("任务{},生成中，已等待 {} 秒...", taskId, (retry + 1) * 5);
        retry++;
      }
      if (StringUtils.isNotEmpty(videoUrl)) {
        result.put("videoUrl", videoUrl);
        result.put("message", "视频生成成功");
        return result;
      }
      else {
        throw new BssException("生成超时或失败");
      }
    }
    catch (InterruptedException e) {
      logger.error("生成视频异常:{}", e.getMessage(), e);
      throw new BssException("生成视频异常" + e.getMessage(), e);
    }
  }

  private static Map<String, Object> getStringObjectMap(DouBaoText2videoParams pluginParams, String modelCode) {
    // 构造请求体
    String prompt = pluginParams.getPrompt()
      + " --rt " + pluginParams.getRatio()
      + " --dur " + pluginParams.getDuration()
      + " --rs " + pluginParams.getResolution();

    // 构造 content 列表
    List<Map<String, Object>> content = new ArrayList<>();
    // 添加文本部分
    Map<String, Object> textMap = new HashMap<>();
    textMap.put("text", prompt);
    textMap.put("type", "text");
    content.add(textMap);

    // 构造 payload
    Map<String, Object> payload = new HashMap<>();
    payload.put("model", modelCode);
    payload.put("content", content);
    return payload;
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DOUBAO_TEXT_2_VIDEO;
  }
}
