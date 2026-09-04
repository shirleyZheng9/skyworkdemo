package com.iwhalecloud.bote.controller.cutover;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwhalecloud.bote.common.annotation.IgnoreSign;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.portal.TenantSettingInfoDTO;
import com.iwhalecloud.bote.dto.tenant.setting.TenantDocChainAccountSettingDTO;
import com.iwhalecloud.bote.mapper.portal.TenantSettingInfoManageMapper;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 割接docChain账号
 *
 * @author qian.sisheng
 * @since 2026-06-18
 */
@RestController
@RequestMapping(path = BaseConsts.API_PREFIX + "cutOver/", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Hidden
@IgnoreSign
public class MigrateDocChainAccountController {

  private final TenantSettingInfoManageMapper tenantSettingInfoManageMapper;
  private final IRefreshCacheService refreshCacheService;

  @GetMapping(path = "migrateDocChainAccount", produces = MediaType.TEXT_PLAIN_VALUE)
  public void migrateDocChainAccount(HttpServletResponse response) throws IOException {
    response.setContentType("text/plain;charset=UTF-8");
    PrintWriter writer = response.getWriter(); //NOPMD - suppressed CloseResource - HTTP 输出流不需要手动关闭
    // 查询需要割接的数据
    List<TenantSettingInfoDTO> tenantSettingInfoList = tenantSettingInfoManageMapper.selectTenantSettingInfoByType(BaseConsts.FUNC_TYPE_KNOWLEDGE);
    if (CollectionUtils.isEmpty(tenantSettingInfoList)) {
      addLog(writer, "没有找到需要割接的数据");
      return;
    }
    addLog(writer, "开始割接docChain账号, 共 %s 条数据\n", tenantSettingInfoList.size());
    for (TenantSettingInfoDTO tenantSettingInfo : tenantSettingInfoList) {
      String settingInfo = tenantSettingInfo.getSettingInfo();
      if (StringUtils.isEmpty(settingInfo)) {
        continue;
      }
      TenantDocChainAccountSettingDTO tenantDocChainAccountSetting = JsonUtil.parseJson(settingInfo, new TypeReference<>() {
      });
      if (tenantDocChainAccountSetting == null) {
        continue;
      }
      // 如果 token 不为空，则跳过
      if (StringUtils.isNotEmpty(tenantDocChainAccountSetting.getToken())) {
        continue;
      }
      tenantDocChainAccountSetting.setToken(tenantDocChainAccountSetting.getPassword());
      tenantSettingInfo.setSettingInfo(JsonUtil.toJsonString(tenantDocChainAccountSetting));
      tenantSettingInfoManageMapper.updateTenantSettingInfo(tenantSettingInfo);
    }
    refreshCacheService.refreshAll(CacheConsts.CACHE_NAME_TENANT_SETTING);
    addLog(writer, "割接docChain账号完成");
  }

  /**
   * 打印日志到 HTTP 响应
   */
  @SuppressFBWarnings("XSS_SERVLET")
  private void addLog(PrintWriter writer, String msg, Object... args) {
    writer.printf(msg, args);
    writer.flush();
  }
}
