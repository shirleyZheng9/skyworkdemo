package com.iwhalecloud.bote.service.skill.impl;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.common.consts.BaseConsts;
import com.iwhalecloud.bote.common.consts.CatalogConsts;
import com.iwhalecloud.bote.common.diffc.DataDifferenceStarter;
import com.iwhalecloud.bote.common.enums.BaseErrorConstant;
import com.iwhalecloud.bote.common.enums.DataSyncCodeEnum;
import com.iwhalecloud.bote.common.enums.OperClassEnum;
import com.iwhalecloud.bote.common.util.SessionUtil;
import com.iwhalecloud.bote.dto.skill.DataSourceDTO;
import com.iwhalecloud.bote.dto.skill.ParseSqlResult;
import com.iwhalecloud.bote.dto.skill.SimpleSkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SqlOperaParams;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bote.mapper.skill.DataSourceManageMapper;
import com.iwhalecloud.bote.mapper.skill.QuerySkillMapper;
import com.iwhalecloud.bote.mapper.skill.SkillSqlManageMapper;
import com.iwhalecloud.bote.service.base.ICatalogManageService;
import com.iwhalecloud.bote.service.element.IResourceElementService;
import com.iwhalecloud.bote.service.element.ResourceElementFactory;
import com.iwhalecloud.bote.service.skill.ISkillSqlManageService;
import com.iwhalecloud.bote.service.skill.impl.helper.ParseSqlHelper;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import com.iwhalecloud.bss.litchi.diffc.result.DataDifference;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 技能：SQL 服务实现
 *
 * @author auto
 * @since 2024-09-15
 */
@Service
@RequiredArgsConstructor
public class SkillSqlManageServiceImpl implements ISkillSqlManageService {

  private final SkillSqlManageMapper sqlManageMapper;
  private final QuerySkillMapper querySkillMapper;
  private final DataSourceManageMapper dataSourceManageMapper;
  private final ICatalogManageService catalogManageService;
  private final ParseSqlHelper parseSqlHelper;
  private final IResourceElementService resourceElementService;

  @Override
  @Transactional
  public ResultVO<SkillSqlDTO> saveSkillSql(SkillSqlDTO sql) {
    if (sql.getCopyServiceId() != null) {
      return copySkillSql(sql);
    }
    if (sqlManageMapper.existsSkillSqlCode(sql)) {
      return BaseErrorConstant.CHECK_CODE.toResult(sql.getServiceCode());
    }
    DataSourceDTO dataSource = sql.getDataSource();
    if (Objects.nonNull(dataSource)) {
      sql.setDataSourceId(dataSource.getDataSourceId());
    }
    if (dataSourceManageMapper.getDataSource(sql.getTenantId(), sql.getDataSourceId()) == null) {
      return BaseErrorConstant.BOT_SKILL_DATASOURCE_INST_NOT_EXISTS.toResult();
    }
    sql.setStatusCd(BaseConsts.STATUS_CD_VALID);
    SkillSqlDTO old = sql.getServiceId() == null ? null : findSkillSql(sql.getTenantId(), sql.getServiceId());
    DataDifference<SkillSqlDTO> difference = DataDifferenceStarter.computeSaveAndLog(old, sql, false, sql.getTenantId(), OperClassEnum.SKILL_SQL);
    if (difference == null) {
      return BaseErrorConstant.NO_DIFFERENCE.toResult();
    }
    return ResultVO.success(difference.getToSaveData());
  }

  private ResultVO<SkillSqlDTO> copySkillSql(SkillSqlDTO sql) {
    SkillSqlDTO oldSql = sqlManageMapper.getSkillSql(sql.getTenantId(), sql.getCopyServiceId());
    if (oldSql == null) {
      return BaseErrorConstant.BOT_SKILL_SQL_NOT_EXISTS.toResult(sql.getCopyServiceId());
    }
    if (sqlManageMapper.existsSkillSqlCode(sql)) {
      return BaseErrorConstant.CHECK_CODE.toResult(sql.getServiceCode());
    }
    DataDifferenceStarter.computeSaveAndLog(null, sql, false, sql.getTenantId(), OperClassEnum.SKILL_SQL);
    return ResultVO.success(sql);
  }

  @Override
  @Nullable
  public SkillSqlDTO findSkillSql(Long tenantId, Long serviceId) {
    SkillSqlDTO sql = sqlManageMapper.getSkillSql(tenantId, serviceId);
    if (Objects.isNull(sql)) {
      return null;
    }
    DataSourceDTO botSkillDataSource = dataSourceManageMapper.getDataSource(tenantId, sql.getDataSourceId());
    sql.setDataSource(botSkillDataSource);
    return sql;
  }

  @Override
  public List<SimpleSkillSqlDTO> querySkillSqlList(SkillQueryParams params) {
    return querySkillMapper.selectSkillSqlList(params);
  }

  @Override
  public PageInfo<SkillSqlDTO> querySkillSqlPage(SkillQueryParams params) {
    if (!BaseConsts.FALSE.equals(params.getConfigFlag())) {
      params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    }
    // noinspection resource
    return sqlManageMapper.selectSkillSqlPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public PageInfo<SimpleSkillSqlDTO> querySimpleSkillSqlPage(SkillQueryParams params) {
    params.setCatalogItemList(catalogManageService.queryChildrenCatalogIds(params.getTenantId(), params.getCatalogItemId(), CatalogConsts.TYPE_SKILL));
    // noinspection resource
    return querySkillMapper.selectSkillSqlPage(params, params.buildRowBounds()).toPageInfo();
  }

  @Override
  public ParseSqlResult parseSql(SqlOperaParams params) {
    return parseSqlHelper.parseSql(params);
  }

  @Override
  @Transactional
  public ResultVO<Void> deleteSkillSql(Long tenantId, Long serviceId) {
    if (resourceElementService.existsRelatedResource(tenantId, serviceId, DataSyncCodeEnum.SKILL_SQL.getCode())) {
      return ResultVO.fail("SQL已存在关联配置数据，不允许删除");
    }
    sqlManageMapper.deleteSkillSql(tenantId, serviceId, SessionUtil.getLoginInfo().getUserId());
    ResourceElementFactory.get(OperClassEnum.SKILL_SQL.name()).clear(tenantId, serviceId);
    return ResultVO.success();
  }
}
