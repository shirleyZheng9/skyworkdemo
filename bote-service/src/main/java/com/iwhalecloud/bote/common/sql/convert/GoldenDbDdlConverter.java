package com.iwhalecloud.bote.common.sql.convert;

import com.iwhalecloud.bote.common.sql.consts.TableModelConsts;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import com.iwhalecloud.bote.dto.database.DataTableDTO;
import com.iwhalecloud.bote.service.skill.IDataSourceManageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GoldenDB DDL 转换器
 *
 * @author YuBoliang
 * @since 2023-03-27
 */
@Component
@RequiredArgsConstructor
public class GoldenDbDdlConverter extends MysqlDdlConverter {

  private final IDataSourceManageService dataSourceService;


  private static final Pattern PATTERN = Pattern.compile("[?&]lcdpSharding=([^&]*)", Pattern.CASE_INSENSITIVE);
  @Override
  protected String getCreateTableSql(DataTableDTO appModelTable) {

    if (appModelTable.getDataSourceId() == null) {
      return TableModelConsts.CREATE_TABLE_DISTRIBUTED;
    }

    return getCreateTableByShadingInfo(appModelTable);

  }

  /**
   * 根据分片信息创建表
   * @param tableDTO
   * @return
   */
  private String getCreateTableByShadingInfo(DataTableDTO tableDTO) {
    DataSourceProperties properties = dataSourceService.findDataSourceProperties(tableDTO.getTenantId(), tableDTO.getDataSourceId());

    // 没有配置，则返回默认的（正常不会出现这样的数据）
    String url = properties == null ? "" : properties.getUrl();
    if (StringUtils.isEmpty(url)) {
      return TableModelConsts.CREATE_TABLE_DISTRIBUTED;
    }

    Matcher m = PATTERN.matcher(url);
    // 有配置，有可能是指定了分片，有可能是指定了空字符串
    if (m.find()) {
      String shadingInfo = m.group(1);

      // 指定了空字符串，当成普通数据库不指定数据库创建
      if (StringUtils.isEmpty(shadingInfo)) {
        return TableModelConsts.CREATE_TABLE;
      }

      // 替换分片
      return TableModelConsts.CREATE_TABLE_DISTRIBUTED_SHADING.replace("@@@", shadingInfo);
    }

    // 没有匹配到也返回默认的
    return TableModelConsts.CREATE_TABLE_DISTRIBUTED;
  }
}
