package com.iwhalecloud.bote.generator.flow.converter.node;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.generator.flow.node.SqlNodeData;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.generator.flow.context.FlowConverterContext;
import com.iwhalecloud.bote.generator.flow.converter.AbstractNodeConverter;
import com.iwhalecloud.bss.litchi.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * SQL 节点转换器
 *
 * @author bianjp
 * @since 2025-03-31
 */
public class SqlNodeConverter extends AbstractNodeConverter<SqlNodeData> {
  public SqlNodeConverter() {
    super(SqlNodeData.class);
  }

  @Override
  protected void simplifyNodeData(SqlNodeData data) {
    data.setParameters(simplifyParameter(data.getParameters()));
    data.setOutData(null);
  }

  @Override
  protected void supplementNodeData(FlowConverterContext context, SqlNodeData data) {
    data.setParameters(supplementParameter(data.getParameters()));
    if (data.getSqlId() != null) {
      SkillSqlDTO sql = flowAiQueryMapper.selectSqlServiceById(context.getTenantId(), data.getSqlId());
      if (sql != null && StringUtils.isNotEmpty(sql.getRespJson())) {
        data.setOutData(JsonUtil.parseJson(sql.getRespJson(), ParameterSpec.class));
      }
    }
  }
}
