package com.iwhalecloud.bote.wechat;

import com.iwhalecloud.bote.common.util.ExpUtil;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信参数与报文处理工具类 整合了 SHA1 签名、PKCS7 编解码、加密报文解析与解密等能力 验签规则： 明文：signature = sha1(sorted(token, timestamp, nonce))
 * 安全：msg_signature = sha1(sorted(token, timestamp, nonce, Encrypt))
 *
 * @author lizuyin
 * @since 2025/08/14
 */
@Component
public final class WechatParamHelper {

  /**
   * 默认字符集
   */
  private static final Charset CHARSET = StandardCharsets.UTF_8;

  /**
   * 用 SHA1 算法生成安全签名。
   *
   * @param token 票据
   * @param timestamp 时间戳
   * @param nonce 随机字符串
   * @param encrypt 加密串（明文场景可为 null）
   * @return 安全签名（十六进制小写）
   * @throws BssException 计算失败
   */
  @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_SHA1")
  public String getSHA1(String token, String timestamp, String nonce, String encrypt) throws BssException {
    try {
      String[] array = new String[] {
        StringUtils.defaultString(token), StringUtils.defaultString(timestamp),
        StringUtils.defaultString(nonce), StringUtils.defaultString(encrypt)
      };
      Arrays.sort(array);
      StringBuilder sb = new StringBuilder();
      for (String s : array) {
        sb.append(s);
      }
      return DigestUtils.sha1Hex(sb.toString().getBytes(CHARSET));
    }
    catch (Exception e) {
      throw new BssException("计算SHA1签名失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 检验消息真实性并获取解密后的明文字段。 适用于“安全模式”：msg_signature = sha1(sorted(token, timestamp, nonce, Encrypt))。
   *
   * @param token 开发者 Token
   * @param encodingAesKey EncodingAESKey（43位）
   * @param appId AppId
   * @param msgSignature URL 参数 msg_signature
   * @param timeStamp URL 参数 timestamp
   * @param nonce URL 参数 nonce
   * @param postData POST 的 XML 密文体
   * @return 明文字段 Map（包含 ToUserName、FromUserName、CreateTime、MsgType、Content、MsgId 若存在）
   * @throws BssException 解密或解析失败
   */
  public Map<String, String> decryptMsg(String token, String encodingAesKey, String appId, String msgSignature,
    String timeStamp, String nonce, String postData) throws BssException {
    if (StringUtils.length(encodingAesKey) != 43) {
      throw new BssException("微信EncodingAESKey长度非法，期望43，实际: " + StringUtils.length(encodingAesKey));
    }
    Object[] extract = extractEncrypt(postData);
    String encrypt = String.valueOf(extract[1]);
    String calc = getSHA1(token, timeStamp, nonce, encrypt);
    if (!calc.equals(msgSignature)) {
      throw new BssException("签名校验失败，msgSignature与计算结果不一致");
    }
    byte[] aesKey = Base64.decodeBase64(encodingAesKey + "=");
    String plainXml = decryptInternal(encrypt, aesKey, appId);
    return parsePlainFields(plainXml);
  }

  /**
   * 创建安全的 DocumentBuilderFactory 对象
   *
   * @return DocumentBuilderFactory 对象
   */
  @SuppressWarnings("HttpUrlsUsage")
  public DocumentBuilderFactory buildDocumentBuilderFactory() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      // 禁用外部实体解析，防止XXE攻击
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      dbf.setXIncludeAware(false);
      dbf.setExpandEntityReferences(false);
      return dbf;
    }
    catch (ParserConfigurationException e) {
      throw new BssException("创建DocumentBuilderFactory失败!" + e.getMessage(), e);
    }
  }

  /**
   * 执行 AES/CBC/NoPadding 解密，并做 PKCS7 去填充及 AppId 校验。
   *
   * @param cipherTextBase64 Base64 编码的密文
   * @param aesKey 二进制 AES 密钥（由 EncodingAESKey 解码而得）
   * @param appId 期望的 AppId（用于校验）
   * @return 明文 XML 字符串
   * @throws BssException 解密失败或明文结构非法
   */
  @SuppressFBWarnings("CIPHER_INTEGRITY")
  public String decryptInternal(String cipherTextBase64, byte[] aesKey, String appId) throws BssException {
    byte[] original;
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
      SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
      IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(aesKey, 0, 16));
      cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
      byte[] encrypted = Base64.decodeBase64(cipherTextBase64);
      original = cipher.doFinal(encrypted);
    }
    catch (Exception e) {
      throw new BssException("微信消息解密失败: " + ExpUtil.getMsg(e), e);
    }

    try {
      byte[] bytes = pkcs7Decode(original);
      int xmlLength = recoverNetworkBytesOrder(Arrays.copyOfRange(bytes, 16, 20));
      String xmlContent = new String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET);
      String fromAppId = new String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length), CHARSET);
      if (!fromAppId.equals(appId)) {
        throw new BssException("AppId校验失败，解密得到的fromAppId与配置的appId不一致");
      }
      return xmlContent;
    }
    catch (Exception e) {
      throw new BssException("微信消息解密后内容非法: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 还原 4 字节网络字节序为整数。
   */
  private int recoverNetworkBytesOrder(byte[] orderBytes) {
    int sourceNumber = 0;
    for (int i = 0; i < 4; i++) {
      sourceNumber <<= 8;
      sourceNumber |= orderBytes[i] & 0xff;
    }
    return sourceNumber;
  }

  /**
   * 从加密 XML 中提取 Encrypt 与 ToUserName 字段。 FB误报，使用注解镇压XXE_DOCUMENT报错
   */
  @SuppressFBWarnings("XXE_DOCUMENT")
  private Object[] extractEncrypt(String xmltext) throws BssException {
    Object[] result = new Object[3];
    try {
      DocumentBuilderFactory dbf = buildDocumentBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      try (StringReader sr = new StringReader(xmltext)) {
        InputSource is = new InputSource(sr);
        Document document = db.parse(is);
        Element root = document.getDocumentElement();
        NodeList encryptNodeList = root.getElementsByTagName("Encrypt");
        NodeList toUserNameNodeList = root.getElementsByTagName("ToUserName");
        if (encryptNodeList.getLength() == 0 || toUserNameNodeList.getLength() == 0) {
          throw new BssException("解析微信XML失败: 缺少必要字段 Encrypt 或 ToUserName");
        }
        result[0] = 0;
        result[1] = encryptNodeList.item(0).getTextContent();
        result[2] = toUserNameNodeList.item(0).getTextContent();
        return result;
      }
    }
    catch (Exception e) {
      throw new BssException("解析微信XML失败: " + ExpUtil.getMsg(e), e);
    }
  }

  /**
   * 解析明文 XML 的常见字段到 Map。 FB误报，使用注解镇压XXE_DOCUMENT报错
   */
  @SuppressFBWarnings("XXE_DOCUMENT")
  private Map<String, String> parsePlainFields(String plainXml) throws BssException {
    try {
      DocumentBuilderFactory dbf = buildDocumentBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      try (StringReader sr = new StringReader(plainXml)) {
        InputSource is = new InputSource(sr);
        Document document = db.parse(is);
        Element root = document.getDocumentElement();
        Map<String, String> messageMap = new HashMap<>();
        String[] fields = {
          "ToUserName", "FromUserName", "CreateTime", "MsgType", "Content", "MsgId", "AgentID"
        };
        for (String field : fields) {
          NodeList nodeList = root.getElementsByTagName(field);
          if (nodeList.getLength() > 0) {
            messageMap.put(field, nodeList.item(0).getTextContent());
          }
        }
        return messageMap;
      }
    }
    catch (Exception e) {
      throw new BssException("解析失败，接收微信消息非法!" + e.getMessage(), e);
    }
  }

  /**
   * PKCS7 去填充处理。
   */
  private byte[] pkcs7Decode(byte[] decrypted) {
    int pad = decrypted[decrypted.length - 1] & 0xFF;
    if (pad < 1 || pad > 32) {
      pad = 0;
    }
    return Arrays.copyOfRange(decrypted, 0, decrypted.length - pad);
  }

}


