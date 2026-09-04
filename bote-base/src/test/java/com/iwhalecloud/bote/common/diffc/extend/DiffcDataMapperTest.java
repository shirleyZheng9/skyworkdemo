package com.iwhalecloud.bote.common.diffc.extend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iwhalecloud.bss.litchi.database.util.DbUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * {@link DiffcDataMapper} 单元测试。
 *
 * <p>覆盖 updateToRemoveStatus 的 Oracle/非 Oracle SQL 拼接分支与 deleteRemoveStatusData 的
 * SQL 构造，JdbcTemplate 用 @Mock 隔离，DbUtil.isOracle() 以 mockStatic 注入。</p>
 */
class DiffcDataMapperTest {

  @Test
  void updateToRemoveStatus_nonOracle_usesNowFunction() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(5);
    DiffcDataMapper mapper = new DiffcDataMapper(jdbcTemplate);

    int result;
    try (MockedStatic<DbUtil> dbUtil = mockStatic(DbUtil.class)) {
      dbUtil.when(DbUtil::isOracle).thenReturn(false);
      result = mapper.updateToRemoveStatus(List.of(1L, 2L), 10L, "t_cat", "id", "X");
    }

    assertThat(result).isEqualTo(5);
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).update(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue())
      .isEqualTo("update t_cat set status_cd = ?, updator_id = ?, updated_time = now()  where id in (1,2)");
  }

  @Test
  void updateToRemoveStatus_oracle_replacesNowWithSysdate() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(3);
    DiffcDataMapper mapper = new DiffcDataMapper(jdbcTemplate);

    try (MockedStatic<DbUtil> dbUtil = mockStatic(DbUtil.class)) {
      dbUtil.when(DbUtil::isOracle).thenReturn(true);
      mapper.updateToRemoveStatus(List.of(7L), 99L, "t_label", "label_id", "D");
    }

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).update(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue())
      .contains("sysdate")
      .doesNotContain("now()")
      .isEqualTo("update t_label set status_cd = ?, updator_id = ?, updated_time = sysdate  where label_id in (7)");
  }

  @Test
  void deleteRemoveStatusData_buildsDeleteSqlAndCallsUpdate() {
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(2);
    DiffcDataMapper mapper = new DiffcDataMapper(jdbcTemplate);

    int result = mapper.deleteRemoveStatusData("t_cat", "X");

    assertThat(result).isEqualTo(2);
    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
    verify(jdbcTemplate).update(sqlCaptor.capture(), any(Object[].class));
    assertThat(sqlCaptor.getValue()).isEqualTo("delete t_cat from ? where status_cd = ?");
  }
}
