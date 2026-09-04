package com.iwhalecloud.bote.service.publish.platform.feishu;

import com.iwhalecloud.bote.service.publish.platform.feishu.dto.FeishuMessageResponse.ImageResponse.ImageContent;
import com.iwhalecloud.bote.service.publish.platform.feishu.dto.FeishuMessageResponse.TextResponse.TextContent;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.lark.oapi.Client;
import com.lark.oapi.service.application.v6.model.GetApplicationReq;
import com.lark.oapi.service.application.v6.model.GetApplicationResp;
import com.lark.oapi.service.auth.v3.model.InternalTenantAccessTokenReq;
import com.lark.oapi.service.auth.v3.model.InternalTenantAccessTokenReqBody;
import com.lark.oapi.service.auth.v3.model.InternalTenantAccessTokenResp;
import com.lark.oapi.service.im.v1.model.CreateMessageReactionReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReactionReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.lark.oapi.service.im.v1.model.Emoji;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 飞书API客户端
 * 基于飞书官方SDK封装API调用
 *
 * @author system
 * @since 2025-01-09
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class FeishuApiClient {
  private static final Logger logger = LoggerFactory.getLogger(FeishuApiClient.class);
  private final Client client;
  private final String appId;
  private final String appSecret;

  public FeishuApiClient(String appId, String appSecret) {
    this.appId = appId;
    this.appSecret = appSecret;
    this.client = Client.newBuilder(appId, appSecret).requestTimeout(3, TimeUnit.SECONDS).build();
  }

  /**
   * 预校验凭证，鉴权失败时抛出异常
   */
  public void validateCredentials() {
    try {
      // 构建请求体
      InternalTenantAccessTokenReq req = InternalTenantAccessTokenReq.newBuilder()
        .internalTenantAccessTokenReqBody(InternalTenantAccessTokenReqBody.newBuilder()
          .appId(appId)
          .appSecret(appSecret)
          .build())
        .build();
      // 发送请求
      InternalTenantAccessTokenResp resp = client.auth().v3().tenantAccessToken().internal(req);
      if (!resp.success()) {
        throw new BssException(
          "飞书鉴权失败: code=" + resp.getCode() + ", msg=" + resp.getMsg());
      }
    }
    catch (Exception e) {
      throw new BssException("飞书鉴权失败: msg=" + e.getMessage(), e);
    }

  }

  public Map<String, String> getApplicationInfo(String appId) {
    GetApplicationResp resp;
    try {
      GetApplicationReq req = GetApplicationReq.newBuilder()
        .appId(appId)
        .lang("zh_cn")
        .userIdType("open_id")
        .build();
      // 发起请求
      resp = client.application().v6().application().get(req);
      if (resp == null || resp.getData() == null) {
        return Map.of();
      }
      return Map.of("name", resp.getData().getApp().getAppName(),
        "avatarUrl", resp.getData().getApp().getAvatarUrl());
    }
    catch (Exception e) {
      //忽略异常不处理
      logger.error("获取飞书应用名称失败", e);
    }
    return Map.of();
  }

  /**
   * 发送文本消息
   */
  public boolean sendTextMessage(String chatId, TextContent content) {
    try {
      CreateMessageReq req = CreateMessageReq.newBuilder()
        .receiveIdType("chat_id")
        .createMessageReqBody(CreateMessageReqBody.newBuilder()
          .receiveId(chatId)
          .msgType("text")
          .content(JsonUtil.toJsonString(content))
          .build())
        .build();
      CreateMessageResp resp = client.im().message().create(req);
      if (!resp.success()) {
        logger.error("发送飞书文本消息失败: code={}, msg={}, logId={}",
          resp.getCode(), resp.getMsg(), resp.getRequestId());
        return false;
      }
      return true;
    }
    catch (Exception e) {
      logger.error("发送飞书文本消息异常", e);
      return false;
    }
  }

  /**
   * 发送富文本消息
   */
  public boolean sendRichTextMessage(String chatId, String content) {
    try {
      CreateMessageReq req = CreateMessageReq.newBuilder()
        .receiveIdType("chat_id")
        .createMessageReqBody(CreateMessageReqBody.newBuilder()
          .receiveId(chatId)
          .msgType("post")
          .content(content)
          .build())
        .build();
      CreateMessageResp resp = client.im().message().create(req);
      if (!resp.success()) {
        logger.error("发送飞书富文本消息失败: code={}, msg={}, logId={}",
          resp.getCode(), resp.getMsg(), resp.getRequestId());
        return false;
      }
      logger.info("发送飞书富文本消息成功: messageId={}", resp.getData().getMessageId());
      return true;
    }
    catch (Exception e) {
      logger.error("发送飞书富文本消息异常", e);
      return false;
    }
  }

  /**
   * 发送卡片消息
   */
  public boolean sendCardMessage(String chatId, String cardContent) {
    try {
      CreateMessageReq req = CreateMessageReq.newBuilder()
        .receiveIdType("chat_id")
        .createMessageReqBody(CreateMessageReqBody.newBuilder()
          .receiveId(chatId)
          .msgType("interactive")
          .content(cardContent)
          .build())
        .build();
      CreateMessageResp resp = client.im().message().create(req);
      if (!resp.success()) {
        logger.error("发送飞书卡片消息失败: code={}, msg={}, logId={}",
          resp.getCode(), resp.getMsg(), resp.getRequestId());
        return false;
      }
      logger.info("发送飞书卡片消息成功: messageId={}", resp.getData().getMessageId());
      return true;
    }
    catch (Exception e) {
      logger.error("发送飞书卡片消息异常", e);
      return false;
    }
  }

  /**
   * 发送图片消息
   */
  public boolean sendImageMessage(String chatId, ImageContent imageContent) {
    try {
      CreateMessageReq req = CreateMessageReq.newBuilder()
        .receiveIdType("chat_id")
        .createMessageReqBody(CreateMessageReqBody.newBuilder()
          .receiveId(chatId)
          .msgType("image")
          .content(JsonUtil.toJsonString(imageContent))
          .build())
        .build();
      CreateMessageResp resp = client.im().message().create(req);
      if (!resp.success()) {
        logger.error("发送飞书图片消息失败: code={}, msg={}, logId={}",
          resp.getCode(), resp.getMsg(), resp.getRequestId());
        return false;
      }
      logger.info("发送飞书图片消息成功: messageId={}", resp.getData().getMessageId());
      return true;
    }
    catch (Exception e) {
      logger.error("发送飞书图片消息异常", e);
      return false;
    }
  }

  /** 飞书 Typing 表情类型，用于收到消息时立即反馈 */
  public static final String EMOJI_TYPE_TYPING = "Typing";

  /**
   * 给消息添加表情回复（如 Typing），用于收到消息时立即反馈
   *
   * @param messageId 消息 ID
   * @param emojiType 表情类型，如 {@link #EMOJI_TYPE_TYPING}
   * @return 是否成功
   */
  public boolean addReactionToMessage(String messageId, @Nullable String emojiType) {
    try {
      var req = CreateMessageReactionReq.newBuilder()
        .messageId(messageId)
        .createMessageReactionReqBody(
          CreateMessageReactionReqBody.newBuilder()
            .reactionType(
              Emoji.newBuilder()
                .emojiType(emojiType != null ? emojiType : EMOJI_TYPE_TYPING)
                .build())
            .build())
        .build();
      var resp = client.im().messageReaction().create(req);
      return resp.success();
    }
    catch (Exception e) {
      return false;
    }
  }

}
