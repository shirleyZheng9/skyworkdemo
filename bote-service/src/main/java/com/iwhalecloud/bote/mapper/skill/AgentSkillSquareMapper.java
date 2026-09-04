package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillSquareExistingKey;
import com.iwhalecloud.bote.dto.skill.SkillTypeStatisticsVO;
import com.iwhalecloud.bote.entity.skill.AgentSkillSquareEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

/**
 * SKILL广场技能元数据 Mapper
 *
 * @author skill-square
 * @since 2026-03-18
 */
public interface AgentSkillSquareMapper {

  int insert(AgentSkillSquareEntity entity);

  int updateById(AgentSkillSquareEntity entity);

  int updateStatus(@Param("skillId") Long skillId, @Param("onlineStatus") String onlineStatus);

  int updateStatusCd(@Param("skillId") Long skillId, @Param("statusCd") String statusCd);

  int incrementInstallCount(@Param("skillId") Long skillId);

  AgentSkillSquareEntity selectById(@Param("skillId") Long skillId);

  AgentSkillSquareEntity selectByCode(@Param("skillCode") String skillCode);

  Page<AgentSkillSquareEntity> selectPage(@Param("type") String type,
                                          @Param("keyword") String keyword,
                                          @Param("onlineStatus") String onlineStatus,
                                          @Param("statusCd") String statusCd,
                                          @Param("spaceId") Long spaceId,
                                          @Param("tenantId") Long tenantId,
                                          @Param("userId") Long userId,
                                          @Param("installed") Boolean installed,
                                          RowBounds rowBounds);

  long countPage(@Param("type") String type,
                 @Param("keyword") String keyword,
                 @Param("onlineStatus") String onlineStatus,
                 @Param("statusCd") String statusCd);

  Page<AgentSkillSquareEntity> selectAdminPage(@Param("keyword") String keyword,
                                               @Param("onlineStatus") String onlineStatus,
                                               @Param("source") String source,
                                               @Param("skillType") String skillType,
                                               @Param("statusCd") String statusCd,
                                               RowBounds rowBounds);

  long countAdminPage(@Param("keyword") String keyword,
                      @Param("onlineStatus") String onlineStatus,
                      @Param("source") String source,
                      @Param("skillType") String skillType,
                      @Param("statusCd") String statusCd);

  /**
   * 批量导入前分页预加载有效技能编码与主键、包文件引用（与 {@link #selectPage} 相同：RowBounds + PageHelper）
   */
  Page<SkillSquareExistingKey> selectExistingKeysForBulkImportPage(RowBounds rowBounds);

  /**
   * 分页导出（避免一次加载全表）；按安装量降序、skill_id 升序；{@code topLimit} 非空且大于 0 时仅在该前 N 条内分页
   */
  List<AgentSkillSquareEntity> selectForExportPage(@Param("topLimit") Integer topLimit,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);

  /**
   * 可导出记录数（与 {@link #selectForExportPage} 相同范围）
   *
   * @param topLimit 非空且大于 0 时最多计该条数（安装量 Top N），否则计全部上架可导出技能
   */
  long countForExport(@Param("topLimit") Integer topLimit);

  /**
   * 按照广场技能类型统计数量（仅统计上架且有效的技能）
   *
   * @return 各类型技能的数量统计列表
   */
  List<SkillTypeStatisticsVO> countSkillByType();
}
