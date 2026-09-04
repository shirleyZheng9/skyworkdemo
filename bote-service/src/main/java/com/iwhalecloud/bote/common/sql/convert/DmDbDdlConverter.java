package com.iwhalecloud.bote.common.sql.convert;

import com.iwhalecloud.bote.common.sql.script.TableModelScriptUtil;
import com.iwhalecloud.bote.dto.base.DataSourceProperties;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

@Component
public class DmDbDdlConverter extends OracleDdlConverter {

  @Override
  protected String getRenameSequenceSql(String oldSequenceCode, String newSequenceCode, DataSourceProperties dataSourceProperties) {
    if (Strings.CI.equals(oldSequenceCode, newSequenceCode)) {
      return "";
    }
    return TableModelScriptUtil.getDmRenameSequenceSql(dataSourceProperties.getTenantId(), dataSourceProperties.getDataSourceId(), dataSourceProperties.getUsername(), oldSequenceCode, newSequenceCode);
  }
}
