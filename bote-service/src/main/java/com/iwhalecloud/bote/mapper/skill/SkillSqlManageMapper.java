package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * 技能：SQL Mapper
 *
 * @author auto
 * @since 2024-09-15
 */
public interface SkillSqlManageMapper {
  /**
   * 校验 SQL 的编码唯一性
   *
   * @param sql SQ
   * @return 结果
   */
  boolean existsSkillSqlCode(@Param("dto") SkillSqlDTO sql);

  /**
   * 根据主键获取 SQL
   *
   * @param tenantId 租户 ID
   * @param serviceId SQL ID
   * @return SQL服务
   */
  SkillSqlDTO getSkillSql(@Param("tenantId") Long tenantId, @Param("id") Long serviceId);

  /**
   * 查询 SQL 服务的简单信息
   */
  SkillSqlDTO selectSimpleSql(@Param("tenantId") Long tenantId, @Param("id") Long serviceId);

  /**
   * 批量查询 SQL 服务的简单信息
   */
  List<SkillSqlDTO> selectSimpleSqls(@Param("tenantId") Long tenantId, @Param("ids") List<Long> serviceIds);

  /**
   * 新增 SQL
   *
   * @param sql SQL
   * @return 结果
   */
  int insertSkillSql(@Param("dto") SkillSqlDTO sql);

  /**
   * 修改 SQL
   *
   * @param sql SQL
   * @return 结果
   */
  int updateSkillSql(@Param("dto") SkillSqlDTO sql);

  /**
   * 删除 SQL
   */
  int deleteSkillSql(@Param("tenantId") Long tenantId, @Param("serviceId") Long serviceId, @Param("updatorId") Long updatorId);

  /**
   * 获取SQL服务列表（分页）
   *
   * @param queryParams 查询条件
   * @return SQL服务分页列表
   */
  Page<SkillSqlDTO> selectSkillSqlPage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);
}
