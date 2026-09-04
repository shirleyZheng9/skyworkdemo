package com.iwhalecloud.bote.common.diffc.extend;

import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import com.iwhalecloud.bss.litchi.diffc.mapper.DataMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 差异计算数据更新操作
 *
 * @author chen.linfa
 * @since 2024-08-12
 */
@SuppressFBWarnings("SQL_INJECTION_SPRING_JDBC")
public class DiffcDataMapper extends DataMapper {
  public DiffcDataMapper(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  /**
   * 更新数据状态为删除状态
   *
   * @param objIdList id列表
   * @param updatorId 修改人Id
   * @param tableName 表名
   * @param primaryKey 表主键
   * @param delStatusCd 删除状态
   * @return 结果
   */
  @Override
  public int updateToRemoveStatus(List<Long> objIdList, Long updatorId, String tableName, String primaryKey, String delStatusCd) {
    String sql = "update " + tableName + " set status_cd = ?, updator_id = ?, updated_time = now() ";
    if (DbUtil.isOracle()) {
      sql = sql.replace("now()", "sysdate");
    }
    sql = sql + " where " + primaryKey + " in (" + StringUtils.join(objIdList, ",") + ")";
    return jdbcTemplate.update(sql, delStatusCd, updatorId);
  }

  /**
   * 删除数据状态为删除状态的数据
   *
   * @param tableName 表名
   * @return 结果
   */
  @Override
  public int deleteRemoveStatusData(String tableName, String statusCd) {
    String sql = "delete " + tableName + " from ? where status_cd = ?";
    return jdbcTemplate.update(sql, statusCd);
  }
}
