package com.iwhalecloud.bote.controller.portal;

import com.iwhalecloud.bassc.basiccenter.annotation.IgnoreSession;
import com.iwhalecloud.bote.cache.SessionTicketMappingCache;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.util.ExternalPortalUtil;
import com.iwhalecloud.bote.dto.portal.ExternalSystemSession;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.CacheFactory;
import com.iwhalecloud.bss.litchi.cache.inf.ICacheClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * CAS门户登出
 *
 * <p> 当用户从CAS服务器登出时，CAS会向所有已登录的子应用发送登出请求 </p>
 * <p> 登出处理逻辑：1、接收CAS服务器的登出通知 2、解析请求中的票据(ticket) 3、使本地会话失效 </p>
 *
 * @author lxs
 * @since 2025-08-03
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cas/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "CAS门户登出")
public class CasLogoutController {

  private static final Logger logger = LoggerFactory.getLogger(CasLogoutController.class);

  private final ICacheClient cacheClient;
  private final SessionTicketMappingCache sessionTicketMappingCache;

  public CasLogoutController(CacheFactory cacheFactory, SessionTicketMappingCache sessionTicketMappingCache) {
    this.cacheClient = cacheFactory.getCacheClient(CacheConsts.GROUP_PLATFORM, CacheConsts.KEY_PREFIX_LOGIN);
    this.sessionTicketMappingCache = sessionTicketMappingCache;
  }

  /**
   * CAS登出回调接口
   *
   * @param logoutRequest CAS登出请求XML或直接传递ticket参数
   * @return 处理结果
   */
  @IgnoreSession
  @IgnoreSign
  @PostMapping("/logout")
  public ResultVO<String> handleCasLogout(
    @RequestParam(value = "logoutRequest", required = false) String logoutRequest,
    @RequestParam(value = "ticket", required = false) String ticket) {

    try {
      // 解析ticket - CAS可能以两种方式发送登出请求
      String sessionTicket = parseTicketFromLogoutRequest(logoutRequest, ticket);

      if (StringUtils.isEmpty(sessionTicket)) {
        throw new IllegalArgumentException("无法从登出请求中解析出ticket");
      }

      // 使本地会话失效
      invalidateLocalSession(sessionTicket);

    } catch (Exception e) {
      // 清理异常消息中的CRLF字符
      String safeErrorMessage = e.getMessage() != null ? e.getMessage().replaceAll("[\r\n]", "") : "未知错误";
      logger.error("CAS登出处理失败: {}", safeErrorMessage);
    }
    return ResultVO.success();
  }

  /**
   * 从CAS登出请求中解析ticket
   *
   * @param logoutRequest CAS登出请求XML
   * @param ticket 直接传递的ticket参数
   * @return 解析出的ticket
   */
  @SuppressWarnings("HttpUrlsUsage")
  private String parseTicketFromLogoutRequest(String logoutRequest, String ticket) throws ParserConfigurationException, IOException, SAXException {
    // CAS可能直接传递ticket参数，或者通过logoutRequest XML传递
    if (ticket != null && !ticket.isEmpty()) {
      return ticket;
    }

    // 解析 XML - 防止XXE攻击
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // 禁用外部实体解析，防止XXE攻击
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(new InputSource(new StringReader(logoutRequest)));

    // 提取 <samlp:SessionIndex> 的值
    NodeList sessionIndexNodes = document.getElementsByTagName("samlp:SessionIndex");
    if (sessionIndexNodes.getLength() > 0) {
      return sessionIndexNodes.item(0).getTextContent();
    }

    // 如果 CAS 使用 <SessionIndex>
    sessionIndexNodes = document.getElementsByTagName("SessionIndex");
    if (sessionIndexNodes.getLength() > 0) {
      return sessionIndexNodes.item(0).getTextContent();
    }

    return null;
  }

  /**
   * 使本地会话失效
   * @param ticket 会话票据
   */
  private void invalidateLocalSession(String ticket) {
    // 根据票据获取本地会话ID
    ExternalSystemSession systemSession = sessionTicketMappingCache.getMapping(BaseConsts.PORTAL_TYPE_CAS + ticket);
    if (systemSession == null) {
      return;
    }
    String sessionId = systemSession.getSessionId();
    if (sessionId != null) {
      // 清除 CAS门户系统缓存
      String value = cacheClient.opsForValue().get(sessionId);
      ExternalPortalUtil.clearCasSystemCache(value);
      // 使会话失效
      cacheClient.delete(sessionId);
    }

    logger.info("失效本地会话成功，sessionId = {}", sessionId);
  }

}
