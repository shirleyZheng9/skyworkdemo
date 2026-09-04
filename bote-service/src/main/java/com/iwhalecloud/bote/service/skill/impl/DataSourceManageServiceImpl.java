package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.datasource.helper.CreateDatabaseHelper;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.SystemParameter;
import com.iwhalecloud.bote.common.util.AesUtil;
import com.iwhalecloud.bote.common.util.CryptoUtil;
import com.iwhalecloud.bote.common.util.DatabaseUtil;
import com.iwhalecloud.bote.common.util.EnvUtil;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.config.properties.PlatformDatabaseProperties;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceInstDTO;
import com.iwhalecloud.bote.dto.skill.DataSourceTestParams;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.DataSourceManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.skill.IDataSourceManageService;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：数据源 服务实现
 *
 * @author auto
 * @since 2024-09-16
 */
@Service
@RequiredArgsConstructor
public class DataSourceManageServiceImpl implements IDataSourceManageService {

  private final DataSourceManageMapper dataSourceManageMapper;
  private final ICatalogManageService catalogManageService;
  private final IResourceElementService resourceElementService;
  private final CreateDatabaseHelper createDatabaseHelper;
  private final PlatformDatabaseProperties platformDatabaseProperties;

  @Override
  @Transactional
  public ResultVO<DataSourceDTO> saveDataSource(DataSourceDTO dataSource) {
    if (dataSourceManageMapper.existsDataSourceCode(dataSource)) {
      return BaseErrorConstant.CHECK_CODE.toResult(dataSource.getDataSourceCode());
    }
    dataSource.setStatusCd(BaseConsts.STATUS_CD_VALID);
    DataSourceDTO old = dataSource.getDataSourceId() == null ? null : findDataSource(dataSource.getTenantId(), dataSource.getDataSourceId());
    // 新建的数据源默认都是自定义数据源
    if (old == null && StringUtils.isBlank(dataSource.getDataSourceChannel())) {
      dataSource.setDataSourceChannel(BaseConsts.DATABASE_TUNNEL_CUSTOM);
    }
    // 数据源密码加密存储
    encryptPassword(dataSource.getInsts());
    DataDifference<DataSourceDTO> difference = DataDifferenceStarter.computeSave(old, dataSource, true, dataSource.getTenantId());
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  @Override
  @Nullable
  public DataSourceDTO findDataSource(Long tenantId, Long dataSourceId) {
    DataSourceDTO dataSource = dataSourceManageMapper.getDataSource(tenantId, dataSourceId);
    if (dataSource == null) {
      return null;
    }
    List<DataSourceInstDTO> insts = dataSourceManageMapper.selectDataSourceInstList(tenantId, dataSourceId);
    // 数据源密码解密
    decryptPassword(insts);
    dataSource.setInsts(insts);
    return dataSource;
  }

  @Override
  @Nullable
  public DataSourceProperties findDataSourceProperties(Long tenantId, Long dataSourceId) {
    DataSourceProperties properties = dataSourceManageMapper.selectInstByDataSourceIdAndEnvCode(tenantId, dataSourceId, EnvUtil.getEnvCode());
    if (properties != null) {
      properties.setPassword(CryptoUtil.decrypt(properties.getPassword()));
    }
    return properties;
  }

  @Override
  public List<DataSourceDTO> queryDataSourceList(SkillQueryParams params) {
    return dataSourceManageMapper.selectDataSourceList(params);
  }

  @Override
  public PageInfo<DataSourceDTO> queryDataSourcePage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return dataSourceManageMapper.selectDataSourcePage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteDataSource(Long tenantId, Long dataSourceId) {
    if (resourceElementService.existsRelatedResource(tenantId, dataSourceId, DataSyncCodeEnum.DATA_SOURCE.getCode())) {
      return ResultVO.fail("数据源已存在关联配置数据，不允许删除");
    }
    dataSourceManageMapper.deleteDataSource(tenantId, dataSourceId, SessionUtil.getLoginInfo().getUserId());
    return ResultVO.success();
  }

  /**
   * 后端加密数据源密码
   */
  private void encryptPassword(List<DataSourceInstDTO> dataSourceInsts) {
    for (DataSourceInstDTO dataSourceInst : CollectionUtils.emptyIfNull(dataSourceInsts)) {
      dataSourceInst.setStatusCd(BaseConsts.STATUS_CD_VALID);
      dataSourceInst.setPassword(CryptoUtil.encrypt(dataSourceInst.getPassword()));
    }
  }

  /**
   * 后端解密数据源密码
   */
  private void decryptPassword(List<DataSourceInstDTO> dataSourceInsts) {
    for (DataSourceInstDTO dataSourceInst : CollectionUtils.emptyIfNull(dataSourceInsts)) {
      dataSourceInst.setPassword(CryptoUtil.decrypt(dataSourceInst.getPassword()));
    }
  }

  @Override
  public ResultVO<String> testDataSourceLink(DataSourceTestParams params) {
    String password = AesUtil.aesDecrypt(params.getPassword(), SystemParameter.ENCRYPTION_AES.getValueFromDb());
    if (StringUtils.isEmpty(password)) {
      return ResultVO.fail("密码解析异常");
    }
    return DatabaseUtil.testAppDataSource(params.getUrl(), params.getUsername(), password);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public DataSourceDTO getPlatformDataSource(Long tenantId) {
    // 获取租户对应的平台数据源
    DataSourceDTO dataSourceDTO = dataSourceManageMapper.selectPlatformDataSource(tenantId);
    // 不存在则创建
    if (dataSourceDTO == null) {
      // 创建租户对应的平台数据源
      DataSourceProperties database = createDatabaseHelper.createPlatformDatabase(tenantId, EnvUtil.getEnvCode(), null);
      // 保存数据源实例
      dataSourceDTO = new DataSourceDTO();
      dataSourceDTO.setDataSourceChannel(BaseConsts.DATABASE_TUNNEL_PLATFORM);
      dataSourceDTO.setTenantId(tenantId);
      dataSourceDTO.setDataSourceCode("bote_" + tenantId);
      dataSourceDTO.setDataSourceType(platformDatabaseProperties.getType());
      dataSourceDTO.setDataSourceName("平台数据源");
      DataSourceInstDTO instDTO = new DataSourceInstDTO();
      instDTO.setUrl(database.getUrl());
      instDTO.setUserName(database.getUsername());
      instDTO.setPassword(database.getPassword());
      instDTO.setTenantId(tenantId);
      instDTO.setEnvCode(EnvUtil.getEnvCode());
      dataSourceDTO.setInsts(Collections.singletonList(instDTO));
      saveDataSource(dataSourceDTO);
    }
    return dataSourceDTO;
  }
}
