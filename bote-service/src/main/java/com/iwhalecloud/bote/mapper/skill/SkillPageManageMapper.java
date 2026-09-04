package com.iwhalecloud.bote.mapper.skill;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.skill.SkillPageDTO;
import com.iwhalecloud.bote.dto.skill.query.SkillQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 技能：页面 MAPPER
 *
 * @author auto
 * @since 2024-09-15
 */
public interface SkillPageManageMapper {

  /**
   * 新增页面
   *
   * @param skillPage 页面
   * @return 结果
   */
  int insertSkillPage(@Param("dto") SkillPageDTO skillPage);

  /**
   * 批量新增页面
   *
   * @param list 批量页面
   * @return 结果
   */
  int batchInsertSkillPage(@Param("list") List<SkillPageDTO> list);

  /**
   * 修改页面
   *
   * @param skillPage 页面
   * @return 结果
   */
  int updateSkillPage(@Param("dto") SkillPageDTO skillPage);

  /**
   * 根据主键获取页面
   *
   * @param tenantId 租户 ID
   * @param pageId 页面主键
   * @return 页面
   */
  SkillPageDTO getSkillPage(@Param("tenantId") Long tenantId, @Param("id") Long pageId);

  /**
   * 查询页面的简单信息
   */
  SkillPageDTO selectSimplePage(@Param("tenantId") Long tenantId, @Param("id") Long pageId);

  /**
   * 批量查询页面的简单信息
   */
  List<SkillPageDTO> selectSimplePages(@Param("tenantId") Long tenantId, @Param("ids") List<Long> pageIds);

  /**
   * 获取页面列表（分页）
   *
   * @param queryParams 查询条件
   * @return 页面分页列表
   */
  Page<SkillPageDTO> selectSkillPagePage(@Param("query") SkillQueryParams queryParams, RowBounds rowBounds);

  /**
   * 删除页面
   */
  int deleteSkillPage(@Param("tenantId") Long tenantId, @Param("pageId") Long pageId, @Param("updatorId") Long updatorId);

  /**
   * 检验页面编码唯一性
   *
   * @param skillPage 页面
   * @return 结果
   */
  Boolean existsSkillPageCode(@Param("dto") SkillPageDTO skillPage);

  /**
   * 根据编码获取页面内容
   *
   * @param pageCode 页面编码
   * @param tenantId 租户ID
   * @return 页面内容
   */
  String getSkillPageContentByCode(@Param("pageCode") String pageCode, @Param("tenantId") Long tenantId);

  /**
   * 根据页面编码获取页面文件 ID
   *
   * @param pageCode 页面编码
   * @param tenantId 租户ID
   * @return 页面文件 ID
   */
  @Nullable
  Long getFileIdByPageCode(@Param("pageCode") String pageCode, @Param("tenantId") Long tenantId);
}
