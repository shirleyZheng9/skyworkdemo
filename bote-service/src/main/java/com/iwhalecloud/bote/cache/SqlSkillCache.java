package com.iwhalecloud.bote.cache;

import com.iwhalecloud.bote.common.consts.CacheConsts;
import com.iwhalecloud.bote.dto.skill.SimpleSqlSkillDTO;
import com.iwhalecloud.bote.dto.skill.SkillSqlDTO;
import com.iwhalecloud.bote.entity.skill.SkillSqlEntity;
import com.iwhalecloud.bote.mapper.skill.SkillSqlManageMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * SQL 技能缓存
 *
 * <p>缓存 key 为 tenantId:serviceId</p>
 *
 * @author bianjp
 * @since 2024-12-17
 */
@Component
public final class SqlSkillCache extends AbstractSkillCache<SimpleSqlSkillDTO> {
  private final SkillSqlManageMapper skillSqlMapper;

  public SqlSkillCache(SkillSqlManageMapper skillSqlMapper) {
    super(CacheConsts.KEY_PREFIX_SQL);
    this.skillSqlMapper = skillSqlMapper;
    disableDistributionCache();
  }

  @Override
  public String getCacheName() {
    return CacheConsts.CACHE_NAME_SQL;
  }

  @Nullable
  @Override
  protected SimpleSqlSkillDTO loadById(Long tenantId, Long id) {
    SkillSqlDTO skill = skillSqlMapper.selectSimpleSql(tenantId, id);
    return skill == null ? null : SimpleSqlSkillDTO.from(skill);
  }

  @Override
  protected Map<Long, SimpleSqlSkillDTO> loadByIds(Long tenantId, List<Long> ids) {
    return skillSqlMapper.selectSimpleSqls(tenantId, ids).stream()
      .collect(Collectors.toMap(SkillSqlEntity::getServiceId, SimpleSqlSkillDTO::from));
  }
}
