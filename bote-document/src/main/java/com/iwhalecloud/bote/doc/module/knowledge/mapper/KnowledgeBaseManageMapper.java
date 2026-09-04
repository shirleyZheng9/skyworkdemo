package com.iwhalecloud.bote.doc.module.knowledge.mapper;

import com.github.pagehelper.Page;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.KnowledgeComboboxDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.SimpleKnowledgeBaseDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.UpdateKnowlegeVisibilityScopeDTO;
import com.iwhalecloud.bote.dto.knowledge.docchain.DocChainExtraCfgDTO;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.DocKnowledgeBaseQueryParams;
import com.iwhalecloud.bote.doc.module.knowledge.dto.query.KnowledgeComboboxQueryParams;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.lang.Nullable;

/**
 * 知识库管理
 *
 * @author auto
 * @since 2024-09-20
 */
public interface KnowledgeBaseManageMapper {

  /**
   * 校验知识库的编码唯一性
   *
   * @param knowledgeBase 知识库
   * @return 结果
   */
  boolean existsKnowledgeBaseName(@Param("dto") KnowledgeBaseDTO knowledgeBase);

  /**
   * 根据主键获取知识库
   *
   * @param knowledgeId 知识库主键
   * @return 知识库
   */
  KnowledgeBaseDTO getKnowledgeBase(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId, @Param("userId") Long userId);

  /**
   * 查询知识库状态
   */
  @Nullable
  String selectKnowledgeStatus(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  /**
   * 新增知识库
   *
   * @param knowledgeBase 知识库
   * @return 结果
   */
  int insertKnowledgeBase(@Param("dto") KnowledgeBaseDTO knowledgeBase);

  /**
   * 修改知识库
   *
   * @param knowledgeBase 知识库
   * @return 结果
   */
  int updateKnowledgeBase(@Param("dto") KnowledgeBaseDTO knowledgeBase);

  /**
   * 修改知识库，用于重构
   *
   * @param knowledgeBase 知识库
   * @return 结果
   */
  int updateKnowledgeForRebuild(@Param("dto") KnowledgeBaseDTO knowledgeBase);

  /**
   * 删除属性
   *
   * @param tenantId 租户 ID
   * @param knowledgeId 主键 ID
   * @param updatorId 操作人 ID
   * @return 结果
   */
  int deleteKnowledgeBase(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId, @Param("updatorId") Long updatorId);

  /**
   * 获取知识库列表
   *
   * @param queryParams 查询条件
   * @return 知识库列表
   */
  List<SimpleKnowledgeBaseDTO> selectKnowledgeBaseList(@Param("query") DocKnowledgeBaseQueryParams queryParams);

  /**
   * 获取知识库列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库分页列表
   */
  Page<KnowledgeBaseDTO> selectKnowledgeBasePage(@Param("query") DocKnowledgeBaseQueryParams queryParams, RowBounds rowBounds);

  /**
   * 获取知识库列表（分页）
   *
   * @param queryParams 查询条件
   * @return 知识库分页列表
   */
  Page<SimpleKnowledgeBaseDTO> selectSimpleKnowledgeBasePage(@Param("query") DocKnowledgeBaseQueryParams queryParams, RowBounds rowBounds);

  SimpleKnowledgeBaseDTO getSimpleKnowledge(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  /**
   * 分页查询知识库下拉列表
   */
  Page<KnowledgeComboboxDTO> selectKnowledgeComboboxPage(@Param("query") KnowledgeComboboxQueryParams queryParams, RowBounds rowBounds);

  /**
   * 查询知识库简单信息
   */
  @Nullable
  KnowledgeBaseDTO selectSimpleKnowledgeById(@Param("tenantId") Long tenantId, @Param("knowledgeId") Long knowledgeId);

  /**
   * 批量查询知识库简单信息
   */
  List<KnowledgeBaseDTO> selectSimpleKnowledgeByIds(@Param("tenantId") Long tenantId, @Param("knowledgeIds") List<Long> knowledgeId);

  /**
   * 查询 DocChain 主题扩展参数定义
   */
  List<DocChainExtraCfgDTO> selectAllDocChainExtraCfg();

  /**
   * 查询 DocChain 动态主题扩展参数定义
   */
  List<DocChainExtraCfgDTO> selectDynamicDocChainExtraCfg();

  /**
   * 更新时间，标记 DocChain 策略设置发生变动
   */
  int updateDocChainExtraCfgTime();

  KnowledgeBaseDTO selectKnowledgeBaseById(@Param("knowledgeId") Long knowledgeId);

  int updateKnowledgeBaseVisibilityScope(UpdateKnowlegeVisibilityScopeDTO params);
}
