package com.iwhalecloud.bote.service.portal;

import com.iwhalecloud.bote.dto.portal.AccountDTO;
import com.iwhalecloud.bote.dto.portal.KnowledgeGraphAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.WeKnoraAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;

/**
 * 租户设置信息管理服务
 *
 * @author auto
 * @since 2024-12-18
 */
public interface ITenantSettingInfoManageService {

  /**
   * 根据租户 ID、机器人 ID，收集设置信息
   *
   * @param tenantId 租户 ID
   * @param botId 机器人 ID
   * @return 结果
   */
  Map<String, String> findSettingInfo(Long tenantId, @Nullable Long botId);

  /**
   * 查询单个租户设置信息
   *
   * @param tenantId 租户 ID
   * @param funcType 功能类型
   * @return 租户设置信息
   */
  @Nullable
  TenantSettingInfoDTO findTenantSettingInfo(Long tenantId, String funcType);

  /**
   * 保存租户设置信息
   *
   * @param tenantSettingInfo 租户设置信息
   * @return 结果
   */
  ResultVO<TenantSettingInfoDTO> saveTenantSettingInfo(TenantSettingInfoDTO tenantSettingInfo);

  /**
   * 查询租户设置信息列表
   *
   * @param tenantId 租户 ID
   * @return 租户设置信息列表
   */
  List<TenantSettingInfoDTO> queryTenantSettingInfoList(Long tenantId);

  /**
   * 查询租户设置信息
   *
   * @param queryParams 查询参数
   * @return 结果
   */
  ResultVO<Map<String, TenantSettingInfoDTO>> batchGetTenantSettingInfo(TenantQueryParams queryParams);

  /**
   * 测试DocChain账号
   *
   * @param dto 账号信息
   * @return 结果
   */
  ResultVO<Void> testDocChainAccount(AccountDTO dto);

  /**
   * 同步DocChain账号
   *
   * @param dto 账号信息
   * @return 结果
   */
  ResultVO<String> syncDocChainAccount(AccountDTO dto);

  /**
   * 保存 funcSwitch 信息
   *
   * @param tenantId 租户 ID
   * @param botId 智能应用ID
   * @param funcSwitch 开关数据
   */
  void saveFuncSwitchInfo(Long tenantId, Long botId, String funcSwitch);

  /**
   * 保存插件市场信息
   *
   * @param tenantId 租户 ID
   * @param apiKey 门户开发者密钥
   */
  void savePluginHub(Long tenantId, String apiKey);

  /**
   * 测试Weknora账号
   *
   * @param account 账号信息
   * @return 结果
   */
  ResultVO<Void> testWeknoraAccount(WeKnoraAccountSettingDTO account);
  /**
   * 同步WeKnora账号
   *
   * @param account 账号信息
   * @return 结果
   */
  ResultVO<Void> registerWeknoraAccount(WeKnoraAccountSettingDTO account);

  /**
   * 修改WeKnora账号密码
   * @param account 账号信息
   * @return 结果
   */
  ResultVO<Void> updateWeknoraAccountPw(WeKnoraAccountSettingDTO account);

  /**
   * 测试 knowledgeGraph 登录
   *
   * @param account 登录参数
   * @return 结果
   */
  ResultVO<Void> testKnowledgeGraphAccount(KnowledgeGraphAccountSettingDTO account);
}
