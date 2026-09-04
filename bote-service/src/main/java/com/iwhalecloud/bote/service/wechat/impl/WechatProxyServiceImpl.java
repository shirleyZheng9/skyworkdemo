package com.iwhalecloud.bote.service.wechat.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.ChatConsts;
import com.iwhalecloud.bote.common.consts.ChatMessageType;
import com.iwhalecloud.bote.common.thread.ThreadPools;
import com.iwhalecloud.bote.common.util.SceneContextUtil;
import com.iwhalecloud.bote.dto.chat.ReplyDTO;
import com.iwhalecloud.bote.dto.orchestration.OrchestrationEngineResponse;
import com.iwhalecloud.bote.dto.publish.ResourcePublishRecordDTO;
import com.iwhalecloud.bote.dto.scene.SceneChatParamsDTO;
import com.iwhalecloud.bote.dto.wechat.WechatCfgDTO;
import com.iwhalecloud.bote.dto.wechat.WechatMsgDTO;
import com.iwhalecloud.bote.mapper.publish.ResourcePublishRecordMapper;
import com.iwhalecloud.bote.service.orchestration.reply.handlers.NonStreamFlowReplyHandler;
import com.iwhalecloud.bote.service.scene.ISceneChatService;
import com.iwhalecloud.bote.service.wechat.IWechatProxyService;
import com.iwhalecloud.bote.wechat.WechatApiClient;
import com.iwhalecloud.bote.wechat.WechatParamHelper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * IWechatProxyService 实现
 * <p>符合微信明文/安全模式的签名校验与消息解密规范：
 * - 明文模式：signature = sha1(sorted(token, timestamp, nonce))； - 安全模式：msg_signature = sha1(sorted(token, timestamp, nonce,
 * Encrypt))。
 * <p>默认快速回包 "success"；如需业务自定义回包体，请在异步处理内接入并按需进行加密回包。
 *
 * @author lizuyin
 * @since 2025-08-08
 */

@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class WechatProxyServiceImpl implements IWechatProxyService {

  private static final Logger logger = LoggerFactory.getLogger(WechatProxyServiceImpl.class);

  private final ResourcePublishRecordMapper resourcePublishRecordMapper;

  private final ISceneChatService sceneChatService;

  private final WechatApiClient wechatApiClient;

  private final WechatParamHelper wechatParamHelper;

  private static final String DEFUALT_REPLY_TEXT = "思考中...";

  private static final String DEFUALT_REPLY_FORBIDDEN = "forbidden";

  private static final String DEFUALT_REPLY_SUCCESS = "success";

  /**
   * 微信服务器 GET 验证（兼容明文/安全模式）。
   * <p>安全模式场景下，实际场景会传入 msg_signature。由于控制器参数名可能统一为 signature，
   * 这里同时支持两种验签计算： - sha1(sorted(token, timestamp, nonce))； - sha1(sorted(token, timestamp, nonce, echoStr))。
   */
  @Override
  public String wechatEchoVerify(WechatMsgDTO msgDTO) {
    String code = msgDTO.getCode();
    String signature = msgDTO.getMsgSignature();
    String timestamp = msgDTO.getTimestamp();
    String nonce = msgDTO.getNonce();
    String echoStr = msgDTO.getEchoStr();
    WechatCfgDTO cfgDTO = getCfgMap(code);
    Map<String, Object> cfgMap = cfgDTO.getCfgMap();
    String token = String.valueOf(cfgMap.get("token"));
    if (StringUtils.isAnyBlank(signature, timestamp, nonce, echoStr) || StringUtils.isBlank(token)) {
      return DEFUALT_REPLY_FORBIDDEN;
    }
    String sigPlain = wechatParamHelper.getSHA1(token, timestamp, nonce, null);
    String sigSafe = wechatParamHelper.getSHA1(token, timestamp, nonce, echoStr);
    return signature.equals(sigPlain) || signature.equals(sigSafe) ? echoStr : DEFUALT_REPLY_FORBIDDEN;
  }

  /**
   * 微信消息/事件 POST 投递处理（明文/安全模式）。
   * <p>明文：校验 signature = sha1(sorted(token, timestamp, nonce))。
   * <p>安全：校验 msg_signature，并解密出真实明文。
   */
  @Override
  public String wechatProxy(WechatMsgDTO msgDTO) {
    String code = msgDTO.getCode();
    String signature = msgDTO.getMsgSignature();
    String timestamp = msgDTO.getTimestamp();
    String nonce = msgDTO.getNonce();
    String openid = msgDTO.getOpenid();
    String xml = msgDTO.getXml();
    logger.debug("[wechatProxy] xml, code={}, openid={}", code, openid);
    WechatCfgDTO cfgDTO = getCfgMap(code);
    String sceneId = String.valueOf(cfgDTO.getRecord().getResourceId());
    String tenantId = String.valueOf(cfgDTO.getRecord().getTenantId());
    Map<String, Object> cfgMap = cfgDTO.getCfgMap();
    String token = String.valueOf(cfgMap.get("token"));
    if (isInvalidBasicParams(signature, timestamp, nonce, token)) {
      return DEFUALT_REPLY_FORBIDDEN;
    }
    String xmlTrimmed = StringUtils.trimToEmpty(xml);
    boolean isSafeMode = isSafeModeXml(xmlTrimmed);
    return isSafeMode
      ? handleSafeMode(sceneId, tenantId, signature, timestamp, nonce, openid, xml, cfgMap, token)
      : handlePlainMode(sceneId, tenantId, signature, timestamp, nonce, xmlTrimmed, cfgMap, token);
  }

  /**
   * 调用场景服务并获取响应
   *
   * @param sceneId 场景ID
   * @param tenantId 租户ID
   * @param messageMap 微信XML消息
   * @param openid 用户openid
   * @return 场景服务响应文本
   */
  private String callSceneServiceAndGetResponse(String sceneId, String tenantId, Map<String, String> messageMap,
    String openid) {
    try {
      if (!isValidTextMessage(messageMap)) {
        logger.debug("[callSceneServiceAndGetResponse] 非文本消息或解析失败，跳过场景调用");
        return "非文本消息，暂不支持处理！";
      }
      String content = messageMap.get("Content");
      if (StringUtils.isBlank(content)) {
        logger.debug("[callSceneServiceAndGetResponse] 消息内容为空，跳过场景调用");
        return "空消息，请重新发送！";
      }
      SceneChatParamsDTO sceneChatParams = buildSceneChatParams(sceneId, tenantId, content, openid);
      logger.debug("[callSceneServiceAndGetResponse] 调用场景服务，sceneId={}, tenantId={}, content={}", sceneId,
        tenantId, content);
      OrchestrationEngineResponse response = sceneChatService.run(sceneChatParams);
      return processSceneServiceResponse(response, sceneChatParams);
    }
    catch (Exception e) {
      logger.error("[callSceneServiceAndGetResponse] 调用场景服务异常", e);
      return "调用服务异常，请联系管理员！";
    }
  }

  /**
   * 验证是否为有效的文本消息
   *
   * @param messageMap 消息Map
   * @return 是否为有效文本消息
   */
  private boolean isValidTextMessage(Map<String, String> messageMap) {
    if (messageMap == null) {
      return false;
    }
    return "text".equals(messageMap.get("MsgType"));
  }

  /**
   * 构建场景聊天参数
   *
   * @param sceneId 场景ID
   * @param tenantId 租户ID
   * @param content 消息内容
   * @param openid 用户openid
   * @return 场景聊天参数
   */
  private SceneChatParamsDTO buildSceneChatParams(String sceneId, String tenantId, String content, String openid) {
    SceneChatParamsDTO sceneChatParams = new SceneChatParamsDTO();
    sceneChatParams.setDebug(false);
    sceneChatParams.setDebugInnerService(false);
    sceneChatParams.setLogEnabled(false);
    sceneChatParams.setTenantId(parseLong(tenantId));
    sceneChatParams.setSceneId(parseLong(sceneId));
    sceneChatParams.setMessageContent(content);
    sceneChatParams.setContextId(SceneContextUtil.newContextId());
    sceneChatParams.setConversationId(ChatConsts.WECHAT_SESSION_ID);
    sceneChatParams.setHistoryMessagesLoader(ArrayList::new);

    // 设置非流式回复处理器
    NonStreamFlowReplyHandler replyHandler = new NonStreamFlowReplyHandler(openid);
    sceneChatParams.setReplyHandler(replyHandler);

    return sceneChatParams;
  }

  /**
   * 处理场景服务响应
   *
   * @param response 编排引擎响应
   * @param sceneChatParams 场景聊天参数
   * @return 回复文本内容
   */
  private String processSceneServiceResponse(OrchestrationEngineResponse response, SceneChatParamsDTO sceneChatParams) {
    if (!Boolean.TRUE.equals(response.getSuccess())) {
      logger.warn("[callSceneServiceAndGetResponse] 场景服务调用失败: {}", response.getFailMsg());
      return "场景服务调用失败，请联系管理员！";
    }
    // 获取回复内容
    String replyText = extractReplyTextFromHandler(sceneChatParams);
    if (StringUtils.isNotBlank(replyText)) {
      logger.debug("[callSceneServiceAndGetResponse] 场景服务调用成功，回复内容: {}", replyText);
      return replyText;
    }
    return null;
  }

  /**
   * 从回复处理器中提取回复文本
   *
   * @param sceneChatParams 场景聊天参数
   * @return 回复文本内容
   */
  private String extractReplyTextFromHandler(SceneChatParamsDTO sceneChatParams) {
    try {
      // 从场景聊天参数中获取回复处理器
      NonStreamFlowReplyHandler replyHandler = (NonStreamFlowReplyHandler) sceneChatParams.getReplyHandler();
      if (replyHandler == null) {
        logger.warn("[extractReplyTextFromHandler] 回复处理器为空");
        return null;
      }
      // 获取回复列表
      List<ReplyDTO> replies = replyHandler.getReplies();
      if (CollectionUtils.isEmpty(replies)) {
        logger.debug("[extractReplyTextFromHandler] 回复列表为空");
        return null;
      }
      // 查找文本类型的回复
      Optional<ReplyDTO> textReply = replies.stream()
        .filter(reply -> ChatMessageType.TEXT.equals(reply.getType()) && StringUtils.isNotBlank(reply.getText()))
        .findFirst();
      if (textReply.isPresent()) {
        return textReply.get().getText();
      }
      logger.debug("[extractReplyTextFromHandler] 未找到有效的文本回复");
      return null;
    }
    catch (Exception e) {
      logger.warn("[extractReplyTextFromHandler] 从回复处理器提取回复文本失败: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 构造微信回复消息XML
   *
   * @param openid 用户openid
   * @return 微信回复XML
   */
  private String buildWechatReplyXml(String openid, String fromUserName) {

    return String.format(
      "<xml>" + "<ToUserName><![CDATA[%s]]></ToUserName>" + "<FromUserName><![CDATA[%s]]></FromUserName>"
        + "<CreateTime>%d</CreateTime>" + "<MsgType><![CDATA[text]]></MsgType>" + "<Content><![CDATA[%s]]></Content>"
        + "</xml>", openid, fromUserName, System.currentTimeMillis() / 1000, DEFUALT_REPLY_TEXT);
  }

  /**
   * 提交异步任务执行
   */
  private void submitAsync(Runnable task) {
    try {
      ThreadPools.getCommon().submit(task);
    }
    catch (Exception e) {
      logger.warn("[submitAsync] 提交异步任务失败: {}", e.getMessage());
      // 兜底直接同步执行，避免消息丢失
      try {
        task.run();
      }
      catch (Exception ex) {
        logger.error("[submitAsync] 同步兜底执行失败", ex);
      }
    }
  }

  /**
   * 异步执行业务逻辑（明文/安全模式通用），并主动下发最终智能体响应
   */
  private void handleBusinessAsync(String sceneId, String tenantId, Map<String, String> messageMap, String openid,
    Map<String, Object> cfgMap) {
    try {
      // 复用原有处理逻辑
      String sceneResponse = callSceneServiceAndGetResponse(sceneId, tenantId, messageMap, openid);
      if (StringUtils.isNotBlank(sceneResponse)) {
        String appId = cfgMap == null ? null : String.valueOf(cfgMap.getOrDefault("appId", ""));
        String secret = cfgMap == null ? null : String.valueOf(cfgMap.getOrDefault("secret", ""));
        if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(secret)) {
          wechatApiClient.sendText(openid, sceneResponse, appId, secret);
        }
      }
    }
    catch (Exception e) {
      logger.error("[handleBusinessAsync] 异步处理失败", e);
    }
  }

  /**
   * 异步执行业务逻辑（安全模式场景：沿用已解析的 appId/secret）
   */
  private void handleBusinessAsync(String sceneId, String tenantId, Map<String, String> messageMap, String openid,
    String appId, String secret) {
    try {
      String sceneResponse = callSceneServiceAndGetResponse(sceneId, tenantId, messageMap, openid);
      if (StringUtils.isNotBlank(sceneResponse)) {
        wechatApiClient.sendText(openid, sceneResponse, appId, secret);
      }
    }
    catch (Exception e) {
      logger.error("[handleBusinessAsync(safe)] 异步处理失败", e);
    }
  }

  /**
   * 从发布记录中加载微信配置
   */
  private WechatCfgDTO getCfgMap(String code) {
    ResourcePublishRecordDTO record = loadWechatRecord(code);
    Map<String, Object> cfgMap = JsonUtil.parseJson(record.getPublishParams(),
      new TypeReference<Map<String, Object>>() {
      });
    WechatCfgDTO cfgDTO = new WechatCfgDTO();
    cfgDTO.setCfgMap(cfgMap);
    cfgDTO.setRecord(record);
    return cfgDTO;
  }

  /**
   * 解析明文XML为Map，仅提取必要字段 FB误报，使用注解镇压XXE_DOCUMENT报错
   */
  @SuppressFBWarnings("XXE_DOCUMENT")
  private Map<String, String> parsePlainXmlToMap(String xml) {
    Map<String, String> map = new HashMap<>();
    if (StringUtils.isBlank(xml)) {
      return map;
    }
    try {
      DocumentBuilderFactory dbf = wechatParamHelper.buildDocumentBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      try (StringReader sr = new StringReader(xml)) {
        InputSource is = new InputSource(sr);
        Document document = db.parse(is);
        Element root = document.getDocumentElement();
        putIfPresent(map, "ToUserName", textOf(root, "ToUserName"));
        putIfPresent(map, "FromUserName", textOf(root, "FromUserName"));
        putIfPresent(map, "MsgType", textOf(root, "MsgType"));
        putIfPresent(map, "Content", textOf(root, "Content"));
      }
    }
    catch (Exception e) {
      logger.warn("[parsePlainXmlToMap] 解析明文XML失败: {}", e.getMessage());
    }
    return map;
  }

  /**
   * 添加字段
   */
  private static void putIfPresent(Map<String, String> map, String key, String val) {
    if (StringUtils.isNotBlank(val)) {
      map.put(key, val);
    }
  }

  /**
   * 获取标签文本内容
   */
  private static String textOf(Element root, String tag) {
    NodeList list = root.getElementsByTagName(tag);
    if (list.getLength() > 0 && list.item(0) != null) {
      return list.item(0).getTextContent();
    }
    return null;
  }

  /**
   * 加载微信配置记录
   **/
  private ResourcePublishRecordDTO loadWechatRecord(String code) {
    return resourcePublishRecordMapper.getRecordByCallbackCode(code);
  }

  /**
   * 字符串转Long
   */
  private static Long parseLong(String v) {
    return StringUtils.isBlank(v)
      ? null
      : Optional.of(v).map(String::trim).filter(StringUtils::isNotEmpty).map(Long::valueOf).orElse(null);
  }

  /**
   * 基础参数校验（signature/timestamp/nonce 与 token）
   *
   * @param signature 签名
   * @param timestamp 时间戳
   * @param nonce 随机串
   * @param tokenToUse token
   * @return 参数不合法返回 true
   */
  private boolean isInvalidBasicParams(String signature, String timestamp, String nonce, String tokenToUse) {
    return StringUtils.isAnyBlank(signature, timestamp, nonce) || StringUtils.isBlank(tokenToUse);
  }

  /**
   * 是否为安全模式（存在 Encrypt 节点）
   *
   * @param xmlTrimmed 去空白后的原始XML
   * @return 是否安全模式
   */
  private boolean isSafeModeXml(String xmlTrimmed) {
    return Strings.CS.contains(xmlTrimmed, "<Encrypt>");
  }

  /**
   * 明文模式处理：验签通过后，异步执行业务并快速回包。
   *
   * @param sceneId 场景ID
   * @param tenantId 租户ID
   * @param signature 签名
   * @param timestamp 时间戳
   * @param nonce 随机串
   * @param xmlTrimmed 去空白后的XML
   * @param cfgMap 发布配置
   * @param tokenToUse token
   * @return 回包XML或 success/forbidden
   */
  private String handlePlainMode(String sceneId, String tenantId, String signature, String timestamp, String nonce,
    String xmlTrimmed, Map<String, Object> cfgMap, String tokenToUse) {
    String calc = wechatParamHelper.getSHA1(tokenToUse, timestamp, nonce, null);
    if (!signature.equals(calc)) {
      return DEFUALT_REPLY_FORBIDDEN;
    }

    Map<String, String> messageMap = parsePlainXmlToMap(xmlTrimmed);
    String toOpenId = messageMap.get("FromUserName");
    String fromOpenId = messageMap.get("ToUserName");
    submitAsync(() -> handleBusinessAsync(sceneId, tenantId, messageMap, toOpenId, cfgMap));
    return StringUtils.isNotBlank(toOpenId) ? buildWechatReplyXml(toOpenId, fromOpenId) : DEFUALT_REPLY_SUCCESS;
  }

  /**
   * 安全模式处理：解密消息，异步执行业务并快速回包。
   *
   * @param sceneId 场景ID
   * @param tenantId 租户ID
   * @param signature msg_signature
   * @param timestamp 时间戳
   * @param nonce 随机串
   * @param openid 可选的 openid
   * @param xml 原始XML
   * @param cfgMap 发布配置
   * @param tokenToUse token
   * @return 回包XML或 success/forbidden
   */
  private String handleSafeMode(String sceneId, String tenantId, String signature, String timestamp, String nonce,
    String openid, String xml, Map<String, Object> cfgMap, String tokenToUse) {
    try {
      String appId = cfgMap == null ? null : String.valueOf(cfgMap.getOrDefault("appId", ""));
      String aesKey = cfgMap == null ? null : String.valueOf(cfgMap.getOrDefault("aesKey", ""));
      String secret = cfgMap == null ? null : String.valueOf(cfgMap.getOrDefault("secret", ""));
      logger.debug("[wechatProxy] appId/aesKey, sceneId={}, tenantId={}, appId={}, aesKey={}", sceneId, tenantId, appId,
        aesKey);
      if (StringUtils.isAnyBlank(appId, aesKey)) {
        logger.warn("[wechatProxy] missing appId/aesKey, sceneId={}, tenantId={}", sceneId, tenantId);
        return DEFUALT_REPLY_FORBIDDEN;
      }
      Map<String, String> messageMap = wechatParamHelper.decryptMsg(tokenToUse, aesKey, appId, signature, timestamp,
        nonce, xml);
      logger.debug("[wechatProxy] decrypted map, sceneId={}, tenantId={}, openid={}, messageMap={}", sceneId, tenantId,
        StringUtils.trimToEmpty(openid), messageMap);
      String toOpenId = StringUtils.isNotBlank(openid) ? openid : messageMap.get("FromUserName");
      String fromOpenId = messageMap.get("ToUserName");
      submitAsync(() -> handleBusinessAsync(sceneId, tenantId, messageMap, toOpenId, appId, secret));
      return StringUtils.isNotBlank(toOpenId) ? buildWechatReplyXml(toOpenId, fromOpenId) : DEFUALT_REPLY_SUCCESS;
    }
    catch (Exception e) {
      logger.warn("[wechatProxy] decrypt failed: sceneId={}, tenantId={}, err={}", sceneId, tenantId, e.getMessage());
      return DEFUALT_REPLY_FORBIDDEN;
    }
  }
}





