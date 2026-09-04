package com.iwhalecloud.bote.service.skill;

import com.github.pagehelper.PageInfo;
import com.iwhalecloud.bote.dto.skill.ParseSqlResult;
import com.iwhalecloud.bote.dto.skill.SimpleSkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.dto.skill.SqlOperaParams;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import com.iwhalecloud.bss.litchi.base.vo.ResultVO;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 技能：SQL 服务
 *
 * @author auto
 * @since 2024-09-15
 */
public interface ISkillSqlManageService {

  /**
   * 查询单个 SQL
   *
   * @param tenantId 租户 ID
   * @param serviceId SQL
   * @return SQL服务
   */
  @Nullable
  SkillSqlDTO findSkillSql(Long tenantId, Long serviceId);

  /**
   * 保存 SQL
   *
   * @param sql SQL
   * @return 结果
   */
  ResultVO<SkillSqlDTO> saveSkillSql(SkillSqlDTO sql);

  /**
   * 删除 SQL
   *
   * @param tenantId 租户 ID
   * @param serviceId SQL 主键
   * @return 结果
   */
  ResultVO<Void> deleteSkillSql(Long tenantId, Long serviceId);

  /**
   * 查询 SQL 列表
   *
   * @param queryParams 查询条件
   * @return SQL 列表
   */
  List<SimpleSkillSqlDTO> querySkillSqlList(SkillQueryParams queryParams);

  /**
   * 查询 SQL 列表（分页）
   *
   * @param queryParams 查询条件
   * @return SQL 分页列表
   */
  PageInfo<SkillSqlDTO> querySkillSqlPage(SkillQueryParams queryParams);

  /**
   * 查询 SQL 列表（分页），用于其他模块引用
   *
   * @param queryParams 查询条件
   * @return SQL 分页列表
   */
  PageInfo<SimpleSkillSqlDTO> querySimpleSkillSqlPage(SkillQueryParams queryParams);

  /**
   * sql参数解析
   *
   * @param params 解析参数
   * @return 参数列表
   */
  ParseSqlResult parseSql(SqlOperaParams params);

}
