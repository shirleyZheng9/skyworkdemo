package com.iwhalecloud.bote.common.util;

import com.iwhalecloud.bote.cache.SessionTicketMappingCache;
import com.iwhalecloud.bote.cache.TenantSettingInfoCache;
import com.iwhalecloud.bote.cache.ZhxyTokenCache;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.KnowledgeConsts;
import com.iwhalecloud.bote.dto.knowledge.KnowledgeInfoDTO;
import com.iwhalecloud.bote.dto.portal.ExternalPortalDTO;
import com.iwhalecloud.bote.dto.portal.ExternalSystemSession;
import com.iwhalecloud.bote.dto.portal.LoginInfo;
import com.iwhalecloud.bote.mapper.portal.ExternalPortalMapper;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import com.iwhalecloud.bss.litchi.util.SpringUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

/**
 * 门户对接工具类
 *
 * @author lxs
 * @since 2025-08-02
 */
public final class ExternalPortalUtil {

  private static final Logger logger = LoggerFactory.getLogger(ExternalPortalUtil.class);

  private ExternalPortalUtil() {
  }
  private static final ExternalPortalMapper EXTERNAL_PORTAL_MAPPER = SpringUtil.getBean(ExternalPortalMapper.class);
  private static final SessionTicketMappingCache MAPPING_CACHE = SpringUtil.getBean(SessionTicketMappingCache.class);
  private static final ZhxyTokenCache TOKEN_CACHE = SpringUtil.getBean(ZhxyTokenCache.class);
  private static final TenantSettingInfoCache SETTING_INFO_CACHE = SpringUtil.getBean(TenantSettingInfoCache.class);

  /**
   * 根据 sessionId获取 CAS门户重定向地址
   *
   * @param sessionId 本地会话
   * @param redirectType 重定向类型
   *
   * @return 重定向地址
   */
  @Nullable
  public static String getCasRedirectUrl(@Nullable String sessionId, String redirectType) {
    if (StringUtils.isEmpty(sessionId)) {
      return null;
    }
    ExternalSystemSession systemSession = MAPPING_CACHE.getMapping(sessionId);
    if (systemSession == null) {
      return null;
    }
    String ticket = systemSession.getSessionId();
    // 如果不是对接 CAS门户，返回
    if (StringUtils.isEmpty(ticket) || !ticket.startsWith(BaseConsts.PORTAL_TYPE_CAS)) {
      return null;
    }
    String redirectUri = "";
    try {
      // 查询外系统对应的 CAS门户配置
      List<ExternalPortalDTO> externalPortalDTOS = EXTERNAL_PORTAL_MAPPER.selectPortalByPortalType(BaseConsts.PORTAL_TYPE_CAS, systemSession.getSystemCode());
      if (CollectionUtils.isNotEmpty(externalPortalDTOS)) {
        ExternalPortalDTO externalPortalDTO = externalPortalDTOS.get(0);
        // 获取 CAS地址与博特门户地址
        String casServiceUrl = externalPortalDTO.getLoginUrl();
        String webUrl = externalPortalDTO.getLoggedUrl();
        redirectUri = casServiceUrl + "/" + redirectType + "?service=" + URLEncoder.encode(webUrl, StandardCharsets.UTF_8);
        logger.info("门户登出重定向地址：{}", redirectUri);
      }
    }
    finally {
      MAPPING_CACHE.deleteMapping(ticket);
      MAPPING_CACHE.deleteMapping(sessionId);
    }
    return redirectUri;
  }

  /**
   * 清除 CAS门户系统缓存
   *
   * @param value 登录信息
   */
  public static void clearCasSystemCache(@Nullable String value) {
    if (StringUtils.isEmpty(value)) {
      return;
    }
    // 解析登录信息
    LoginInfo loginDTO = JsonUtil.parseJsonRequired(value, LoginInfo.class);
    if (StringUtils.isNotEmpty(loginDTO.getExtUserId())) {
      // 获取知识中台用户ID并删除缓存
      clearZhxyToken(loginDTO.getExtUserId(), loginDTO.getDefaultTenantId());
    }
  }

  /**
   * 清除知识中台用户令牌
   *
   * @param extUserId 知识中台用户 ID
   * @param tenantId 租户 ID
   */
  public static void clearZhxyToken(String extUserId, Long tenantId) {
    if (tenantId == null) {
      return;
    }
    // 是否知识中台
    KnowledgeInfoDTO knowledgeInfo = SETTING_INFO_CACHE.getKnowledgeInfo(tenantId);
    if (KnowledgeConsts.KNOWLEDGE_TYPE_PLATFORM.equals(knowledgeInfo.getKnowledgeType())) {
      TOKEN_CACHE.remove(extUserId);
      logger.info("清除知识中台用户令牌成功: extUserId = {}", extUserId);
    }
  }
}
