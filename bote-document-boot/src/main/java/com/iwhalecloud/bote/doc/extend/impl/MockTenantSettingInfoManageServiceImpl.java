package com.iwhalecloud.bote.doc.extend.impl;

import com.iwhalecloud.bote.dto.portal.AccountDTO;
import com.iwhalecloud.bote.dto.portal.KnowledgeGraphAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.portal.WeKnoraAccountSettingDTO;
import com.iwhalecloud.bote.dto.portal.query.TenantQueryParams;
import com.iwhalecloud.bote.service.portal.ITenantSettingInfoManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * @author chen.linfa
 * @since 2025-11-19
 */
@Component
public class MockTenantSettingInfoManageServiceImpl implements ITenantSettingInfoManageService {
  @Override
  public Map<String, String> findSettingInfo(Long tenantId, Long botId) {
    return Map.of();
  }

  @Override
  public TenantSettingInfoDTO findTenantSettingInfo(Long tenantId, String funcType) {
    return null;
  }

  @Override
  public ResultVO<TenantSettingInfoDTO> saveTenantSettingInfo(TenantSettingInfoDTO tenantSettingInfo) {
    return ResultVO.success();
  }

  @Override
  public List<TenantSettingInfoDTO> queryTenantSettingInfoList(Long tenantId) {
    return List.of();
  }

  @Override
  public ResultVO<Map<String, TenantSettingInfoDTO>> batchGetTenantSettingInfo(TenantQueryParams queryParams) {
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> testDocChainAccount(AccountDTO dto) {
    return ResultVO.success();
  }

  @Override
  public ResultVO<String> syncDocChainAccount(AccountDTO dto) {
    return ResultVO.success();
  }

  @Override
  public void saveFuncSwitchInfo(Long tenantId, Long botId, String funcSwitch) {
    // 不做任何操作
  }

  @Override
  public void savePluginHub(Long tenantId, String apiKey) {
    // 不做任何操作
  }

  @Override
  public ResultVO<Void> testWeknoraAccount(WeKnoraAccountSettingDTO account) {
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> registerWeknoraAccount(WeKnoraAccountSettingDTO account) {
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> updateWeknoraAccountPw(WeKnoraAccountSettingDTO account) {
    return ResultVO.success();
  }

  @Override
  public ResultVO<Void> testKnowledgeGraphAccount(KnowledgeGraphAccountSettingDTO account) {
    return ResultVO.success();
  }
}
