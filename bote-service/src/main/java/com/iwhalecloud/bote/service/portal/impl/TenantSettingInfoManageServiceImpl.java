package com.iwhalecloud.bote.service.portal.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.WeKnoraLoginHelper;
import com.iwhalecloud.bote.doc.module.knowledge.dto.weknora.req.WeKnoraRegisterRequest;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.DocChainLoginHelper;
import com.iwhalecloud.bote.doc.module.knowledge.service.helper.KnowledgeGraphClientHelper;
import com.iwhalecloud.bote.dto.base.FileInfoDTO;
import com.iwhalecloud.bote.dto.chat.ChatReplyThemeDTO;
import com.iwhalecloud.bote.dto.chat.ChatThemeDTO;
import com.iwhalecloud.bote.dto.portal.AccountDTO;
import com.iwhalecloud.bote.dto.portal.KnowledgeGraphAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.WeKnoraAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.dto.tenant.setting.TenantPluginHubSettingDTO;
import com.iwhalecloud.bote.mapper.base.FileInfoManageMapper;
import com.iwhalecloud.bote.mapper.chat.ChatReplyThemeManageMapper;
import com.iwhalecloud.bote.mapper.chat.ChatThemeMapper;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.exception.BssException;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 租户设置信息管理服务实现
 *
 * @author auto
 * @since 2024-12-18
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GuardLogStatement")
public class TenantSettingInfoManageServiceImpl implements ITenantSettingInfoManageService {
  private static final Logger logger = LoggerFactory.getLogger(TenantSettingInfoManageServiceImpl.class);

  private final TenantSettingInfoManageMapper mapper;
  private final DocChainLoginHelper docChainLoginHelper;
  private final ObjectProvider<WeKnoraLoginHelper> weKnoraLoginHelper;
  private final ObjectProvider<KnowledgeGraphClientHelper> knowledgeGraphClientHelper;
  private final FileInfoManageMapper fileInfoManager;
  private final ChatThemeMapper chatThemeMapper;
  private final IResourceElementService resourceElementService;
  private final ChatReplyThemeManageMapper chatReplyThemeMapper;
  private final IRefreshCacheService refreshCacheService;

  /** 对话窗口扩展类型 */
  private static final Set<String> PIN_SETTING_TYPES = Set.of(
    BaseConsts.FUNC_TYPE_PIN_SETTING,
    BaseConsts.FUNC_TYPE_PIN_SETTING_BOTTOM
  );

  /** 判断是否为对话窗口扩展类型 */
  private static boolean isPinSettingTypes(String funcType) {
    return PIN_SETTING_TYPES.contains(funcType);
  }

  /** 判断对话窗口扩展类型是否启用 */
  private static boolean isPinSettingTypeEnabled(String info) {
    // JSON 字符串转 Map
    Map<String, Object> infoMap = JsonUtil.parseJson(info, new TypeReference<>() {
    });
    // 判断是否启用
    return Boolean.TRUE.equals(MapUtils.getBoolean(infoMap, "enablePin"));
  }

  @Override
  public Map<String, String> findSettingInfo(Long tenantId, @Nullable Long botId) {
    List<TenantSettingInfoDTO> list = mapper.selectTenantSettingInfoList(tenantId);
    Map<String, String> map = new HashMap<>();
    for (TenantSettingInfoDTO dto : CollectionUtils.emptyIfNull(list)) {
      // 只收集租户级的设置
      if (dto.getBotId() != null) {
        continue;
      }
      map.put(dto.getFuncType(), dto.getSettingInfo());
    }
    if (botId != null) {
      // 优先使用机器人级的设置
      List<TenantSettingInfoDTO> botSettingInfos = CollectionUtils.emptyIfNull(list)
        .stream().filter(p -> Objects.equals(botId, p.getBotId()))
        .filter(p -> StringUtils.isNotEmpty(p.getSettingInfo()))
        .filter(p -> !isPinSettingTypes(p.getFuncType()) || isPinSettingTypeEnabled(p.getSettingInfo()))
        .collect(Collectors.toList());
      for (TenantSettingInfoDTO dto : CollectionUtils.emptyIfNull(botSettingInfos)) {
        map.put(dto.getFuncType(), dto.getSettingInfo());
      }
      // 查找当前bot使用的chat_theme
      ChatThemeDTO chatThemeDTO = chatThemeMapper.getBotUsedTheme(botId, tenantId);
      if (chatThemeDTO != null) {
        // 如果使用的主题是平台主题则替换为平台主题数据
        String platformThemeJson = chatThemeMapper.getPlatformThemeJson(chatThemeDTO.getThemeId());
        if (StringUtils.isNotEmpty(platformThemeJson)) {
          chatThemeDTO.setThemeJson(platformThemeJson);
        }
        map.put(BaseConsts.FUNC_TYPE_CHAT_THEME, chatThemeDTO.getThemeJson());
        // 回复消息主题
        if (chatThemeDTO.getReplyThemeId() != null) {
          ChatReplyThemeDTO chatReplyTheme = chatReplyThemeMapper.getChatReplyTheme(chatThemeDTO.getReplyThemeId(), tenantId);
          if (chatReplyTheme != null) {
            map.put(BaseConsts.FUNC_TYPE_REPLY_CHAT_THEME, chatReplyTheme.getThemeJson());
          }
        }
      }
    }
    return map;
  }

  @Nullable
  @Override
  @SuppressWarnings("unchecked")
  public TenantSettingInfoDTO findTenantSettingInfo(Long tenantId, String funcType) {
    TenantSettingInfoDTO tenantSettingInfo = mapper.getTenantSettingInfoByTenantIdAndType(tenantId, funcType);
    if (tenantSettingInfo == null) {
      return null;
    }
    // 插件类型需要根据fileInfoId获取fileId,传递前端下载文件
    if (BaseConsts.FUNC_TYPE_CHAT_PLUGINS.equals(funcType)) {
      List<Map<String, Object>> dataList = JsonUtil.parseJson(tenantSettingInfo.getSettingInfo(), new TypeReference<List<Map<String, Object>>>() {
      });
      for (Map<String, Object> map : CollectionUtils.emptyIfNull(dataList)) {
        if ("file".equals(MapUtils.getString(map, "pluginType"))) {
          Map<String, Object> pluginContent = (Map<String, Object>) MapUtils.getMap(map, "pluginContent");
          if (pluginContent != null && pluginContent.get("fileInfoId") != null) {
            FileInfoDTO fileInfo = fileInfoManager.getFileInfo(tenantId, MapUtils.getLong(pluginContent, "fileInfoId"));
            if (fileInfo == null) {
              throw new BssException("文件不存在, fileInfoId=" + MapUtils.getLong(pluginContent, "fileInfoId"));
            }
            pluginContent.replace("fileId", String.valueOf(fileInfo.getFileId()));
          }
        }
      }
      if (CollectionUtils.isNotEmpty(dataList)) {
        tenantSettingInfo.setSettingInfo(JsonUtil.toJsonString(dataList));
      }
    }
    return tenantSettingInfo;
  }

  @Override
  @Transactional
  public ResultVO<TenantSettingInfoDTO> saveTenantSettingInfo(TenantSettingInfoDTO tenantSettingInfo) {
    tenantSettingInfo.setStatusCd(BaseConsts.STATUS_CD_VALID);
    TenantSettingInfoDTO old = tenantSettingInfo.getSettingId() == null ? null : mapper.getTenantSettingInfo(tenantSettingInfo.getTenantId(), tenantSettingInfo.getSettingId());
    DataDifference<TenantSettingInfoDTO> difference = DataDifferenceStarter.computeSave(old, tenantSettingInfo, false, tenantSettingInfo.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    if (old == null && tenantSettingInfo.getBotId() != null) {
      // 新增智能应用的设置时，需要记录血缘关系
      resourceElementService.batchAdd(tenantSettingInfo.getTenantId(), tenantSettingInfo.getBotId(), DataSyncCodeEnum.BOT.getCode(),
        Collections.singletonList(tenantSettingInfo.getSettingId()), DataSyncCodeEnum.TENANT_SETTING_INFO.getCode());
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  public List<TenantSettingInfoDTO> queryTenantSettingInfoList(Long tenantId) {
    return mapper.selectTenantSettingInfoList(tenantId);
  }

  @Override
  public ResultVO<Map<String, TenantSettingInfoDTO>> batchGetTenantSettingInfo(TenantQueryParams queryParams) {
    List<TenantSettingInfoDTO> tenantSettingInfoList = mapper.selectTenantSettingInfoListByFuncTypes(queryParams);
    Map<String, TenantSettingInfoDTO> map = new HashMap<>();
    if (CollectionUtils.isNotEmpty(tenantSettingInfoList)) {
      map.putAll(tenantSettingInfoList.stream().collect(Collectors.toMap(TenantSettingInfoDTO::getFuncType, p -> p)));
    }
    if (queryParams.getBotId() != null) {
      if (MapUtils.isEmpty(map) || !map.containsKey(BaseConsts.FUNC_TYPE_FUNC_SWITCH)) {
        // 应用级的功能开关，前端展示有些特殊，没有定义时，需要同步租户级定义
        TenantSettingInfoDTO info = mapper.getTenantSettingInfoByTenantIdAndType(queryParams.getTenantId(), BaseConsts.FUNC_TYPE_FUNC_SWITCH);
        if (info != null) {
          info.setSettingId(Sequences.TENANT_SETTING_INFO_SETTING_ID.next());
          info.setBotId(queryParams.getBotId());
          map.put(BaseConsts.FUNC_TYPE_FUNC_SWITCH, info);
        }
      }
    }
    return ResultVO.success(map);
  }

  @Override
  public ResultVO<Void> testDocChainAccount(AccountDTO dto) {
    String password = AesUtil.aesDecrypt(dto.getToken(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(password)) {
      return ResultVO.fail("密码解析异常");
    }
    docChainLoginHelper.doLogin(dto.getTenantId(), dto.getUsername(), Base64.encodeBase64String(password.getBytes(StandardCharsets.UTF_8)));
    if (StringUtils.isNotEmpty(dto.getApiKey())) {
      boolean exists = docChainLoginHelper.existsApiKey(dto.getTenantId(), dto.getApiKey());
      if (!exists) {
        return ResultVO.fail("无效 API Key");
      }
    }
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<String> syncDocChainAccount(AccountDTO dto) {
    String decryptPassword = AesUtil.aesDecrypt(dto.getToken(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(decryptPassword)) {
      return ResultVO.fail("密码解析异常");
    }
    // 同步账号
    try {
      docChainLoginHelper.register(dto.getTenantId(), dto.getUsername(), Base64.encodeBase64String(decryptPassword.getBytes(StandardCharsets.UTF_8)));
    }
    catch (BssException e) {
      logger.error("同步DocChain账号失败: message = {}", e.getMessage(), e);
      if (!e.getMessage().contains("User " + dto.getUsername() + " already exists")) {
        throw new BssException("同步DocChain账号失败: " + e.getMessage(), e);
      }
    }
    // 同步 API Key
    String apiKey = dto.getApiKey();
    if (StringUtils.isNotEmpty(apiKey)) {
      boolean exists = docChainLoginHelper.existsApiKey(dto.getTenantId(), apiKey);
      if (!exists) {
        apiKey = docChainLoginHelper.generateApiKey(CommonConsts.COPILOT_TENANT_ID, dto.getUsername());
      }
    }
    else {
      apiKey = docChainLoginHelper.generateApiKey(CommonConsts.COPILOT_TENANT_ID, dto.getUsername());
    }
    // 信息入库
    Map<String, Object> info = new HashMap<>();
    info.put("userName", dto.getUsername());
    info.put("token", dto.getToken());
    info.put("apiKey", apiKey);
    info.put("enabled", true);
    TenantSettingInfoDTO old = findTenantSettingInfo(dto.getTenantId(), BaseConsts.FUNC_TYPE_KNOWLEDGE);
    if (old != null) {
      old.setSettingInfo(JsonUtil.toJsonString(info));
    }
    else {
      old = new TenantSettingInfoDTO();
      old.setTenantId(dto.getTenantId());
      old.setFuncType(BaseConsts.FUNC_TYPE_KNOWLEDGE);
      old.setSettingInfo(JsonUtil.toJsonString(info));
    }
    saveTenantSettingInfo(old);
    return ResultVO.success("success");
  }

  @Override
  @Transactional
  public void saveFuncSwitchInfo(Long tenantId, Long botId, String funcSwitch) {
    // 如果存在 botId 对应的 func_switch 信息则更新 func_switch 信息
    if (mapper.existsFuncSwitchSettingInfo(tenantId, botId)) {
      mapper.updateFuncSwitchInfo(tenantId, botId, funcSwitch);
    }
    else {
      // 不存在则新建一条新的 func_switch 类型配置数据
      TenantSettingInfoDTO settingInfo = new TenantSettingInfoDTO();
      settingInfo.setTenantId(tenantId);
      settingInfo.setBotId(botId);
      settingInfo.setSettingInfo(funcSwitch);
      settingInfo.setFuncType(BaseConsts.FUNC_TYPE_FUNC_SWITCH);
      settingInfo.setStatusCd(BaseConsts.STATUS_CD_VALID);
      DataDifferenceStarter.computeSave(null, settingInfo, false, tenantId);

      // 新增智能应用的设置时，需要记录血缘关系
      resourceElementService.batchAdd(tenantId, botId, DataSyncCodeEnum.BOT.getCode(), Collections.singletonList(settingInfo.getSettingId()),
        DataSyncCodeEnum.TENANT_SETTING_INFO.getCode());
    }
  }

  @Override
  @Transactional
  public void savePluginHub(Long tenantId, String apiKey) {
    TenantSettingInfoDTO dto = findTenantSettingInfo(tenantId, CommonConsts.FUNC_TYPE_PLUGIN_HUB);
    TenantPluginHubSettingDTO setting = new TenantPluginHubSettingDTO();
    setting.setPortalUserApiKey(apiKey);
    if (dto == null) {
      dto = new TenantSettingInfoDTO();
      dto.setTenantId(tenantId);
      dto.setFuncType(CommonConsts.FUNC_TYPE_PLUGIN_HUB);
      dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
    }
    dto.setSettingInfo(JsonUtil.toJsonString(setting));
    ResultVO<TenantSettingInfoDTO> result = saveTenantSettingInfo(dto);
    if (result.isSuccess()) {
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, tenantId.toString());
    }
  }

  @Override
  public ResultVO<Void> testWeknoraAccount(WeKnoraAccountSettingDTO account) {
    String password = AesUtil.aesDecrypt(account.getPassword(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(password)) {
      return ResultVO.fail("密码解析异常");
    }
    getWeKnoraLoginHelper().login(account.getTenantId(), account.getEmail(), password);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> registerWeknoraAccount(WeKnoraAccountSettingDTO account) {
    String decryptPassword = AesUtil.aesDecrypt(account.getPassword(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(decryptPassword)) {
      return ResultVO.fail("密码解析异常");
    }
    // 同步账号
    try {
      WeKnoraRegisterRequest request = new WeKnoraRegisterRequest();
      request.setPassword(decryptPassword);
      request.setEmail(account.getEmail());
      request.setUsername(account.getUserName());
      getWeKnoraLoginHelper().register(account.getTenantId(), request);
    }
    catch (BssException e) {
      logger.error("同步Weknora账号失败: message = {}", e.getMessage(), e);
      if (!e.getMessage().contains("user with this email already exists")) {
        throw new BssException("注册Weknora账号失败: " + e.getMessage(), e);
      }
      else {
        return ResultVO.fail("注册Weknora账号失败: 邮箱已经注册！");
      }
    }
    TenantSettingInfoDTO old = findTenantSettingInfo(account.getTenantId(), CommonConsts.FUNC_TYPE_WEKNORA);
    if (old != null) {
      old.setSettingInfo(JsonUtil.toJsonString(account));
    }
    else {
      old = new TenantSettingInfoDTO();
      old.setTenantId(account.getTenantId());
      old.setFuncType(CommonConsts.FUNC_TYPE_WEKNORA);
      old.setSettingInfo(JsonUtil.toJsonString(account));
    }
    saveTenantSettingInfo(old);
    return ResultVO.success();
  }

  @Override
  @Transactional
  public ResultVO<Void> updateWeknoraAccountPw(WeKnoraAccountSettingDTO account) {
    String decryptPassword = AesUtil.aesDecrypt(account.getPassword(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(decryptPassword)) {
      return ResultVO.fail("密码解析异常");
    }
    TenantSettingInfoDTO old = findTenantSettingInfo(account.getTenantId(), CommonConsts.FUNC_TYPE_WEKNORA);
    if (old == null) {
      throw new BssException("先注册再修改密码！");
    }
    WeKnoraAccountSettingDTO oldDTO = JsonUtil.parseJson(old.getSettingInfo(), WeKnoraAccountSettingDTO.class);
    Assert.notNull(oldDTO, "账号信息丢失");
    String oldPassword = AesUtil.aesDecrypt(oldDTO.getPassword(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    getWeKnoraLoginHelper().updatePassword(account.getTenantId(), oldPassword, decryptPassword);

    oldDTO.setPassword(account.getPassword());
    old.setSettingInfo(JsonUtil.toJsonString(oldDTO));
    saveTenantSettingInfo(old);
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> testKnowledgeGraphAccount(KnowledgeGraphAccountSettingDTO account) {
    getKnowledgeGraphClientHelper().testLogin(account);
    return ResultVO.success();
  }

  /**
   * 获取WeKnoraLoginHelper
   */
  private WeKnoraLoginHelper getWeKnoraLoginHelper() {
    WeKnoraLoginHelper loginHelper = weKnoraLoginHelper.getIfAvailable();
    if (loginHelper == null) {
      throw new BssException("未开启Weknora知识库，请联系管理员");
    }
    return loginHelper;
  }

  /**
   * 获取 KnowledgeGraphClientHelper
   */
  private KnowledgeGraphClientHelper getKnowledgeGraphClientHelper() {
    KnowledgeGraphClientHelper clientHelper = knowledgeGraphClientHelper.getIfAvailable();
    if (clientHelper == null) {
      throw new BssException("未开启knowledgeGraph知识库，请联系管理员");
    }
    return clientHelper;
  }
}
