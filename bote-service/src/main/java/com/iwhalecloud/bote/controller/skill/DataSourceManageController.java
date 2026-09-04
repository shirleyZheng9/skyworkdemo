package com.iwhalecloud.bote.controller.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceInstDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceTestParams;
import com.iwhalecloud.bote.dto.skill.DataSourceVectorParams;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.service.plugin.IPluginRemoteService;
import com.iwhalecloud.bote.service.skill.IDataSourceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.cache.refresh.IRefreshCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 技能：数据源 Controller
 *
 * @author auto
 * @since 2024-09-16
 */
@RequestMapping(path = BaseConsts.API_PREFIX + "manager/dataSource", name = MediaType.APPLICATION_JSON_VALUE)
@RestController
@RequiredArgsConstructor
@Tag(name = "技能：数据源管理")
public class DataSourceManageController {
  /** 禁止的数据源连接参数。根据安全基线 REQ_181 需要防范危险的参数 */
  private static final Set<String> DISALLOWED_URL_PARAMS = Set.of(
    "allowLoadLocalInfileInPath",
    "allowLoadLocalInfile",
    "allowUrlInLocalInfile");

  private final IDataSourceManageService dataSourceManageService;
  private final IPluginRemoteService pluginRemoteService;
  private final IRefreshCacheService refreshCacheService;

  @Operation(summary = "保存数据源")
  @PostMapping("saveDataSource")
  public ResultVO<DataSourceDTO> saveDataSource(@RequestBody @Valid DataSourceDTO dataSource) {
    // 数据源密码前端已加密，进行 AES 解密
    aesDecryptPassword(dataSource);
    validateDataSourceUrl(dataSource);
    Long dataSourceId = dataSource.getDataSourceId();
    ResultVO<DataSourceDTO> result = dataSourceManageService.saveDataSource(dataSource);
    // 修改数据源时刷新缓存
    if (dataSourceId != null && result.isSuccess()) {
      String key = dataSource.getTenantId() + CacheConsts.COLON + dataSourceId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_BOT_DATA_SOURCE, key);
    }
    return result;
  }

  /**
   * 校验数据源连接地址
   */
  private void validateDataSourceUrl(DataSourceDTO dataSource) {
    for (DataSourceInstDTO inst : ListUtils.emptyIfNull(dataSource.getInsts())) {
      String url = inst.getUrl();
      if (StringUtils.isEmpty(url)) {
        continue;
      }
      Assert.isTrue(url.startsWith("jdbc:"), "连接地址必须以 jdbc: 开头");
      for (String param : DISALLOWED_URL_PARAMS) {
        Assert.isTrue(!url.contains(param), () -> "连接地址不能包含危险参数: " + param);
      }
    }
  }

  @Operation(summary = "查询单个数据源")
  @GetMapping("findDataSource")
  public ResultVO<DataSourceDTO> findDataSource(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("dataSourceId") Long dataSourceId) {
    Assert.notNull(dataSourceId, "数据源ID不能为空");
    DataSourceDTO dataSource = dataSourceManageService.findDataSource(tenantId, dataSourceId);
    // 数据源密码，进行AES加密返回前端
    aesEncryptPassword(dataSource);
    return ResultVO.success(dataSource);
  }

  @Operation(summary = "查询数据源列表")
  @PostMapping("queryDataSourceList")
  public ResultVO<List<DataSourceDTO>> queryDataSourceList(@RequestBody SkillQueryParams params) {
    return ResultVO.success(dataSourceManageService.queryDataSourceList(params));
  }

  @Operation(summary = "分页查询数据源")
  @PostMapping("queryDataSourcePage")
  public ResultVO<PageInfo<DataSourceDTO>> queryDataSourcePage(@RequestBody SkillQueryParams params) {
    return ResultVO.success(dataSourceManageService.queryDataSourcePage(params));
  }

  @Operation(summary = "删除数据源")
  @GetMapping("deleteDataSource")
  public ResultVO<Void> deleteDataSource(@RequestParam(value = "tenantId", required = false) Long tenantId,
    @RequestParam("dataSourceId") Long dataSourceId) {
    Assert.notNull(dataSourceId, "数据源ID不能为空");
    ResultVO<Void> result = dataSourceManageService.deleteDataSource(tenantId, dataSourceId);
    // 刷新缓存
    if (result.isSuccess()) {
      String key = tenantId + CacheConsts.COLON + dataSourceId;
      refreshCacheService.refresh(CacheConsts.CACHE_NAME_BOT_DATA_SOURCE, key);
    }
    return result;
  }

  @Operation(summary = "测试数据源连接")
  @PostMapping("testDataSourceLink")
  public ResultVO<String> testDataSourceLink(@RequestBody DataSourceTestParams params) {
    Assert.hasText(params.getUrl(), "链接不能为空");
    Assert.hasText(params.getUsername(), "账号不能为空");
    Assert.hasText(params.getPassword(), "密码不能为空");
    return dataSourceManageService.testDataSourceLink(params);
  }

  @Operation(summary = "向量化数据源到插件")
  @PostMapping("vector")
  public ResultVO<String> vector(@RequestBody DataSourceVectorParams params) {
    Assert.notNull(params.getDataSourceInstId(), "数据源实例ID不能为空");
    return pluginRemoteService.buildSchemaVector(params.getTenantId(), params.getDataSourceInstId());
  }

  /**
   * 数据源密码前端已加密，进行 AES 解密
   *
   * @param dataSource 数据源
   */
  private void aesDecryptPassword(DataSourceDTO dataSource) {
    CollectionUtils.emptyIfNull(dataSource.getInsts()).forEach(inst -> {
      if (StringUtils.isEmpty(inst.getPassword())) {
        return;
      }
      String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
      inst.setPassword(AesUtil.aesDecrypt(inst.getPassword(), encryptionKey));
    });
  }

  /**
   * 数据源密码返回给前端，进行 AES 加密
   *
   * @param dataSource 数据源
   */
  private void aesEncryptPassword(DataSourceDTO dataSource) {
    if (dataSource == null) {
      return;
    }
    CollectionUtils.emptyIfNull(dataSource.getInsts()).forEach(inst -> {
      if (StringUtils.isEmpty(inst.getPassword())) {
        return;
      }
      String encryptionKey = SystemParameter.ENCRYPTION_AES.getValueFromDb();
      inst.setPassword(AesUtil.aesEncrypt(inst.getPassword(), encryptionKey));
    });
  }
}
