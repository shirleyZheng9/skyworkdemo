package com.iwhalecloud.bote.service.base.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bassc.basiccenter.base.dto.SecurityRuleDto;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.consts.CommonConsts;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.Sequences;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.doc.module.knowledge.mapper.KnowledgeBaseManageMapper;
import com.iwhalecloud.bote.dto.base.DcCfgDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSafeDTO;
import com.iwhalecloud.bote.dto.base.DcCfgSaveRequest;
import com.iwhalecloud.bote.dto.base.DcCfgSimpleDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.mapper.base.DcCfgMapper;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bote.service.base.IDcCfgService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author auto
 * @since 2024-09-14
 */
@Service
@RequiredArgsConstructor
public class DcCfgServiceImpl implements IDcCfgService {

  private final DcCfgMapper dcCfgMapper;
  private final KnowledgeBaseManageMapper knowledgeBaseManageMapper;
  private final TenantSettingInfoManageMapper tenantSettingInfoManageMapper;
  private final IRefreshCacheService refreshCacheService;

  @Override
  public List<DcCfgDTO> querySwitchList() {
    return dcCfgMapper.selectAllSwitches(BaseConsts.DC_CFG_TYPE_1);
  }

  @Override
  public List<DcCfgDTO> queryParamList(String searchContent, String type) {
    List<DcCfgDTO> dcCfgList = dcCfgMapper.selectAllParams(type);
    List<String> allParamCodes = new ArrayList<>();
    for (DcCfgDTO dcCfg : CollectionUtils.emptyIfNull(dcCfgList)) {
      if (StringUtils.isNotEmpty(dcCfg.getRelParamCodes())) {
        dcCfg.setParamName(dcCfg.getParamGroupName());
        allParamCodes.addAll(Arrays.asList(StringUtils.split(dcCfg.getRelParamCodes(), ",")));
      }
    }

    Map<String, DcCfgSimpleDTO> paramValueMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(allParamCodes)) {
      List<DcCfgSimpleDTO> allChildren = dcCfgMapper.selectParamValueByParamCodes(allParamCodes);
      CollectionUtils.emptyIfNull(allChildren).forEach(child -> paramValueMap.put(child.getParamCode(), child));
    }

    for (DcCfgDTO dcCfg : CollectionUtils.emptyIfNull(dcCfgList)) {
      if (StringUtils.isNotEmpty(dcCfg.getRelParamCodes())) {
        String[] paramCodeArray = StringUtils.split(dcCfg.getRelParamCodes(), ",");
        if (ArrayUtils.isNotEmpty(paramCodeArray)) {
          List<DcCfgSimpleDTO> children = new ArrayList<>();
          for (String paramCode : paramCodeArray) {
            DcCfgSimpleDTO child = paramValueMap.get(paramCode);
            if (child != null) {
              children.add(child);
            }
          }
          dcCfg.setChildren(children);
        }
      }
    }
    return StringUtils.isEmpty(searchContent) ? dcCfgList : filterDcCfgList(dcCfgList, searchContent);
  }

  private List<DcCfgDTO> filterDcCfgList(List<DcCfgDTO> dcCfgList, String searchContent) {
    return dcCfgList.stream().filter(dcCfg -> matchesSearchContent(dcCfg, searchContent)).collect(Collectors.toList());
  }

  private boolean matchesSearchContent(DcCfgDTO dcCfg, String searchContent) {
    if (Strings.CI.contains(dcCfg.getParamName(), searchContent) || Strings.CI.contains(dcCfg.getRelParamCodes(), searchContent)
      || Strings.CI.contains(dcCfg.getParamCode(), searchContent)) {
      return true;
    }
    if (CollectionUtils.isEmpty(dcCfg.getChildren())) {
      return false;
    }
    return dcCfg.getChildren().stream().anyMatch(
      child -> Strings.CI.contains(child.getParamName(), searchContent) || Strings.CI.contains(child.getParamDesc(), searchContent)
        || Strings.CI.contains(child.getParamCode(), searchContent));
  }

  @Override
  @Transactional
  public ResultVO<Void> modParamData(DcCfgSaveRequest request) {
    // 方法不要加事务注解，使用默认的自动提交机制，以避免缓存刷新时事务还未提交
    String paramCode = request.getParamCode();
    Assert.hasLength(paramCode, "参数编码不能为空");
    int affectedRows = dcCfgMapper.updateParamValue(paramCode, request.getParamVal(), SessionUtil.getLoginInfo().getUserId());
    if (affectedRows < 1) {
      return BaseErrorConstant.DC_PARAM_NOT_FOUND_ERROR.toResult(paramCode);
    }
    doForDocChain(paramCode, request.getParamVal());
    doForDefaultModel(paramCode, request.getParamVal());
    return ResultVO.success();
  }

  @Override
  public List<DcCfgSafeDTO> getSafeParamList() {
    // 查询所有安全相关的配置参数
    List<DcCfgSafeDTO> cfgDTOList = dcCfgMapper.selectAllSafeParams();
    if (CollectionUtils.isEmpty(cfgDTOList)) {
      return Collections.emptyList();
    }
    // 按配置策略分组
    List<DcCfgSafeDTO> groupCfgList = cfgDTOList.stream().filter(dto -> StringUtils.isNotEmpty(dto.getRelParamCodes())).collect(Collectors.toList());
    for (DcCfgSafeDTO dcCfgDTO : CollectionUtils.emptyIfNull(groupCfgList)) {
      // 设置分组下的子配置项列表
      String[] splits = StringUtils.split(dcCfgDTO.getRelParamCodes(), ",");
      List<DcCfgSafeDTO> childrenList = cfgDTOList.stream().filter(dto -> Arrays.asList(splits).contains(dto.getParamCode()))
        .sorted(Comparator.comparing(DcCfgSafeDTO::getSort)).collect(Collectors.toList());
      // 将组件属性选项的Json字符串转为对象给前端
      for (DcCfgSafeDTO dto : CollectionUtils.emptyIfNull(childrenList)) {
        if (StringUtils.isNotEmpty(dto.getCfgCompProps())) {
          dto.setCfgCompPropsObj(JsonUtil.parseJson(dto.getCfgCompProps(), new TypeReference<Map<String, Object>>() {
          }));
          dto.setCfgCompProps(null);
        }
      }
      dcCfgDTO.setChildren(childrenList);
      // 处理分组配置
      dcCfgDTO.setParamName(dcCfgDTO.getParamGroupName());
      dcCfgDTO.setParamGroupName(null);
      dcCfgDTO.setRelParamCodes(null);
    }
    return groupCfgList;
  }

  @Override
  @Transactional
  public ResultVO<Void> modSafeParamData(DcCfgSaveRequest request) {
    Assert.hasLength(request.getParamCode(), "参数编码不能为空");
    String paramCode = request.getParamCode();
    String paramVal = request.getParamVal();
    // 更新配置值
    int affectedRows = dcCfgMapper.updateParamValue(paramCode, paramVal, SessionUtil.getLoginInfo().getUserId());
    if (affectedRows < 1) {
      return BaseErrorConstant.DC_PARAM_NOT_FOUND_ERROR.toResult(paramCode);
    }
    // 处理安全校验规则相关配置
    modSecurityParamData(paramCode, paramVal);
    return ResultVO.success();
  }

  /**
   * 修改安全校验规则相关配置
   */
  private void modSecurityParamData(String paramCode, String paramVal) {
    if (BaseConsts.PASSWORD_STRATEGY_CFG_LIST.contains(paramCode)) {
      String securityRule = SystemParameter.SECURITY_RULE_LOWER.getValueFromDb();
      SecurityRuleDto securityRuleDto = JsonUtil.parseJson(securityRule, SecurityRuleDto.class);
      Assert.notNull(securityRuleDto, "安全校验规则配置解析失败，请检查参数配置是否正确");
      switch (paramCode) {
        case "PWD_COMPOSITION":
          securityRuleDto.setPwdComposition(paramVal);
          break;
        case "PWD_MIN_LENGTH":
          securityRuleDto.setPwdMinLength(Long.parseLong(paramVal));
          break;
        case "LOGIN_RUN_MODEL":
          securityRuleDto.setLoginRunModel(paramVal);
          break;
        case "LOGIN_RUN_MODEL_RULE":
          securityRuleDto.setLoginRunModelRule(paramVal);
          break;
        case "PWD_KEYBOARD_NEAR":
          securityRuleDto.setPwdKeyboardNear(BaseConsts.TRUE.equals(paramVal) ? 1L : 0L);
          break;
        case "PWD_SAME_CHAR":
          securityRuleDto.setPwdSameChar(BaseConsts.TRUE.equals(paramVal) ? 1L : 0L);
          break;
        default:
          break;
      }
      // 更新安全校验规则
      dcCfgMapper.updateParamValue(SystemParameter.SECURITY_RULE_LOWER.getCode(), JsonUtil.toJsonString(securityRuleDto),
        SessionUtil.getLoginInfo().getUserId());
    }
  }

  /**
   * 个性化逻辑
   */
  private void doForDocChain(String paramCode, String paramVal) {
    if (SystemParameter.DOCCHAIN_TOPIC_API_REQUEST.getCode().equals(paramCode)) {
      // 标记 DocChain 策略设置发生变动
      knowledgeBaseManageMapper.updateDocChainExtraCfgTime();
    }
    else if (SystemParameter.DOCCHAIN_OCR_USER_NAME.getCode().equals(paramCode) || SystemParameter.DOCCHAIN_OCR_PASSWORD.getCode().equals(paramCode)
      || SystemParameter.DOCCHAIN_OCR_API_KEY.getCode().equals(paramCode)) {
      // DocChain ORC 账号发生变化，需要同步到博特助手租户设置信息中
      Long userId = SessionUtil.getLoginInfo().getUserId();
      TenantSettingInfoDTO dto = tenantSettingInfoManageMapper.getTenantSettingInfoByTenantIdAndType(CommonConsts.COPILOT_TENANT_ID,
        BaseConsts.FUNC_TYPE_KNOWLEDGE);
      Map<String, Object> settingInfo = new HashMap<>();
      settingInfo.put("enabled", true);
      if (SystemParameter.DOCCHAIN_OCR_USER_NAME.getCode().equals(paramCode)) {
        settingInfo.put("userName", paramVal);
      }
      else if (SystemParameter.DOCCHAIN_OCR_PASSWORD.getCode().equals(paramCode)) {
        settingInfo.put("token", paramVal);
      }
      else {
        settingInfo.put("apiKey", paramVal);
      }
      if (dto == null) {
        dto = new TenantSettingInfoDTO();
        dto.setSettingId(Sequences.TENANT_SETTING_INFO_SETTING_ID.next());
        dto.setTenantId(CommonConsts.COPILOT_TENANT_ID);
        dto.setFuncType(BaseConsts.FUNC_TYPE_KNOWLEDGE);
        dto.setSettingInfo(JsonUtil.toJsonString(settingInfo));
        dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
        dto.setCreatorId(userId);
        tenantSettingInfoManageMapper.insertTenantSettingInfo(dto);
      }
      else {
        Map<String, Object> map = JsonUtil.parseJson(dto.getSettingInfo(), new TypeReference<>() {
        });
        if (MapUtils.isEmpty(map)) {
          map = new HashMap<>();
        }
        map.putAll(settingInfo);
        dto.setSettingInfo(JsonUtil.toJsonString(map));
        dto.setUpdatorId(userId);
        tenantSettingInfoManageMapper.updateTenantSettingInfo(dto);
      }
      // 刷新缓存
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, CommonConsts.COPILOT_TENANT_ID.toString());
    }
  }

  /**
   * 默认模型发生变动，平台租户（tenantId in -1 2）对应的默认模型配置，需要动态调整
   */
  private void doForDefaultModel(String paramCode, String paramVal) {
    if (!SystemParameter.COMMON_CHAT_MODEL_ID.getCode().equals(paramCode)) {
      return;
    }
    Long userId = SessionUtil.getLoginInfo().getUserId();
    Map<String, Object> settingInfo = new HashMap<>();
    settingInfo.put("largeModelId", paramVal);
    List<Long> tenantIds = Arrays.asList(BaseConsts.PLATFORM_TENANT_ID, BaseConsts.ASSET_TENANT_ID);
    for (Long tenantId : tenantIds) {
      TenantSettingInfoDTO dto = tenantSettingInfoManageMapper.getTenantSettingInfoByTenantIdAndType(tenantId,
        BaseConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL);
      if (dto == null) {
        dto = new TenantSettingInfoDTO();
        dto.setSettingId(Sequences.TENANT_SETTING_INFO_SETTING_ID.next());
        dto.setTenantId(tenantId);
        dto.setFuncType(BaseConsts.FUNC_DEFAULT_TYPE_LARGE_MODEL);
        dto.setSettingInfo(JsonUtil.toJsonString(settingInfo));
        dto.setStatusCd(BaseConsts.STATUS_CD_VALID);
        dto.setCreatorId(userId);
        tenantSettingInfoManageMapper.insertTenantSettingInfo(dto);
      }
      else {
        dto.setSettingInfo(JsonUtil.toJsonString(settingInfo));
        dto.setUpdatorId(userId);
        tenantSettingInfoManageMapper.updateTenantSettingInfo(dto);
      }
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_TENANT_SETTING, tenantId.toString());
    }
  }
}
