package com.iwhalecloud.bote.service.plugin.runner;

import com.iwhalecloud.bote.cache.DingDingCache;
import com.iwhalecloud.bote.common.consts.PluginConsts;
import com.iwhalecloud.bote.common.enums.AttrDataType;
import com.iwhalecloud.bote.common.util.HttpUtil;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.plugin.params.DingDingWorkNoticePluginParams;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 发送钉钉工作通知
 *
 * @author qian.sisheng
 * @see <a href="https://open.dingtalk.com/document/orgapp/asynchronous-sending-of-enterprise-session-messages">asynchronous-sending-of-enterprise-session-messages</a>
 * @since 2025-04-12
 */
@Component
@SuppressWarnings("PMD.GuardLogStatement")
public class DingDingWorkNoticePlugin extends AbstractPlugin<DingDingWorkNoticePluginParams> {
  /** 发送钉钉工作通知 URL */
  private static final String NOTICE_URL = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";
  /** 获取TOKEN URL */
  private static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
  /** 钉钉工作通知请求参数 */
  private static final ClassPathResource resource = new ClassPathResource("/plugin-params/dingDingWorkNotice.json");

  private final DingDingCache dingDingCache;

  public DingDingWorkNoticePlugin(DingDingCache dingDingCache) {
    super(DingDingWorkNoticePluginParams.class);
    this.dingDingCache = dingDingCache;
  }

  @Override
  public ParameterSpec createRequestParameter() {
    try (InputStream inputStream = resource.getInputStream()) {
      return JsonUtil.parseJsonRequired(inputStream, ParameterSpec.class);
    }
    catch (Exception e) {
      logger.error("Failed to createRequestParameter, message={}", e.getMessage(), e);
      throw new BssException("创建钉钉机器人通知插件请求参数失败: ", e.getMessage(), e);
    }
  }

  @Override
  public ParameterSpec createResponseParameter() {
    return ParameterSpec.newRoot(Arrays.asList(
      ParameterSpec.newProperty("errmsg", "错误信息，执行成功ok", AttrDataType.STRING),
      ParameterSpec.newProperty("errcode", "错误码，0表示成功", AttrDataType.INTEGER),
      ParameterSpec.newProperty("taskId", "异步任务ID", AttrDataType.STRING),
      ParameterSpec.newProperty("requestId", "请求ID", AttrDataType.STRING)
    ));
  }

  @Override
  public String getPluginCode() {
    return PluginConsts.PLUGIN_CODE_DING_DING_WORK_NOTICE;
  }

  @Override
  public void validateParams(DingDingWorkNoticePluginParams params) {
    Assert.notNull(params.getAppKey(), "appKey不能为空");
    Assert.notNull(params.getAppSecret(), "appSecret不能为空");
    Assert.notNull(params.getAgentId(), "agentId不能为空");
    Assert.notNull(params.getMessage().getMsgType(), "msgType不能为空");
  }

  @Override
  public Object doRun(DingDingWorkNoticePluginParams plugin) {
    try {
      Map<String, Object> params = buildParams(plugin);
      params.put("agent_id", plugin.getAgentId());
      if (CollectionUtils.isNotEmpty(plugin.getUserIdList())) {
        params.put("userid_list", String.join(",", plugin.getUserIdList()));
      }
      if (CollectionUtils.isNotEmpty(plugin.getDeptIdList())) {
        params.put("dept_id_list", String.join(",", plugin.getDeptIdList()));
      }
      params.put("to_all_user", plugin.isToAllUser());
      String url = NOTICE_URL + "?access_token=" + getToken(plugin);
      Map<String, Object> result = HttpUtil.post(url, params, new ParameterizedTypeReference<Map<String, Object>>() {
      });
      if (!"0".equals(MapUtils.getString(result, "errcode"))) {
        throw new BssException("发送钉钉机器人通知失败: " + MapUtils.getString(result, "errmsg"));
      }
      result.put("requestId", result.get("request_id"));
      result.put("taskId", result.get("task_id"));
      result.remove("task_id");
      result.remove("request_id");
      return result;
    }
    catch (Exception e) {
      logger.error("发送钉钉钉工作通知失败: message={}", e.getMessage(), e);
      throw new BssException("发送钉钉钉工作通知失败: " + e.getMessage(), e);
    }
  }

  /**
   * 根据 appKey 和 appSecret 获取token
   *
   * @param plugin 插件
   * @return token
   */
  private String getToken(DingDingWorkNoticePluginParams plugin) {
    Assert.notNull(plugin.getAppKey(), "appKey不能为空");
    String token = dingDingCache.get(plugin.getAppKey());
    if (StringUtils.isNotEmpty(token)) {
      return token;
    }
    return refreshToken(plugin);
  }

  /**
   * 钉钉 access_token 的有效期为7200秒（2小时），有效期内重复获取会返回相同结果并自动续期，过期后获取会返回新的access_token
   *
   * @param plugin 插件
   * @see <a href= "https://open.dingtalk.com/document/orgapp/obtain-orgapp-token?spm=ding_open_doc.document.0.0.616d40e9AFsewo">obtain-orgapp-token</a>
   * @return token
   */
  private String refreshToken(DingDingWorkNoticePluginParams plugin) {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("appkey", plugin.getAppKey());
      params.put("appsecret", plugin.getAppSecret());
      Map<String, String> result = HttpUtil.get(TOKEN_URL, params, new ParameterizedTypeReference<Map<String, String>>() {
      });
      if ("0".equals(MapUtils.getString(result, "errcode"))) {
        String token = MapUtils.getString(result, "access_token");
        dingDingCache.save(plugin.getAppKey(), token);
        return token;
      }
      else {
        logger.error("获取钉钉token失败: message={}", MapUtils.getString(result, "errmsg"));
        throw new BssException("获取钉钉token失败: " + MapUtils.getString(result, "errmsg"));
      }
    }
    catch (Exception e) {
      logger.error("获取钉钉token失败: message={}", e.getMessage(), e);
      throw new BssException("获取钉钉token失败: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> buildParams(DingDingWorkNoticePluginParams plugin) {
    Map<String, Object> requestParams = new HashMap<>();
    Map<String, Object> params;
    // 处理模板卡片消息类型的特殊情况
    if (PluginConsts.MESSAGE_TYPE_LINK.equals(plugin.getMessage().getMsgType())) {
      params = JsonUtil.convert(plugin.getMessage(), Map.class);
    }
    else {
      params = snakeCaseMapper.convertValue(plugin.getMessage(), Map.class);
    }
    params.remove("msg_type");
    // 设置消息类型
    params.put(PluginConsts.MESSAGE_TYPE, plugin.getMessage().getMsgType());
    requestParams.put("msg", params);
    return requestParams;
  }
}
