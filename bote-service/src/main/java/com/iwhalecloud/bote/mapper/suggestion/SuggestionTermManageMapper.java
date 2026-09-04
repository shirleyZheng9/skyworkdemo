package com.iwhalecloud.bote.mapper.suggestion;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.dto.suggestion.SuggestionTermDTO;
import com.iwhalecloud.bote.dto.suggestion.query.SuggestionTermQueryParams;
import com.iwhalecloud.bote.entity.suggestion.SuggestionTermEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 联想术语管理 Mapper
 *
 * @author lizuyin
 * @since 2025-06-25
 */
public interface SuggestionTermManageMapper {

  /**
   * 获取联想术语列表（分页）
   *
   * @param queryParams 查询条件
   * @param rowBounds 分页参数
   * @return 联想术语分页列表
   */
  Page<SuggestionTermDTO> selectSuggestionTermPage(@Param("query") SuggestionTermQueryParams queryParams, RowBounds rowBounds);

  /**
   * 新增联想术语
   *
   * @param entity 联想术语实体
   * @return 影响行数
   */
  int insertSuggestionTerm(SuggestionTermEntity entity);

  /**
   * 修改联想术语
   *
   * @param entity 联想术语实体
   * @return 影响行数
   */
  int updateSuggestionTerm(SuggestionTermEntity entity);

  /**
   * 删除联想术语
   *
   * @param termId 联想术语ID
   * @return 影响行数
   */
  int deleteSuggestionTerm(@Param("termId") Long termId, @Param("tenantId") Long tenantId);

  /**
   * 校验智能应用是否存在且有效
   *
   * @param sceneId 智能应用ID
   * @return 是否存在
   */
  boolean checkSceneExists(@Param("sceneId") Long sceneId);

  /**
   * 校验租户是否存在且有效
   *
   * @param tenantId 租户ID
   * @return 是否存在
   */
  boolean checkTenantExists(@Param("tenantId") Long tenantId);

  /**
   * 获取联想术语列表
   *
   * @param tenantId 租户ID
   * @return 联想术语列表
   */
  List<SuggestionTermDTO> selectSuggestionTerm(@Param("tenantId") Long tenantId);

  /**
   * 根据ID查询联想术语实体
   *
   * @param termId 联想术语ID
   * @param tenantId 租户ID
   * @return 联想术语实体
   */
  SuggestionTermEntity selectSuggestionTermById(@Param("termId") Long termId, @Param("tenantId") Long tenantId);

  /**
   * 检查联想术语是否已存在
   *
   * @param ownerId 归属者ID
   * @param ownerType 归属者类型
   * @param termType 联想话术类型
   * @param termContent 联想术语内容
   * @param tenantId 租户ID
   * @return 是否存在
   */
  boolean checkSuggestionTermExists(@Param("termId") Long termId,
                                   @Param("ownerId") Long ownerId,
                                   @Param("ownerType") String ownerType,
                                   @Param("termType") String termType,
                                   @Param("termContent") String termContent,
                                   @Param("tenantId") Long tenantId);

  /**
   * 批量插入联想术语
   *
   * @param entityList 联想术语实体列表
   * @return 影响行数
   */
  int batchInsertSuggestionTerms(@Param("list") List<SuggestionTermEntity> entityList);

  /**
   * 检查联想术语内容是否重复
   *
   * @param termContents 联想术语内容列表
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @param tenantId 租户ID
   * @return 已存在的联想术语内容列表
   */
  List<String> checkSuggestionTermContentsExist(@Param("termContents") List<String> termContents,
                                                @Param("ownerType") String ownerType,
                                                @Param("ownerId") Long ownerId,
                                                @Param("tenantId") Long tenantId);

  /**
   * 根据查询条件获取联想术语列表（用于导出）
   *
   * @param queryParams 查询参数
   * @return 联想术语列表
   */
  List<SuggestionTermDTO> selectSuggestionTermListForExport(@Param("query") SuggestionTermQueryParams queryParams);

  /**
   * 检查指定实例下是否存在联想术语数据
   *
   * @param tenantId 租户ID
   * @param ownerType 归属者类型
   * @param ownerId 归属者ID
   * @return 是否存在数据
   */
  boolean checkSuggestionTermExistsInInstance(@Param("tenantId") Long tenantId,
                                             @Param("ownerType") String ownerType,
                                             @Param("ownerId") Long ownerId);
}
